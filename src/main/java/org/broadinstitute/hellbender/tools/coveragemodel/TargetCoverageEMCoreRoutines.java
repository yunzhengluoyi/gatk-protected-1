package org.broadinstitute.hellbender.tools.coveragemodel;

/**
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 * */

public interface TargetCoverageEMCoreRoutines {

    enum SubroutineStatus {
        SUCCESS, FAILURE
    }

    final class SubroutineSignal {
        public final SubroutineStatus status;
        public final String message;

        SubroutineSignal(final SubroutineStatus status, final String message) {
            this.status = status;
            this.message = message;
        }
    }

    /**
     * Update G for all samples
     */
    void updateG();

    /**
     * Update E[z] for all samples
     */
    void updateZPosterior();

    /**
     * Update E[z z^T] for all samples
     */
    void updateZZPosterior();

    /**
     * M-step -- Update mean bias vector "m"
     */
    void updateMeanBias();

    /**
     * M-step -- Update B (auxiliary)
     */
    void updateB();

    /**
     * M-step -- Update Psi
     */
    SubroutineSignal updatePsi();

    /**
     * M-step -- Update W
     */
    SubroutineSignal updateW();

    /**
     * Calcaulte the log likelihood
     * @return log likelihood
     */
    double getLogLikelihood();
}
