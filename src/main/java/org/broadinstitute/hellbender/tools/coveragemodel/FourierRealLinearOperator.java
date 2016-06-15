package org.broadinstitute.hellbender.tools.coveragemodel;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealLinearOperator;
import org.apache.commons.math3.linear.RealVector;
import org.broadinstitute.hellbender.utils.param.ParamUtils;
import org.jtransforms.fft.DoubleFFT_1D;

/**
 * A a subclass of {@link RealLinearOperator} that defines the action of a real circulant
 * matrix operator $F(x,x') = F(x-x')$ by providing the DFT components of $F(x)$
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */

public final class FourierRealLinearOperator extends RealLinearOperator {

    private final int dimension;
    private final double[] fourierFactors;
    private final DoubleFFT_1D fftFactory;

    /**
     * Constructs the linear operator of a given {@code dimension} for given Fourier factors
     * Note: the length of {@code fourierFactors} must be equal to {@code floor(dimension/2) + 1}
     *
     * @param dimension dimension of the linear space on which the linear operator acts
     * @param fourierFactors Fourier factors of the linear operator
     * @throws IllegalArgumentException if {@code fourierFactors.length != dimension/2 + 1}
     */
    public FourierRealLinearOperator(final int dimension, final double[] fourierFactors) {
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

    /**
     * Implementation of the action of the Fourier linear operator on a given real vector
     * @param x the {@link RealVector} on which the linear operator is acted on
     * @return the transformed vector
     */
    @Override
    public RealVector operate(final RealVector x) throws DimensionMismatchException {
        if (x.getDimension() != dimension) {
            throw new DimensionMismatchException(x.getDimension(), dimension);
        }
        /* perform real forward FFT */
        double[] xDoubleArray = x.toArray();
        fftFactory.realForward(xDoubleArray);
        for (int k=1; k<dimension/2; k++) {
            xDoubleArray[2*k] *= fourierFactors[k];
            xDoubleArray[2*k+1] *= fourierFactors[k];
        }
        xDoubleArray[0] *= fourierFactors[0];
        xDoubleArray[1] *= fourierFactors[dimension/2];
        if (dimension % 2 == 1) {
            xDoubleArray[dimension-1] *= fourierFactors[(dimension-1)/2];
        }
        fftFactory.realInverse(xDoubleArray, true);
        return new ArrayRealVector(xDoubleArray);
    }

}
