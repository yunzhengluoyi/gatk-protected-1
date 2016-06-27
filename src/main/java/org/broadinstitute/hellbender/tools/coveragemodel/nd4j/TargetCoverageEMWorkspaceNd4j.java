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
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.ops.transforms.Transforms;

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
    private static final int READ_COUNT_ON_UNCOVERED_TARGETS_FIXUP_VALUE = 1;

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

    /***********************
     * distributed members *
     ***********************/

    /* NOTE: in a spark implementation, List must be replaced with JavaRDD */

    private List<TargetSpaceBlock> targetBlocks;
    private Map<TargetSpaceBlock, TargetCoverageModelBlockNd4J> modelBlocksMap;
    private Map<TargetSpaceBlock, TargetCoverageEMCacheBlockNd4j> cacheBlocksMap;
    private Map<TargetSpaceBlock, Pair<TargetCoverageModelBlockNd4J,
            TargetCoverageEMCacheBlockNd4j>> pairMap;

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
        pairMap = new HashMap<>();
        targetBlockStream().forEach(tb -> pairMap.put(tb,
                new ImmutablePair<>(modelBlocksMap.get(tb), cacheBlocksMap.get(tb))));
    }

    private void initializePersistentMembers(final ReadCountCollection readCounts) {
        /* parse reads and initialize containers */
        final List<ReadCountRecord> recs = readCounts.records();

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
                    .map(n -> (int)n > 0 ? n : READ_COUNT_ON_UNCOVERED_TARGETS_FIXUP_VALUE).toArray();
            double[] logReadCountBlock = Arrays.stream(fixedReadCountBlock).map(FastMath::log).toArray();
            double[] statisticalVarianceBlock = Arrays.stream(fixedReadCountBlock).map(n -> 1.0/n).toArray();
            double[] germlineCopyNumberBlock =  IntStream.range(0, rawReadCountBlock.length)
                    .mapToDouble(it -> DEFAULT_GERMLINE_COPY_NUMBER).toArray();

            /** populate persistent members
             * note: for arrays shaped like S X T, the read buffer must be mapped in Fortran order
             */
            cacheBlocksMap.get(tb).sampleReadCounts.assign(Nd4j.create(
                    rawReadCountBlock, new int[]{numSamples, tb.getNumTargets()}, 'f'));
            cacheBlocksMap.get(tb).sampleMasks.assign(Nd4j.create(
                    maskBlock, new int[]{numSamples, tb.getNumTargets()}, 'f'));
            cacheBlocksMap.get(tb).sampleLogReadCounts.assign(Nd4j.create(
                    logReadCountBlock, new int[]{numSamples, tb.getNumTargets()}, 'f'));
            cacheBlocksMap.get(tb).sampleStatisticalVariances.assign(Nd4j.create(
                    statisticalVarianceBlock, new int[]{numSamples, tb.getNumTargets()}, 'f'));
            cacheBlocksMap.get(tb).sampleGermlineCopyNumber.assign(Nd4j.create(
                    germlineCopyNumberBlock, new int[]{numSamples, tb.getNumTargets()}, 'f'));
        });

        logger.warn("Assuming germline copy number " + DEFAULT_GERMLINE_COPY_NUMBER +
                " for all targets. In the future, germline copy number must be supplied and sample-target masks will be" +
                " generated from it.");

        logger.warn("Sample-target masks are inferred from read counts. In the future, it must be inferred from the" +
                " provided germline copy number.");

        cacheBlocksMap.values().stream().forEach(cache -> cache.sampleSummedMasks.assign(cache.sampleMasks.sum(0)));
    }

    /**
     * Initializes mutable members to their initial values
     */
    private void initializeMutableMembers() {
        logger.debug("Initializing model parameters and EM workspace members");
        initializeSampleCopyRatios();
        initializeSampleReadDepths();
        initializeSampleBiases();
        initializeModelParameters();
        updateCaches();
        initializePosteriors();
    }

    /**
     * set $c_{st} <- 1$ on all targets in all cache blocks
     */
    private void initializeSampleCopyRatios() {
        cacheBlocksMap.values().stream().forEach(cb -> cb.setCopyRatiosToConstant(1.0));
    }

    /**
     * Estimate sample mean read depths assuming $e^{b_st} = 1$ and $c_{st} = 1$, such that the total
     * multiplicative bias is the germline copy number
     */
    private void initializeSampleReadDepths() {
        INDArray num = cacheBlocksMap.values().stream()
                .map(TargetCoverageEMCacheBlockNd4j::getSampleReadDepthEstimatorNumeratorInitial)
                .reduce(Nd4j.zeros(numSamples, 1), INDArray::addi);
        INDArray denom = cacheBlocksMap.values().stream()
                .map(TargetCoverageEMCacheBlockNd4j::getSampleReadDepthEstimatorDenominatorInitial)
                .reduce(Nd4j.zeros(numSamples, 1), INDArray::addi);
        sampleMeanReadDepths = num.div(denom);
    }

    /**
     * $m_{st} <- log(n_{st}/(P_{st} d_s))$
     *
     * TODO take care of NaNs -- the latest ND4j on master has a replaceNaN according to raver119
     */
    private void initializeSampleBiases() {
        cacheBlocksMap.values().stream().forEach(cache ->
            cache.sampleBiases.assign(Transforms.log(cache.sampleReadCounts.div(
                    cache.sampleGermlineCopyNumber.mulColumnVector(sampleMeanReadDepths)), false)));
    }

    /**
     *  Set the model parameters to sensible initial values -- initially, there are all zero
     *  we set W to a truncated identity, leave Psi as zero, and set the target bias to the sample averaged
     *  bias assuming no CNV events
     */
    private void initializeModelParameters() {
        /* set $W_{tm}$ to a zero-padded D X D identity matrix, and $m_t$ to mask averaged $m_{st}$ */
        targetBlockStream().forEach(tb -> {
            TargetCoverageEMCacheBlockNd4j cache = cacheBlocksMap.get(tb);
            TargetCoverageModelBlockNd4J model = modelBlocksMap.get(tb);
            /* $W_{tm}$ */
            if (tb.getBegIndex() < params.getNumLatents()) {
                IntStream.range(tb.getBegIndex(), FastMath.min(tb.getEndIndex(), params.getNumLatents())).forEach(ti ->
                    model.setPrincipalLinearMapPerTarget(ti, Nd4j.zeros(1, params.getNumLatents()).putScalar(0, ti, 1.0)));
            }
            /* $m_t$ */
            model.setTargetMeanBias(cache.sampleBiases.mul(cache.sampleMasks).sum(0).div(cache.sampleSummedMasks));
            /* nothing to do for $\Psi_t$ */
        });
    }

    /**
     * Update caches that derive from $\Psi_t$
     */
    private void updateCaches() {
        pairMap.values().stream().forEach(p -> p.getRight().updateCaches(p.getLeft()));
    }

    /**
     * Set posterior expectation values to zero
     */
    private void initializePosteriors() {
        sampleLatentPosteriorFirstMoments = Nd4j.zeros(numSamples, params.getNumLatents());
        sampleLatentPosteriorSecondMoments = Nd4j.zeros(numSamples, params.getNumLatents(), params.getNumLatents());
    }

    /**
     * Calculate the G matrix of a given sample
     * Note: it is assumed that the variance caches are up to date
     *
     * [G]_s = ([I] + [W]^T [diag(M_st \Psi_st)] [W])^{-1}
     *
     * @return List of G matrices
     */
    private INDArray calculateSampleGMatrix(final int sampleIndex) {
        return TargetCoverageEMWorkspaceNd4jUtils.invertNDMatrix(pairMap.values().stream()
                .map(p -> p.getLeft().wtdw(p.getRight().sampleMaskedInverseTotalVariances.getRow(sampleIndex)))
                .reduce(Nd4j.eye(params.getNumLatents()), INDArray::addi));
    }

    /**
     * Get numpy-style slices {@code E[z[sampleIndex, :]]} and {@code E[z[sampleIndex, :] z[sampleIndex, :]^T]}
     * @param sampleIndex
     * @return
     */
    private ImmutablePair<INDArray, INDArray> getLatentPosteriorSlice(final int sampleIndex) {
        return ImmutablePair.of(sampleLatentPosteriorFirstMoments.getRow(sampleIndex),
                    sampleLatentPosteriorSecondMoments.get(NDArrayIndex.point(sampleIndex), NDArrayIndex.all(), NDArrayIndex.all()));
    }

    /**
     * Update the first and second posterior moments of the bias latent variable for sample {@code sampleIndex}
     * @param sampleIndex index of the sample to process
     */
    private void updateLatentPosteriorExpectationsSingle(final int sampleIndex) {
        /* calculate G_s */
        INDArray sampleGMatrix = calculateSampleGMatrix(sampleIndex);

        /* get the slice of posterior expectation containers corresponding to the sample */
        ImmutablePair<INDArray, INDArray> latentPosteriorSlice = getLatentPosteriorSlice(sampleIndex);

        /* E[z_s] = G_s W^T M_{st} \Psi_{st}^{-1} (m_{st} - m_t) */
        latentPosteriorSlice.getLeft().assign(
                sampleGMatrix.mmul(pairMap.values().stream()
                        .map(p -> p.getLeft().wtv(
                                p.getRight().sampleMaskedInverseTotalVariances.getRow(sampleIndex)
                                        .mul(p.getRight().sampleBiasDeviations.getRow(sampleIndex))))
                        .reduce(Nd4j.zeros(params.getNumLatents(), 1), INDArray::addi)));

        /* E[z_s z_s^T] = G_s + E[z_s] E[z_s^T] */
        latentPosteriorSlice.getRight().assign(sampleGMatrix.add(
                latentPosteriorSlice.getLeft().transpose().mmul(latentPosteriorSlice.getLeft())));
    }

    /**
     * Update posterior moments for all samples
     */
    public void updateLatentPosteriorExpectationsAll() {
        sampleIndexStream().forEach(this::updateLatentPosteriorExpectationsSingle);
    }

    public IntStream sampleIndexStream() { return IntStream.range(0, numSamples); }

    public Stream<TargetSpaceBlock> targetBlockStream() { return targetBlocks.stream(); }

}
