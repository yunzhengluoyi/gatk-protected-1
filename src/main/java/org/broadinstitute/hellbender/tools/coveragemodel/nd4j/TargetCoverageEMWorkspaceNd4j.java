package org.broadinstitute.hellbender.tools.coveragemodel.nd4j;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.FastMath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadinstitute.hellbender.tools.coveragemodel.TargetCoverageEMParams;
import org.broadinstitute.hellbender.tools.coveragemodel.TargetCoverageEMWorkspace;
import org.broadinstitute.hellbender.tools.coveragemodel.TargetSpaceBlock;
import org.broadinstitute.hellbender.tools.exome.ReadCountCollection;
import org.broadinstitute.hellbender.tools.exome.ReadCountRecord;
import org.broadinstitute.hellbender.utils.param.ParamUtils;
import org.junit.Assert;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This class implements a local memory version of {@link TargetCoverageEMWorkspace}
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */

public final class TargetCoverageEMWorkspaceNd4j extends TargetCoverageEMWorkspace<INDArray, INDArray> {

    private final Logger logger = LogManager.getLogger(TargetCoverageEMWorkspaceNd4j.class);

    /**
     * Targets with zero read counts will be promoted to the follolwing in {@code sampleLogReadCounts} and
     * {@code sampleStatsticalVariances}
     * */
    private static final int READ_COUNT_ON_MISSING_TARGETS_FIXUP_VALUE = 1;

    /**
     * In the absence of target copy number data, the following value will be used on all targets
     * and a warning will be logged
     */
    private static final int DEFAULT_GERMLINE_COPY_NUMBER = 2;

    /**
     * Not used now, but will be used in the future when we handle zero germline copy targets
     */
    private static final double DEFAULT_SAMPLE_BIAS_ON_MISSING_TARGETS = 0;

    /**
     * Default number of target space blocks
     */
    private static final int DEFAULT_NUMBER_OF_TARGET_BLOCKS = 5;

    /**
     * Minimum size of a target block
     */
    private static final int DEFAULT_MIN_TARGET_BLOCK_SIZE = 5;

    /*****************
     * small members *
     *****************/

    private final int numTargetBlocks;

    /**********************
     * persistent members *
     **********************/

    /* $n_{st}$ */
    private INDArray sampleReadCounts;

    /* $log(n_{st})$ */
    private INDArray sampleLogReadCounts;

    /* P_{st} */
    private INDArray sampleGermlineCopyNumber;

    /* M_{st} */
    private INDArray sampleMasks;

    /* \sum_s M_{st} */
    private INDArray sampleSummedMasks;

    /* $\Sigma$ */
    private INDArray sampleStatisticalVariances;

    /***********************
     * distributed members *
     ***********************/

    /* NOTE: in a spark implementation, List must be replaced with JavaRDD */

    private List<TargetSpaceBlock> targetBlocks;
    private Map<TargetSpaceBlock, TargetCoverageModelBlockNd4J> modelBlocksMap;
    private Map<TargetSpaceBlock, TargetCoverageEMCacheBlockNd4j> cacheBlocksMap;
    private Map<TargetSpaceBlock, Pair<TargetCoverageModelBlockNd4J, TargetCoverageEMCacheBlockNd4j>> pairedMap;

    /*************************
     * small mutable members *
     *************************/

    /* $d_s$ */
    private INDArray sampleMeanReadDepths = null;

    /* $E[z_{sm}]$ */
    private INDArray sampleLatentPosteriorFirstMoments = null;

    /* $E[z_{sm} z_{sn}]$ */
    private INDArray sampleLatentPosteriorSecondMoments = null;

    public TargetCoverageEMWorkspaceNd4j(@Nonnull final ReadCountCollection readCounts,
                                         @Nonnull final TargetCoverageEMParams params,
                                         final int numTargetBlocks) {
        super(readCounts, params);
        this.numTargetBlocks = ParamUtils.inRange(numTargetBlocks, 1, numTargets, "Number of target blocks must be " +
                "between 1 and the size of target space.");

        initializeTargetBlocks();
        initializeBlocksAndMaps();
        initializePersistentMembers(readCounts);
        initializeMutableMembers();
    }

