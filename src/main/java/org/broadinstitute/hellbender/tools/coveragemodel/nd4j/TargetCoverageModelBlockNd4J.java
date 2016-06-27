package org.broadinstitute.hellbender.tools.coveragemodel.nd4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadinstitute.hellbender.exceptions.UserException;
import org.broadinstitute.hellbender.tools.coveragemodel.TargetCoverageModelBlock;
import org.broadinstitute.hellbender.tools.coveragemodel.TargetSpaceBlock;
import org.broadinstitute.hellbender.tools.coveragemodel.linalg.FourierLinearOperator;
import org.broadinstitute.hellbender.utils.Utils;
import org.junit.internal.ArrayComparisonFailure;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.junit.Assert;

import javax.annotation.Nonnull;

/**
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public final class TargetCoverageModelBlockNd4J extends TargetCoverageModelBlock<INDArray, INDArray> {

    private final Logger logger = LogManager.getLogger(TargetCoverageModelBlockNd4J.class);

    private final INDArray targetMeanBias;
    private final INDArray targetUnexplainedVariance;
    private final INDArray principalLinearMap;


    public TargetCoverageModelBlockNd4J(final TargetSpaceBlock targetBlock, final int numLatents) {
        super(targetBlock, numLatents);

        /* create containers */
        logger.debug("Allocating memory for containers (block " + targetBlock + ")");
        targetMeanBias = Nd4j.zeros(1, targetBlock.getNumTargets());
        targetUnexplainedVariance = Nd4j.zeros(1, targetBlock.getNumTargets());
        principalLinearMap = Nd4j.zeros(targetBlock.getNumTargets(), numLatents);
    }

    /**
     * Note: returns a clone since ND4j objects are mutable
     * @return
     */
    @Override
    public INDArray getTargetMeanBias() {
        return targetMeanBias.dup();
    }

    /**
     * Note: returns a clone since ND4j objects are mutable
     * @return
     */
    @Override
    public INDArray getTargetUnexplainedVariance() {
        return targetUnexplainedVariance.dup();
    }

    @Override
    public INDArray getPrincipalLinearMap() {
        return principalLinearMap.dup();
    }

    @Override
    public INDArray wtdw(@Nonnull final INDArray diag) {
        Assert.assertEquals(diag.length(), getTargetSpaceBlock().getNumTargets());
        return principalLinearMap.transpose().mmul(principalLinearMap.mulColumnVector(
                diag.reshape(getTargetSpaceBlock().getNumTargets(), 1)));
    }

    @Override
    public INDArray wtfw(@Nonnull final FourierLinearOperator<INDArray> fop) {
        throw new UnsupportedOperationException();
    }

    @Override
    public INDArray wv(@Nonnull final INDArray v) {
        Assert.assertEquals(v.length(), numLatents);
        return principalLinearMap.mmul(v.reshape(numLatents, 1));
    }

    @Override
    public INDArray wtv(final INDArray v) {
        Assert.assertEquals(v.length(), getTargetSpaceBlock().getNumTargets());
        return principalLinearMap.transpose().mmul(v.reshape(getTargetSpaceBlock().getNumTargets(), 1));
    }

    @Override
    public void setTargetMeanBias(final INDArray newTargetMeanBias) {
        Utils.nonNull(newTargetMeanBias);
        if (newTargetMeanBias.length() != getTargetSpaceBlock().getNumTargets() || !newTargetMeanBias.isVector()) {
            throw new UserException("Either the provited INDArray is not a vector or has the wrong size.");
        }
        targetMeanBias.putRow(0, newTargetMeanBias.dup());
    }

    @Override
    public void setTargetUnexplainedVariance(final INDArray newTargetUnexplainedVariance) {
        Utils.nonNull(newTargetUnexplainedVariance);
        if (newTargetUnexplainedVariance.length() != getTargetSpaceBlock().getNumTargets() || !newTargetUnexplainedVariance.isVector()) {
            throw new UserException("Either the provited INDArray is not a vector or has the wrong size.");
        }
        targetUnexplainedVariance.putRow(0, newTargetUnexplainedVariance.dup());
    }

    @Override
    public void setPrincipalLinearMapPerTarget(final int targetIndex, final INDArray newTargetPrincipalLinearMap) {
        Utils.nonNull(newTargetPrincipalLinearMap);
        if (newTargetPrincipalLinearMap.length() != getNumLatents() || !newTargetPrincipalLinearMap.isVector()) {
            throw new UserException("Either the provited INDArray is not a vector or has the wrong size.");
        }
        assertTargetIndex(targetIndex);
        principalLinearMap.putRow(targetIndex - targetBlock.getBegIndex(), newTargetPrincipalLinearMap.dup());
    }

    @Override
    public void setPrincipalLinearMapPerLatent(final int latentIndex, final INDArray newLatentPrincipalLinearMap) {
        Utils.nonNull(newLatentPrincipalLinearMap);
        if (newLatentPrincipalLinearMap.length() != getTargetSpaceBlock().getNumTargets() || !newLatentPrincipalLinearMap.isVector()) {
            throw new UserException("Either the provited INDArray is not a vector or has the wrong size.");
        }
        assertLatentIndex(latentIndex);
        principalLinearMap.putColumn(latentIndex, newLatentPrincipalLinearMap.dup());
    }

    @Override
    public void setPrincipalLinearMap(final INDArray newPrincipalLinearMap) {
        try {
            Assert.assertArrayEquals(principalLinearMap.shape(), newPrincipalLinearMap.shape());
        } catch (ArrayComparisonFailure e) {
            throw new UserException("The shape of the provided principal linear map is not compatible with the model.");
        }
        principalLinearMap.assign(newPrincipalLinearMap);
    }

    @Override
    public int getNumLatents() {
        return numLatents;
    }
}
