package org.broadinstitute.hellbender.tools.coveragemodel.linalg;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.RealLinearOperator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.stream.IntStream;

/**
 * A a subclass of {@link RealLinearOperator} that defines the action of a real circulant
 * matrix operator $F(x,x') = F(x-x')$ by providing the DFT components of $F(x)$
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */

public final class FourierLinearOperatorNd4j extends FourierLinearOperator<INDArray> {

    public FourierLinearOperatorNd4j(final int dimension, final double[] fourierFactors) {
        super(dimension, fourierFactors);
    }

    /**
     * Implementation of the action of the Fourier linear operator on a given real vector
     * @param x as an {@link INDArray} on which the linear operator is acted on
     * @return the transformed vector
     */
    @Override
    public INDArray operate(final INDArray x) throws DimensionMismatchException {
        if (x.length() != dimension) {
            throw new DimensionMismatchException(x.length(), dimension);
        }
        /* perform real forward FFT */
        double[] xDoubleArray = x.data().asDouble();
        fftFactory.realForward(xDoubleArray);
        /* apply filter */
        IntStream.range(1, dimension/2).forEach(k -> xDoubleArray[2*k] *= fourierFactors[k]);
        IntStream.range(1, dimension/2).forEach(k -> xDoubleArray[2*k+1] *= fourierFactors[k]);
        xDoubleArray[0] *= fourierFactors[0];
        xDoubleArray[1] *= fourierFactors[dimension/2];
        if (dimension % 2 == 1) {
            xDoubleArray[dimension-1] *= fourierFactors[(dimension-1)/2];
        }
        /* transform to real space */
        fftFactory.realInverse(xDoubleArray, true);
        return Nd4j.create(xDoubleArray);
    }

}
