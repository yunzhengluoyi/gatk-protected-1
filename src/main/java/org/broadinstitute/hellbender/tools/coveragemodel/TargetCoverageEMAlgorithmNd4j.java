package org.broadinstitute.hellbender.tools.coveragemodel;

import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.buffer.util.DataTypeUtil;

/**
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public final class TargetCoverageEMAlgorithmNd4j extends TargetCoverageEMAlgorithm {

    public TargetCoverageEMAlgorithmNd4j(final TargetCoverageEMParams params,
                                         final TargetCoverageEMWorkspaceNd4j ws,
                                         final DataBuffer.Type dType) {
        super(params, ws);
        DataTypeUtil.setDTypeForContext(dType);
    }

    @Override
    public SubroutineSignal updateG() {
        /* TODO */
        return null;
    }

    @Override
    public SubroutineSignal updateZPosterior() {
        /* TODO */
        return null;
    }

    @Override
    public SubroutineSignal updateZZPosterior() {
        /* TODO */
        return null;
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
