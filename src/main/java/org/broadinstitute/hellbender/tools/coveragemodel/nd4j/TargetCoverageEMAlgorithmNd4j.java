package org.broadinstitute.hellbender.tools.coveragemodel.nd4j;

import org.broadinstitute.hellbender.tools.coveragemodel.TargetCoverageEMAlgorithm;
import org.broadinstitute.hellbender.tools.coveragemodel.TargetCoverageEMParams;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public final class TargetCoverageEMAlgorithmNd4j extends TargetCoverageEMAlgorithm<INDArray, INDArray> {

    final TargetCoverageEMWorkspaceNd4j ws;

    public TargetCoverageEMAlgorithmNd4j(@Nonnull final TargetCoverageEMParams params,
                                         @Nonnull final TargetCoverageEMWorkspaceNd4j ws) {
        super(params);
        this.ws = ws;
        updateLatentPosteriorExpectations();
    }

    @Override
    public SubroutineSignal updateLatentPosteriorExpectations() {
        ws.updateLatentPosteriorExpectationsAll();
        return new SubroutineSignal(SubroutineStatus.SUCCESS, null);
    }

    @Override
    public SubroutineSignal updateMeanBias() {
        /* TODO */
        return null;
    }

    @Override
    public SubroutineSignal updateB() {
        /* TODO */
        return null;
    }

    @Override
    public SubroutineSignal updatePsi() {
        /* TODO */
        return null;
    }

    @Override
    public SubroutineSignal updateW() {
        /* TODO */
        return null;
    }

    @Override
    public double getLogLikelihood() {
        /* TODO */
        return 0;
    }

}
