package org.broadinstitute.hellbender.tools.coveragemodel.nd4j;

import org.apache.commons.math3.util.FastMath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadinstitute.hellbender.tools.coveragemodel.TargetCoverageEMParams;
import org.broadinstitute.hellbender.tools.coveragemodel.TargetCoverageEMWorkspace;
import org.broadinstitute.hellbender.tools.coveragemodel.TargetCoverageModel;
import org.broadinstitute.hellbender.tools.exome.ReadCountCollection;
import org.broadinstitute.hellbender.tools.exome.ReadCountRecord;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.buffer.util.DataTypeUtil;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;


import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Double.NaN;
import static java.lang.Double.isNaN;

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

    private static final double DEFAULT_SAMPLE_BIAS_ON_MISSING_TARGETS = 0;

    /**********************
     * persistent members *
     **********************/

    /* $n_{st}$ */
    private final INDArray sampleReadCounts;

    /* $log(n_{st})$ */
    private final INDArray sampleLogReadCounts;

    /* P_{st} */
    private final INDArray sampleGermlineCopyNumber;

    /* M_{st} */
    private final INDArray sampleMasks;

    /* \sum_s M_{st} */
    private final INDArray sampleSummedMasks;

    /* $\Sigma$ */
    private final INDArray sampleStatisticalVariances;

    /**********
     * caches *
     **********/

    /* $d_s$ */
    private INDArray sampleMeanReadDepths = null;

    /* $c_{st}$ */
    private INDArray sampleCopyRatios = null;

    /* $m_{st}$ */
    private INDArray sampleBiases = null;

    /* $\Psi_t + \Sigma_{st} */
    private INDArray sampleTotalVariances = null;

    /* $M_{st}/(\Psi_t + \Sigma_{st})$ */
    private INDArray sampleMaskedInverseTotalVariances = null;

    /* $B_{st}$ */
    private INDArray sampleSignalQuadraticPosteriors = null;

    /* $E[z_{sm}]$ */
    private INDArray sampleLatentPosteriorFirstMoments = null;

    /* $E[z_{sm} z_{sn}]$ */
    private INDArray sampleLatentPosteriorSecondMoments = null;

    /*********
     * model *
     *********/

    private final TargetCoverageModel<INDArray, INDArray> model;

    /**
     * Constructor
     *
     * TODO germline copy number
     *
     * @param readCounts a {@link ReadCountCollection} of the samples, not a {@code null}
     */
    public TargetCoverageEMWorkspaceNd4j(final ReadCountCollection readCounts,
                                         final TargetCoverageEMParams params) {
        super(readCounts, params);

        /* parse reads and initialize containers */
        final List<ReadCountRecord> recs = readCounts.records();

        /* read counts, statistical variances and masks */
        double[][] sampleReadCountsArray2D = new double[numSamples][numTargets];
        double[][] sampleLogReadCountsArray2D = new double[numSamples][numTargets];
        double[][] sampleGermlineCopyNumberArray2D = new double[numSamples][numTargets];
        double[][] sampleMasksArray2D = new double[numSamples][numTargets];
        double[][] sampleStatisticalVariancesArray2D = new double[numSamples][numTargets];

        sampleIndexStream().forEach(s -> {
            double[] sampleRawReadCount = recs.stream().mapToDouble(rec -> rec.getDouble(s)).toArray();
            sampleReadCountsArray2D[s] = sampleRawReadCount;
            sampleMasksArray2D[s] = Arrays.stream(sampleRawReadCount).map(n -> (int)n > 0 ? 1 : 0).toArray();
            double[] sampleFixedReadCount = Arrays.stream(sampleRawReadCount)
                    .map(n -> (int)n > 0 ? n : READ_COUNT_ON_MISSING_TARGETS_FIXUP_VALUE).toArray();
            sampleLogReadCountsArray2D[s] = Arrays.stream(sampleFixedReadCount).map(FastMath::log).toArray();
            sampleStatisticalVariancesArray2D[s] = Arrays.stream(sampleFixedReadCount).map(n -> 1.0/n).toArray();
            sampleGermlineCopyNumberArray2D[s] = IntStream.range(0, numTargets)
                    .mapToDouble(it -> DEFAULT_GERMLINE_COPY_NUMBER).toArray();
        });

        logger.warn("Assuming germline copy number " + DEFAULT_GERMLINE_COPY_NUMBER +
                " for all targets. In the future, germline copy number must be supplied and sample-target masks will be" +
                " generated from it.");

        logger.warn("Sample-target masks are inferred from read counts. In the future, it must be inferred from the" +
                " provided germline copy number.");

        sampleReadCounts = Nd4j.create(sampleReadCountsArray2D);
        sampleMasks = Nd4j.create(sampleMasksArray2D);
        sampleLogReadCounts = Nd4j.create(sampleLogReadCountsArray2D);
        sampleStatisticalVariances = Nd4j.create(sampleStatisticalVariancesArray2D);
        sampleGermlineCopyNumber = Nd4j.create(sampleGermlineCopyNumberArray2D);
        sampleSummedMasks = sampleMasks.sum(0);

        /* instantiate a model */
        model = new TargetCoverageModelNd4j(getNumTargets(), getTargetCoverageEMParams().getNumLatents());

        initializeMutableMembers();

        /* TODO set the status to TBD */
    }

    /**
     * Initializes mutable members to their initial values
     */
    private void initializeMutableMembers() {
        logger.debug("Initializing model parameters and EM workspace members");

        /* set $c_{st} <- 1$ on all targets */
        sampleCopyRatios = Nd4j.ones(numSamples, numTargets);

        /* estimate sample mean read depths assuming $e^{b_st} = 1$ and $c_{st} = 1$ */
        sampleMeanReadDepths = Nd4j.create(sampleIndexStream().mapToDouble(si -> estimateMeanReadDepth(
                sampleReadCounts.getRow(si), sampleGermlineCopyNumber.getRow(si), sampleMasks.getRow(si))).toArray(),
                new int[]{numSamples, 1});

        /* TODO take care of NaNs -- the latest ND4j on master has a replaceNaN according to raver119 */
        /* $m_{st} <- log(n_{st}/(P_{st} d_s))$ */
        sampleBiases = Transforms.log(sampleReadCounts.div(
                sampleGermlineCopyNumber.mulColumnVector(sampleMeanReadDepths)), false);

        /**
         *  set the model parameters to sensible initial values -- initially, there are all zero
         *  we set W to a truncated identity, leave Psi as zero, and set the target bias to the sample averaged
         *  bias assuming no CNV events
         *  */

        /* $W_{tm}$ */
        IntStream.range(0, model.getNumLatents()).forEach(ti ->
                model.setPrincipalLinearMapPerTarget(ti, Nd4j.zeros(model.getNumLatents()).putScalar(ti, 1.0)));

        /* $m_t$ */
        model.setTargetMeanBias(sampleBiases.mul(sampleMasks).sum(0).divi(sampleSummedMasks));
    }

    @Override
    public INDArray getSampleCopyRatio(final int sampleIndex) {
        assertSampleIndex(sampleIndex);
        return sampleCopyRatios.getRow(sampleIndex).dup();
    }

    @Override
    public void setSampleCopyRatio(final int sampleIndex, final INDArray newSampleCopyRatio) {
        assertSampleIndex(sampleIndex);
        sampleCopyRatios.putRow(sampleIndex, newSampleCopyRatio.dup());
    }

    @Override
    public double estimateMeanReadDepth(final INDArray readCount, final INDArray totalMultBias,
                                        final INDArray mask) {
        return TargetCoverageEMWorkspaceUtilsNd4j.estimateMeanReadDepth(readCount, totalMultBias, mask);
    }

    @Override
    public TargetCoverageModel<INDArray, INDArray> getModel() { return model; }

    public IntStream sampleIndexStream() { return IntStream.range(0, numSamples); }

}