    private void initializeTargetBlocks() {
        final int targetBlockSize = FastMath.max(numTargets/numTargetBlocks, DEFAULT_MIN_TARGET_BLOCK_SIZE);
        targetBlocks = new ArrayList<>();
        for (int begTargetIndex = 0; begTargetIndex < numTargets; begTargetIndex += targetBlockSize) {
            targetBlocks.add(new TargetSpaceBlock(begTargetIndex, FastMath.min(begTargetIndex + targetBlockSize, numTargets)));
        }
        if (targetBlocks.size() == 0) {
            throw new RuntimeException("Target space paritioning error!");
        }
        while (targetBlocks.size() > numTargetBlocks && targetBlocks.size() > 1) {
            final int newBegIndex = targetBlocks.get(targetBlocks.size() - 2).getBegIndex();
            final int newEndIndex = targetBlocks.get(targetBlocks.size() - 1).getEndIndex();
            targetBlocks.remove(targetBlocks.size() - 1);
            targetBlocks.remove(targetBlocks.size() - 1);
            targetBlocks.add(new TargetSpaceBlock(newBegIndex, newEndIndex));
        }
        Assert.assertEquals(numTargetBlocks, targetBlocks.size());
        logger.debug("Target space blocks: " + targetBlocks.stream().map(TargetSpaceBlock::toString)
                .reduce((L, R) -> L + "\t" + R).orElse("None"));
    }

    private void initializeBlocksAndMaps() {
        modelBlocksMap = new HashMap<>();
        targetBlockStream().forEach(tb -> modelBlocksMap.put(tb,
                new TargetCoverageModelBlockNd4J(tb, params.getNumLatents())));
        cacheBlocksMap = new HashMap<>();
        targetBlockStream().forEach(tb -> cacheBlocksMap.put(tb,
                new TargetCoverageEMCacheBlockNd4j(tb, numSamples)));
        pairedMap = new HashMap<>();
        targetBlockStream().forEach(tb -> pairedMap.put(tb,
                new ImmutablePair<>(modelBlocksMap.get(tb), cacheBlocksMap.get(tb))));
    }

    private void initializePersistentMembers(final ReadCountCollection readCounts) {
        /* parse reads and initialize containers */
        final List<ReadCountRecord> recs = readCounts.records();

        sampleReadCounts = Nd4j.create(numSamples, numTargets);
        sampleMasks = Nd4j.create(numSamples, numTargets);
        sampleLogReadCounts = Nd4j.create(numSamples, numTargets);
        sampleStatisticalVariances = Nd4j.create(numSamples, numTargets);
        sampleGermlineCopyNumber = Nd4j.create(numSamples, numTargets);

        targetBlockStream().forEach(tb -> {
            /* take a contiguous (target chuck) x numSamples chunk from the read count collection */
            double[] rawReadCountBlock = IntStream.range(tb.getBegIndex(), tb.getEndIndex())
                    .mapToObj(ti -> Arrays.asList(ArrayUtils.toObject(recs.get(ti).getDoubleCounts())))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList())
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .toArray();

            /* derived blocks */
            double[] maskBlock = Arrays.stream(rawReadCountBlock).map(n -> (int)n > 0 ? 1 : 0).toArray();
            double[] fixedReadCountBlock = Arrays.stream(rawReadCountBlock)
                    .map(n -> (int)n > 0 ? n : READ_COUNT_ON_MISSING_TARGETS_FIXUP_VALUE).toArray();
            double[] logReadCountBlock = Arrays.stream(fixedReadCountBlock).map(FastMath::log).toArray();
            double[] statisticalVarianceBlock = Arrays.stream(fixedReadCountBlock).map(n -> 1.0/n).toArray();
            double[] germlineCopyNumberBlock =  IntStream.range(0, rawReadCountBlock.length)
                    .mapToDouble(it -> DEFAULT_GERMLINE_COPY_NUMBER).toArray();

            INDArrayIndex[] blockIndex = new INDArrayIndex[2];
            blockIndex[0] = NDArrayIndex.interval(0, numSamples);
            blockIndex[1] = NDArrayIndex.interval(tb.getBegIndex(), tb.getEndIndex());

            /* populate persistent members */
            sampleReadCounts.get(blockIndex).assign(Nd4j.create(
                    rawReadCountBlock, new int[]{numSamples, tb.getNumTargets()}, 'f'));
            sampleMasks.get(blockIndex).assign(Nd4j.create(
                    maskBlock, new int[]{numSamples, tb.getNumTargets()}, 'f'));
            sampleLogReadCounts.get(blockIndex).assign(Nd4j.create(
                    logReadCountBlock, new int[]{numSamples, tb.getNumTargets()}, 'f'));
            sampleStatisticalVariances.get(blockIndex).assign(Nd4j.create(
                    statisticalVarianceBlock, new int[]{numSamples, tb.getNumTargets()}, 'f'));
            sampleGermlineCopyNumber.get(blockIndex).assign(Nd4j.create(
                    germlineCopyNumberBlock, new int[]{numSamples, tb.getNumTargets()}, 'f'));
        });

