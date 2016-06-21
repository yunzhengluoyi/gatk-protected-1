package org.broadinstitute.hellbender.tools.coveragemodel;

import org.broadinstitute.hellbender.tools.exome.ReadCountCollection;

/**
 * This abstract class provides the basic workspace structure for {@link TargetCoverageEMAlgorithm},
 * Explicit implementations may use local or distributed memory allocation and computation.
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */

public abstract class TargetCoverageEMWorkspace {

    protected final int numSamples, numTargets;
    protected final TargetCoverageEMParams params;

    /**
     * Basic constructor -- the constructer of classes that extend this class must take care of
     * parsing the reads and initializing the relevant containers.
     *
     * @param readCounts not {@code null} instance of {@link ReadCountCollection}
     * @param params not {@code null} instance of {@link TargetCoverageEMParams}
     */
    protected TargetCoverageEMWorkspace(final ReadCountCollection readCounts,
                                        final TargetCoverageEMParams params) {
        if (readCounts == null) {
            throw new IllegalArgumentException("The provided read count collection can not be null.");
        } else {
            numSamples = readCounts.columnNames().size();
            numTargets = readCounts.targets().size();
        }
        if (params == null) {
            throw new IllegalArgumentException("The provided target coverage EM params can not be null.");
        } else {
            this.params = params;
        }
    }

    public int getNumSamples() { return numSamples; }

    public int getNumTargets() { return numTargets; }

    public TargetCoverageEMParams getTargetCoverageEMParams() { return params; }

}
