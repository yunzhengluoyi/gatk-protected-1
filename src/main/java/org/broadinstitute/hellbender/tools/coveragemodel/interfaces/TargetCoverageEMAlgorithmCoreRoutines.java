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

        SubroutineSignal(final SubroutineStatus status, final Object adios) {
            this.status = status;
            this.adios = adios;
        }
    }

    /**
     * Update G for all samples
     */
    SubroutineSignal updateG();

    /**
     * Update E[z] for all samples
     */
    SubroutineSignal updateZPosterior();

    /**
     * Update E[z z^T] for all samples
     */
    SubroutineSignal updateZZPosterior();

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
