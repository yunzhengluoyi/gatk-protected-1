package org.broadinstitute.hellbender.tools.coveragemodel.interfaces;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.broadinstitute.hellbender.exceptions.UserException;
import org.broadinstitute.hellbender.tools.coveragemodel.linalg.FourierLinearOperator;
import org.broadinstitute.hellbender.tools.coveragemodel.linalg.GeneralLinearOperator;

/**
 * Target coverage model core routines interface
 *
 * V is the vector type, M is the matrix type
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */

public interface TargetCoverageModelCoreRoutines<V, M> {

    /**************
     * operations *
     **************/

    /**
     * Calculates [W]^T [D] [W] where [D] is a TxT diagonal matrix
     * {@code diag} must be treated as immutable
     *
     * @param diag entries of the diagonal matrix
     * @return a matrix type
     * @throws UnsupportedOperationException
     */
    default M wtdw(final V diag) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Calculates [W]^T F([W]) where F is a Fourier real linear operator
     * @param fop the Fourier operator
     * @return a matrix type
     * @throws UnsupportedOperationException
     */
    default M wtfw(final FourierLinearOperator<V> fop) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Computes [W] v
     * {@code v} must be treated as immutable
     *
     * @param v a vector
     * @return
     * @throws UnsupportedOperationException
     */
    default V wv(final V v) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Computes [W]^T v
     * {@code v} must be treated as immutable
     *
     * @param v a vector
     * @return
     * @throws UnsupportedOperationException
     */
    default V wtv(final V v) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

}
