package org.broadinstitute.hellbender.tools.coveragemodel.linalg;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.broadinstitute.hellbender.utils.param.ParamUtils;
import org.jtransforms.fft.DoubleFFT_1D;

/**
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public abstract class FourierLinearOperator<V> extends GeneralLinearOperator<V> {

    protected final int dimension;
    protected final double[] fourierFactors;
    protected final DoubleFFT_1D fftFactory;

    /**
     * Constructs the linear operator of a given {@code dimension} for given Fourier factors
     * Note: the length of {@code fourierFactors} must be equal to {@code floor(dimension/2) + 1}
     *
     * @param dimension dimension of the linear space on which the linear operator acts
     * @param fourierFactors Fourier factors of the linear operator
     * @throws IllegalArgumentException if {@code fourierFactors.length != dimension/2 + 1}
     */
    public FourierLinearOperator(final int dimension, final double[] fourierFactors) {
        this.dimension = ParamUtils.isPositive(dimension - 1, "The dimension of the linear operator " +
                "must be >= 2.") + 1;
        if (fourierFactors.length != dimension/2 + 1) {
            throw new IllegalArgumentException("The length of Fourier factors must be equal to " +
                    "floor(dimension/2) + 1");
        } else {
            this.fourierFactors = fourierFactors.clone();
        }
        fftFactory = new DoubleFFT_1D(dimension);
    }

    @Override
    public int getRowDimension() { return dimension; }

    @Override
    public int getColumnDimension() { return dimension; }

    @Override
    public boolean isTransposable() {
        return true;
    }

    @Override
    public V operateTranspose(final V x)
            throws DimensionMismatchException, UnsupportedOperationException {
        return operate(x);
    }
}
