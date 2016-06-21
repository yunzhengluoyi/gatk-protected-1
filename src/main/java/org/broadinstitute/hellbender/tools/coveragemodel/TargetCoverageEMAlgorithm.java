package org.broadinstitute.hellbender.tools.coveragemodel;

import com.google.common.annotations.VisibleForTesting;
import org.broadinstitute.hellbender.utils.Utils;

/**
 * Implementation of the maximum likelihood estimator of {@link TargetCoverageModel} parameters
 * via the EM algorithm (see CNV-methods.pdf for technical details).
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */

public abstract class TargetCoverageEMAlgorithm implements TargetCoverageEMCoreRoutines {

    protected final TargetCoverageEMParams params;
    protected final TargetCoverageEMWorkspace ws;

    public enum EMAlgorithmStatus {
        TBD(false, "Status is not determined yet."),
        SUCCESS_LIKELIHOOD(true, "Success -- converged in likelihood change tolerance."),
        SUCCESS_PARAMS(true, "Success -- converged in parameters change tolerance."),
        FAILURE_MAX_ITERS_REACHED(false, "Failure -- maximum iterations reached."),
        FAILURE_PSI_TOL(false, "Failure -- M-step iterations for Psi not converged."),
        FAILURE_W_TOL(false, "Failure -- M-step iterations for W not converged.");

        final boolean success;
        final String message;

        EMAlgorithmStatus(final boolean success, final String message) {
            this.success = success;
            this.message = message;
        }
    }

    public TargetCoverageEMAlgorithm(final TargetCoverageEMParams params,
                                     final TargetCoverageEMWorkspace ws) {
        this.params = Utils.nonNull(params, "Target coverage EM algorithm parameters can not be null.");
        this.ws = Utils.nonNull(ws, "Target covarge EM workspace can not be null.");
    }

    @VisibleForTesting
    public void performEStep() {

    }

    @VisibleForTesting
    public void performMStep() {

    }

}
