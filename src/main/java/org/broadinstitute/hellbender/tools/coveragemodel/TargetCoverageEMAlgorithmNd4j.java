package org.broadinstitute.hellbender.tools.coveragemodel;

/**
 * Created by mehrtash on 6/21/16.
 */
public final class TargetCoverageEMAlgorithmNd4j extends TargetCoverageEMAlgorithm {

    public TargetCoverageEMAlgorithmNd4j(final TargetCoverageEMParams params,
                                         final TargetCoverageEMWorkspaceNd4j ws) {
        super(params, ws);
    }

    @Override
    public void updateG() {
        /* TODO */
    }

    @Override
    public void updateZPosterior() {
        /* TODO */
    }

    @Override
    public void updateZZPosterior() {
        /* TODO */
    }

    @Override
    public void updateMeanBias() {
        /* TODO */
    }

    @Override
    public void updateB() {
        /* TODO */
    }

    @Override
    public void updatePsi() {
        /* TODO */
    }

    @Override
    public void updateW() {
        /* TODO */
    }

    @Override
    public double getLogLikelihood() {
        /* TODO */
        return 0;
    }

}
