package org.broadinstitute.hellbender.tools.coveragemodel;

import org.apache.commons.math3.util.FastMath;
import org.apache.http.util.Asserts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadinstitute.hellbender.exceptions.UserException;
import org.broadinstitute.hellbender.tools.exome.ReadCountCollection;
import org.broadinstitute.hellbender.tools.exome.ReadCountRecord;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;


import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * This class implements a local memory version of {@link TargetCoverageEMWorkspace}
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */

public final class TargetCoverageEMWorkspaceNd4j extends TargetCoverageEMWorkspace {

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

        IntStream.range(0, numSamples).forEach(s -> {
            int[] sampleRawReadCount = recs.stream().mapToInt(rec -> (int)rec.getDouble(s)).toArray();
            sampleReadCountsArray2D[s] = Arrays.stream(sampleRawReadCount)
                    .mapToDouble(FastMath::log).toArray();
            sampleMasksArray2D[s] = Arrays.stream(sampleRawReadCount)
                    .mapToDouble(n -> n > 0 ? 0 : 1).toArray();
            int[] sampleFixedReadCount = Arrays.stream(sampleRawReadCount)
                    .map(n -> n > 0 ? n : READ_COUNT_ON_MISSING_TARGETS_FIXUP_VALUE).toArray();
            sampleLogReadCountsArray2D[s] = Arrays.stream(sampleFixedReadCount)
                    .mapToDouble(FastMath::log).toArray();
            sampleStatisticalVariancesArray2D[s] = Arrays.stream(sampleFixedReadCount)
                    .mapToDouble(n -> 1.0 / n).toArray();
            sampleGermlineCopyNumberArray2D[s] = IntStream.range(0, numTargets)
                    .mapToDouble(it -> DEFAULT_GERMLINE_COPY_NUMBER).toArray();
        });

        logger.warn("Assuming germline copy number " + DEFAULT_GERMLINE_COPY_NUMBER + "" +
                " for all targets. This will be fixed in the future.");

        sampleReadCounts = Nd4j.create(sampleReadCountsArray2D);
        sampleMasks = Nd4j.create(sampleMasksArray2D);
        sampleLogReadCounts = Nd4j.create(sampleLogReadCountsArray2D);
        sampleStatisticalVariances = Nd4j.create(sampleStatisticalVariancesArray2D);
        sampleGermlineCopyNumber = Nd4j.create(sampleGermlineCopyNumberArray2D);

        /* initialize the model */
        model = new TargetCoverageModelNd4j(getNumTargets(), getTargetCoverageEMParams().getNumLatents());
    }

    /**
     * Maximum likelihood estimator of the mean read depth for given read counts and total multiplicative bias
     *
     * @param readCount read count vector
     * @param totalMultBias total multiplicative bias vector
     * @return read depth maximum likelihood estimate
     */
    public static double estimateMeanReadDepth(final INDArray readCount, final INDArray totalMultBias) {
        Asserts.notNull(readCount, "The read count vector can not be null.");
        Asserts.notNull(totalMultBias, "The multiplciative bias vector can not be null.");
        if (readCount.length() == 0) {
            throw new UserException.BadInput("The copy ratio vector can not be empty.");
        }
        if (totalMultBias.length() != readCount.length()) {
            throw new UserException.BadInput("The lengths of the read counts vector and the multiplicative bias vector " +
                    "must be equal.");
        }

        INDArray readCountVector = readCount.linearView();
        INDArray totalMultBiasVector = totalMultBias.linearView();

        final double num = readCountVector.sub(0.5).mul(totalMultBiasVector).sumNumber().doubleValue();
        final double denom = totalMultBias.mul(totalMultBias).sumNumber().doubleValue();
        return num/denom;
    }

}
