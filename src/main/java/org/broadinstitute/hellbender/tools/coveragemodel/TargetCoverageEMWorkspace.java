package org.broadinstitute.hellbender.tools.coveragemodel;

import org.broadinstitute.hellbender.tools.exome.ReadCountCollection;

/**
 * This abstract class provides the basic workspace structure for {@link TargetCoverageEMModeler},
 * Explicit implementations may use local or distributed memory allocation and computation.
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */

public abstract class TargetCoverageEMWorkspace {

    protected final TargetCoverageModel model;

    /**
     * Constructor
     * @param readCountCollection Non-null {@link ReadCountCollection} of the samples
     * @param model Non-null {@link TargetCoverageModel} to be learned
     */
    protected TargetCoverageEMWorkspace(final ReadCountCollection readCountCollection,
                                        final TargetCoverageModel model) {
        if (readCountCollection == null) {
            throw new IllegalArgumentException("The provided read count collection can not be null.");
        } else {
            parseReadCountCollection(readCountCollection);
        }
        if (model == null) {
            throw new IllegalArgumentException("The provided target coverage model can not be null.");
        } else {
            this.model = model;
        }
    }

    /**
     * Parse the {@link ReadCountCollection} of the samples and create relevant persistent caches
     * @param readCountCollection The {@link ReadCountCollection} of the samples
     */
    protected abstract void parseReadCountCollection(final ReadCountCollection readCountCollection);

    /**
     * Update G for all samples
     */
    public abstract void updateG();

    /**
     * Update E[z] for all samples
     */
    public abstract void updateZPosterior();

    /**
     * Update E[z z^T] for all samples
     */
    public abstract void updateZZPosterior();

    /**
     * M-step -- Update mean bias vector "m"
     */
    public abstract void updateMeanBias();

    /**
     * M-step -- Update B (auxiliary)
     */
    public abstract void updateB();

    /**
     * M-step -- Update Psi
     */
    public abstract void updatePsi();

    /**
     * M-step -- Update W
     */
    public abstract void updateW();

    /**
     * Calcaulte the log likelihood
     * @return log likelihood
     */
    public abstract double getLogLikelihood();

}
