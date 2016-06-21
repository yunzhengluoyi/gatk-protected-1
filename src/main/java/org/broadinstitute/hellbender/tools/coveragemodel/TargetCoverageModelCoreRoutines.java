package org.broadinstitute.hellbender.tools.coveragemodel;

import org.apache.commons.math3.exception.DimensionMismatchException;
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

    /************
     * acessors *
     ************/

    V getTargetMeanBias();

    V getTargetUnexplainedVariance();

    GeneralLinearOperator<V> getPrincipalLinearMap();

    /**************
     * operations *
     **************/

    /**
     * Calculates [W]^T [D] [W] where [D] is a TxT diagonal matrix
     * @param diag entries of the diagonal matrix
     * @return a matrix type
     * @throws UnsupportedOperationException
     * @throws DimensionMismatchException
     */
    default M wtdw(final V diag)
            throws UnsupportedOperationException, DimensionMismatchException {
        throw new UnsupportedOperationException();
    }

    /**
     * Calculates [W]^T F([W]) where F is a Fourier real linear operator
     * @param fop the Fourier operator
     * @return a matrix type
     * @throws UnsupportedOperationException
     * @throws DimensionMismatchException
     */
    default M wtfw(final FourierLinearOperator<V> fop)
            throws UnsupportedOperationException, DimensionMismatchException {
        throw new UnsupportedOperationException();
    }

    /**
     * Computes [W] v
     * @param v a vector
     * @return
     * @throws UnsupportedOperationException
     * @throws DimensionMismatchException
     */
    default V wv(final V v)
            throws UnsupportedOperationException, DimensionMismatchException {
        throw new UnsupportedOperationException();
    }

    /**
     * Computes [W]^T v
     * @param v a vector
     * @return
     * @throws UnsupportedOperationException
     * @throws DimensionMismatchException
     */
    default V wtv(final V v)
            throws UnsupportedOperationException, DimensionMismatchException {
        throw new UnsupportedOperationException();
    }


    /************
     * mutators *
     ************/

    void setTargetMeanBias(final V newTargetMeanBias);

    /**
     *
     * @param newTargetUnexplainedVariance
     * @throws UnsupportedOperationException
     * @throws DimensionMismatchException
     */
    default void setTargetUnexplainedVariance(final V newTargetUnexplainedVariance)
            throws UnsupportedOperationException, DimensionMismatchException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param targetIndex
     * @param newTargetPrincipalLinearMap
     * @throws UnsupportedOperationException
     */
    default void setPrincipalLinearMapPerTarget(final int targetIndex, final V newTargetPrincipalLinearMap)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param latentIndex
     * @param newLatentPrincipalLinearMap
     * @throws UnsupportedOperationException
     */
    default void setPrincipalLinearMapPerLatent(final int latentIndex, final V newLatentPrincipalLinearMap)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

}
