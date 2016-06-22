package org.broadinstitute.hellbender.tools.coveragemodel;

import org.broadinstitute.hellbender.tools.coveragemodel.interfaces.TargetCoverageModelCoreRoutines;
import org.broadinstitute.hellbender.tools.coveragemodel.linalg.GeneralLinearOperator;
import org.broadinstitute.hellbender.utils.hdf5.HDF5File;
import org.broadinstitute.hellbender.utils.param.ParamUtils;

/**
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public abstract class TargetCoverageModel<V, M> implements TargetCoverageModelCoreRoutines<V, M> {

    protected final int numTargets, numLatents;

    protected TargetCoverageModel(final int numTargets, final int numLatents) {
        this.numTargets = ParamUtils.isPositive(numTargets, "Number of targets must be positive.");
        this.numLatents = ParamUtils.inRange(numLatents, 1, numTargets, "Number of latent variables must be " +
                ">= 1 and <= number of targets.");
    }

    /**
     * Get the dimension of the target space
     * @return dimension of target space
     */
    public abstract int getNumTargets();

    /**
     * Get the dimension of the latent space
     * @return dimension of the latent space
     */
    public abstract int getNumLatents();

    /**
     * Save model to file
     * @param output output HDF5 file
     */
    public abstract void saveToFile(final HDF5File output);

    /**
     * Initialize model parameters
     */
    public abstract void initialize();

    protected void assertTargetIndex(final int targetIndex) {
        ParamUtils.inRange(targetIndex, 0, numTargets - 1, "Target index out of range");
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
    public abstract GeneralLinearOperator<V> getPrincipalLinearMap();

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
}
