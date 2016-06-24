package org.broadinstitute.hellbender.tools.coveragemodel.nd4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadinstitute.hellbender.tools.coveragemodel.TargetSpaceBlock;
import org.broadinstitute.hellbender.utils.Utils;
import org.broadinstitute.hellbender.utils.param.ParamUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public class TargetCoverageEMCacheBlockNd4j {

    private final Logger logger = LogManager.getLogger(TargetCoverageEMCacheBlockNd4j.class);

    /* $c_{st}$ */
    public final INDArray sampleCopyRatios;

    /* $m_{st}$ */
    public final INDArray sampleBiases;

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
        logger.debug("Allocating memory for containers (block " + targetBlock + ")");
        sampleCopyRatios = Nd4j.create(numSamples, targetBlock.getNumTargets());
        sampleBiases = Nd4j.create(numSamples, targetBlock.getNumTargets());
        sampleTotalVariances = Nd4j.create(numSamples, targetBlock.getNumTargets());
        sampleMaskedInverseTotalVariances = Nd4j.create(numSamples, targetBlock.getNumTargets());
        sampleSignalQuadraticPosteriors = Nd4j.create(numSamples, targetBlock.getNumTargets());
    }
}
