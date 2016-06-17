package org.broadinstitute.hellbender.tools.coveragemodel;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.RealLinearOperator;
import org.apache.commons.math3.linear.RealVector;

/**
 * Preconditioner for iterative solution of the E step equation for W in {@link TargetCoverageEMModeler}.
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */

public final class WMaximizationPreconditioner extends RealLinearOperator {

    public WMaximizationPreconditioner() {
        // TODO
    }

    // TODO
    @Override
    public int getRowDimension() { return 0; }

    // TODO
    @Override
    public int getColumnDimension() { return 0; }

    // TODO
    /**
     * Implementation of the action of the Fourier linear operator on a given real vector
     * @param x the {@link RealVector} on which the linear operator is acted on
     * @return the transformed vector
     */
    @Override
    public RealVector operate(final RealVector x) throws DimensionMismatchException {
        return x;
    }

}
