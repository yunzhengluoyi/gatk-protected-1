package org.broadinstitute.hellbender.tools.coveragemodel;

import org.broadinstitute.hellbender.tools.exome.ReadCountCollection;

/**
 * This class implements a local memory version of {@link TargetCoverageEMWorkspace}
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */

public final class TargetCoverageEMWorkspaceLocal extends TargetCoverageEMWorkspace {

    /**
     * Constructor
     * @param readCountCollection Non-null {@link ReadCountCollection} of the samples
     * @param model Non-null {@link TargetCoverageModel} to be learned
     */
    public TargetCoverageEMWorkspaceLocal(final ReadCountCollection readCountCollection,
                                          final TargetCoverageModel model) {
        super(readCountCollection, model);
        /* TODO */
    }

    /**
     * Parse the {@link ReadCountCollection} of the samples and create relevant persistent caches
     * @param readCountCollection The {@link ReadCountCollection} of the samples
     */
    @Override
    protected void parseReadCountCollection(final ReadCountCollection readCountCollection) {
        /* TODO */
    }

    /**
     * Update G for all samples
     */
    @Override
    public void updateG() {
        /* TODO */
    }

    /**
     * Update E[z] for all samples
     */
    @Override
    public void updateZPosterior() {
        /* TODO */
    }

    /**
     * Update E[z z^T] for all samples
     */
    @Override
    public void updateZZPosterior() {
        /* TODO */
    }

    /**
     * M-step -- Update mean bias vector "m"
     */
    @Override
    public void updateMeanBias() {
        /* TODO */
    }

    /**
     * M-step -- Update B (auxiliary)
     */
    @Override
    public void updateB() {
        /* TODO */
    }

    /**
     * M-step -- Update Psi
     */
    @Override
    public void updatePsi() {
        /* TODO */
    }

    /**
     * M-step -- Update W
     */
    @Override
    public void updateW() {
        /* TODO */
    }

    /**
     * Calcaulte the log likelihood
     * @return log likelihood
     */
    @Override
    public double getLogLikelihood() {
        /* TODO */
        return 0;
    }

}
