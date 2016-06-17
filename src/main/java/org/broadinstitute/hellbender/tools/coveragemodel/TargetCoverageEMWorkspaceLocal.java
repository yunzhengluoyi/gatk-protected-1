package org.broadinstitute.hellbender.tools.coveragemodel;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.broadinstitute.hellbender.tools.exome.ReadCountCollection;

import java.util.List;

/**
 * This class implements a local memory version of {@link TargetCoverageEMWorkspace}
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */

public final class TargetCoverageEMWorkspaceLocal extends TargetCoverageEMWorkspace {

    /* baseline ploidy; the ploidy of each sample is measured w.r.t. this value */
    private static final int BASELINE_PLOIDY = 2;

    /* baseline germline copy ratio; the germline copy ratio of each sample is measured w.r.t. this value */
    private static final int BASELINE_GERMLINE_COPY_RATIO = 1;

    /**
     * persistent members
     */

    /* $n_{st}$ */
    private final RealMatrix sampleLogReadCounts;

    /* $\log(P_{st})$ */
    private final RealMatrix sampleLogPloidies;

    /* M_{st} */
    private final RealMatrix sampleMasks;

    /* $\Sigma$ */
    private final RealMatrix sampleStatisticalVariances;

    /* $d_s$ */
    private final RealMatrix sampleMeanReadDepths;

    /**
     * mutable members
     */

    /* m_{st} */
    private final RealMatrix sampleBiases;

    /* $\Psi_t + \Sigma_{st} */
    private final RealMatrix sampleTotalVariances;

    /* M_{st}/(\Psi_t + \Sigma_{st}) */
    private final RealMatrix sampleMaskedInverseTotalVariances;

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
     * Estimate the mean read depth of each sample
     */
    public void estimateMeanReadDepths() {
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
