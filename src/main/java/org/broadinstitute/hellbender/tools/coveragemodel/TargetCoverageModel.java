package org.broadinstitute.hellbender.tools.coveragemodel;

import org.broadinstitute.hellbender.utils.hdf5.HDF5File;

/**
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public abstract class TargetCoverageModel<V, M> implements TargetCoverageModelCoreRoutines<V, M> {

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

}