        logger.warn("Assuming germline copy number " + DEFAULT_GERMLINE_COPY_NUMBER +
                " for all targets. In the future, germline copy number must be supplied and sample-target masks will be" +
                " generated from it.");

        logger.warn("Sample-target masks are inferred from read counts. In the future, it must be inferred from the" +
                " provided germline copy number.");

        sampleSummedMasks = sampleMasks.sum(0);
    }

    /**
     * Initializes mutable members to their initial values
     */
    private void initializeMutableMembers() {
//        logger.debug("Initializing model parameters and EM workspace members");
//
//        /* set $c_{st} <- 1$ on all targets */
//        sampleCopyRatios = Nd4j.ones(numSamples, numTargets);
//
//        /* estimate sample mean read depths assuming $e^{b_st} = 1$ and $c_{st} = 1$ */
//        sampleMeanReadDepths = Nd4j.create(sampleIndexStream().mapToDouble(si -> estimateMeanReadDepth(
//                sampleReadCounts.getRow(si), sampleGermlineCopyNumber.getRow(si), sampleMasks.getRow(si))).toArray(),
//                new int[]{numSamples, 1});
//
//        /* TODO take care of NaNs -- the latest ND4j on master has a replaceNaN according to raver119 */
//        /* $m_{st} <- log(n_{st}/(P_{st} d_s))$ */
//        sampleBiases = Transforms.log(sampleReadCounts.div(
//                sampleGermlineCopyNumber.mulColumnVector(sampleMeanReadDepths)), false);
//
//        /**
//         *  set the model parameters to sensible initial values -- initially, there are all zero
//         *  we set W to a truncated identity, leave Psi as zero, and set the target bias to the sample averaged
//         *  bias assuming no CNV events
//         *  */
//
//        /* $W_{tm}$ */
//        IntStream.range(0, model.getNumLatents()).forEach(ti ->
//                model.setPrincipalLinearMapPerTarget(ti, Nd4j.zeros(model.getNumLatents()).putScalar(ti, 1.0)));
//
//        /* $m_t$ */
//        model.setTargetMeanBias(sampleBiases.mul(sampleMasks).sum(0).divi(sampleSummedMasks));
    }

//    @Override
//    public INDArray getSampleCopyRatio(final int sampleIndex) {
//        assertSampleIndex(sampleIndex);
//        return sampleCopyRatios.getRow(sampleIndex).dup();
//    }
//
//    @Override
//    public void setSampleCopyRatio(final int sampleIndex, final INDArray newSampleCopyRatio) {
//        assertSampleIndex(sampleIndex);
//        sampleCopyRatios.putRow(sampleIndex, newSampleCopyRatio.dup());
//    }

    @Override
    public double estimateMeanReadDepth(final INDArray readCount, final INDArray totalMultBias,
                                        final INDArray mask) {
        return org.broadinstitute.hellbender.tools.coveragemodel.nd4j.TargetCoverageEMWorkspaceNd4jUtils.estimateMeanReadDepth(readCount, totalMultBias, mask);
    }

//    @Override
//    public TargetCoverageModelBlock<INDArray, INDArray> getModel() { return model; }

    public IntStream sampleIndexStream() { return IntStream.range(0, numSamples); }

    public Stream<TargetSpaceBlock> targetBlockStream() { return targetBlocks.stream(); }


}
