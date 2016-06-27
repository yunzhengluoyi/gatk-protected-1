package org.broadinstitute.hellbender.tools.coveragemodel.interfaces;

/**
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 * */

public interface TargetCoverageEMAlgorithmCoreRoutines<V, M> {

    enum SubroutineStatus {
        SUCCESS, FAILURE
    }

    final class SubroutineSignal {

        /* SUCCESS or FAILURE */
        public final SubroutineStatus status;

        /* anything the subroutine wishes to communicate */
        public final Object adios;

        public SubroutineSignal(final SubroutineStatus status, final Object adios) {
            this.status = status;
            this.adios = adios;
        }
    }

    /**
     * Update E[z] and E[z z^T] for all samples using the current estimate of model parameters
     */
    SubroutineSignal updateLatentPosteriorExpectations();

    /**
     * M-step -- Update mean bias vector "m"
     */
    SubroutineSignal updateMeanBias();

    /**
     * M-step -- Update B (auxiliary)
     */
    SubroutineSignal updateB();

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
