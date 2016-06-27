package org.broadinstitute.hellbender.tools.coveragemodel.nd4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadinstitute.hellbender.tools.coveragemodel.TargetSpaceBlock;
import org.broadinstitute.hellbender.utils.Utils;
import org.broadinstitute.hellbender.utils.param.ParamUtils;
import org.junit.Assert;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public class TargetCoverageEMCacheBlockNd4j {

    private final Logger logger = LogManager.getLogger(TargetCoverageEMCacheBlockNd4j.class);



    /* $n_{st}$ */
    public INDArray sampleReadCounts;

    /* $log(n_{st})$ */
    public INDArray sampleLogReadCounts;

    /* P_{st} */
    public INDArray sampleGermlineCopyNumber;

    /* M_{st} */
    public INDArray sampleMasks;

    /* $\Sigma_{st}$ */
    public INDArray sampleStatisticalVariances;

    /* \sum_s M_{st} */
    public INDArray sampleSummedMasks;




    /* $c_{st}$ */
    public final INDArray sampleCopyRatios;

    /* $m_{st}$ */
    public final INDArray sampleBiases;

    /* $m_{st} - m_t$ */
    public final INDArray sampleBiasDeviations;

    /* $\Psi_t + \Sigma_{st} */
    public final INDArray sampleTotalVariances;

    /* $M_{st}/(\Psi_t + \Sigma_{st})$ */
    public final INDArray sampleMaskedInverseTotalVariances;

    /* $B_{st}$ */
    public final INDArray sampleSignalQuadraticPosteriors;




    private final TargetSpaceBlock targetBlock;

    private final int numSamples;

    TargetCoverageEMCacheBlockNd4j(final TargetSpaceBlock targetBlock, final int numSamples) {
        this.numSamples = ParamUtils.isPositive(numSamples, "Number of samples must be positive.");
        this.targetBlock = Utils.nonNull(targetBlock, "Target space block identifier can not be null.");

        /* allocate memory */
        logger.debug("Allocating memory for cache containers (block " + targetBlock + ")");

        sampleReadCounts = Nd4j.create(numSamples, targetBlock.getNumTargets());
        sampleLogReadCounts = Nd4j.create(numSamples, targetBlock.getNumTargets());
        sampleGermlineCopyNumber = Nd4j.create(numSamples, targetBlock.getNumTargets());
        sampleMasks = Nd4j.create(numSamples, targetBlock.getNumTargets());
        sampleStatisticalVariances = Nd4j.create(numSamples, targetBlock.getNumTargets());
        sampleSummedMasks = Nd4j.create(1, targetBlock.getNumTargets());

        sampleCopyRatios = Nd4j.create(numSamples, targetBlock.getNumTargets());
        sampleBiases = Nd4j.create(numSamples, targetBlock.getNumTargets());
        sampleBiasDeviations = Nd4j.create(numSamples, targetBlock.getNumTargets());
        sampleTotalVariances = Nd4j.create(numSamples, targetBlock.getNumTargets());
        sampleMaskedInverseTotalVariances = Nd4j.create(numSamples, targetBlock.getNumTargets());
        sampleSignalQuadraticPosteriors = Nd4j.create(numSamples, targetBlock.getNumTargets());
    }

    /**
     * Get a copy of the target space block
     * @return
     */
    public TargetSpaceBlock getTargetSpaceBlock() {
        return new TargetSpaceBlock(targetBlock.getBegIndex(), targetBlock.getEndIndex());
    }

    public void setCopyRatiosToConstant(final double constCopyRatio) {
        sampleCopyRatios.assign(Nd4j.ones(numSamples, targetBlock.getNumTargets()).muli(constCopyRatio));
    }

    public INDArray getSampleReadDepthEstimatorNumerator(final INDArray totalMultBias) {
        return sampleReadCounts.sub(0.5).mul(totalMultBias).mul(sampleMasks).sum(1);
    }

    public INDArray getSampleReadDepthEstimatorNumeratorInitial() {
        return getSampleReadDepthEstimatorNumerator(sampleGermlineCopyNumber);
    }

    public INDArray getSampleReadDepthEstimatorDenominator(final INDArray totalMultBias) {
        return totalMultBias.mul(totalMultBias).mul(sampleMasks).sum(1);
    }

    public INDArray getSampleReadDepthEstimatorDenominatorInitial() {
        return getSampleReadDepthEstimatorDenominator(sampleGermlineCopyNumber);
    }

    public void updateCaches(final TargetCoverageModelBlockNd4J model) {
        Assert.assertEquals(targetBlock, model.getTargetSpaceBlock());
        /* $\Psi_{st}$ */
        sampleTotalVariances.assign(sampleStatisticalVariances.addColumnVector(model.getTargetUnexplainedVariance()));
        /* $M_{st} \Psi_{st}^{-1}$ */
        sampleMaskedInverseTotalVariances.assign(sampleMasks.div(sampleTotalVariances));
        /* $m_{st} - m_t$ */
        sampleBiasDeviations.assign(sampleBiases.subRowVector(model.getTargetMeanBias()));
    }

}
