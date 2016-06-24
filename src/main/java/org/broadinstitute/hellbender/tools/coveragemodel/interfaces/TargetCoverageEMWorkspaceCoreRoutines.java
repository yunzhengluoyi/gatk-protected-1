package org.broadinstitute.hellbender.tools.coveragemodel.interfaces;

/**
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public interface TargetCoverageEMWorkspaceCoreRoutines<V, M> {

    /**
     * Estimate mean read depth of a sample
     *
     * Note: {@code totalMultBias} is whatever accompanies $d_s$ in the poisson parameter, i.e.
     * it is $P_{st} x c_{st} x e^{b_{st}}$
     *
     * @param readCounts
     * @param totalMultBias
     * @param mask
     * @return
     */
    double estimateMeanReadDepth(final V readCounts, final V totalMultBias, final V mask);

//    /**
//     * Get a clone of a sample copy ratio estimate
//     * @param sampleIndex sample index
//     * @return a clone or immutable copy of the sample copy ratio estimate
//     */
//    V getSampleCopyRatio(final int sampleIndex);
//
//    /**
//     * Note: must clone the input
//     *
//     * @param sampleIndex
//     * @param newCopyRatio
//     */
//    void setSampleCopyRatio(final int sampleIndex, final V newCopyRatio);

}
