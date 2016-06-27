package org.broadinstitute.hellbender.tools.coveragemodel;

import org.broadinstitute.hellbender.tools.coveragemodel.interfaces.TargetCoverageModelCoreRoutines;
import org.broadinstitute.hellbender.tools.coveragemodel.linalg.GeneralLinearOperator;
import org.broadinstitute.hellbender.utils.Utils;
import org.broadinstitute.hellbender.utils.hdf5.HDF5File;
import org.broadinstitute.hellbender.utils.param.ParamUtils;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public abstract class TargetCoverageModelBlock<V, M> implements TargetCoverageModelCoreRoutines<V, M> {

    protected final int numLatents;
    protected final TargetSpaceBlock targetBlock;

    protected TargetCoverageModelBlock(final TargetSpaceBlock targetBlock, final int numLatents) {
        this.targetBlock = Utils.nonNull(targetBlock, "Target space block can not be null.");
        this.numLatents = ParamUtils.isPositive(numLatents, "Number of latent variables must be " +
                "positive.");
    }

    /**
     * Get a copy of the target space block
     * @return
     */
    public TargetSpaceBlock getTargetSpaceBlock() {
        return new TargetSpaceBlock(targetBlock.getBegIndex(), targetBlock.getEndIndex());
    }
    /**
     * Get the dimension of the latent space
     * @return dimension of the latent space
     */
    public abstract int getNumLatents();

    protected void assertTargetIndex(final int targetIndex) {
        ParamUtils.inRange(targetIndex, targetBlock.getBegIndex(), targetBlock.getEndIndex() - 1, "Target index out of range");
    }

    protected void assertLatentIndex(final int latentIndex) {
        ParamUtils.inRange(latentIndex, 0, numLatents - 1, "Latent index out of range");
    }

    /************
     * acessors *
     ************/

    /**
     * Get a clone of target mean bias
     * @return a clone of target mean bias
     */
    public abstract V getTargetMeanBias();

    /**
     * Get a clone of the unexplained variance
     * @return a clone of the unexplained variance
     */
    public abstract V getTargetUnexplainedVariance();

    /**
     * Get an immutable copy or clone of the principal linear map
     * @return an immutable copy or clone of the principal linear map
     */
    public abstract M getPrincipalLinearMap();

    /************
     * mutators *
     ************/

    /**
     * Note: must clone the input
     *
     * @param newTargetMeanBias
     * @throws UnsupportedOperationException
     */
    public abstract void setTargetMeanBias(final V newTargetMeanBias) throws UnsupportedOperationException;

    /**
     * Note: must clone the input
     *
     * @param newTargetUnexplainedVariance
     * @throws UnsupportedOperationException
     */
    public void setTargetUnexplainedVariance(final V newTargetUnexplainedVariance) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Note: must clone the input
     *
     * @param targetIndex
     * @param newTargetPrincipalLinearMap
     * @throws UnsupportedOperationException
     */
    public void setPrincipalLinearMapPerTarget(final int targetIndex, final V newTargetPrincipalLinearMap)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Note: must clone the input
     *
     * @param latentIndex
     * @param newLatentPrincipalLinearMap
     * @throws UnsupportedOperationException
     */
    public void setPrincipalLinearMapPerLatent(final int latentIndex, final V newLatentPrincipalLinearMap)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Note: must clone the input
     *
     * @param newPrincipalLinearMap
     * @throws UnsupportedOperationException
     */
    public void setPrincipalLinearMap(final M newPrincipalLinearMap)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
