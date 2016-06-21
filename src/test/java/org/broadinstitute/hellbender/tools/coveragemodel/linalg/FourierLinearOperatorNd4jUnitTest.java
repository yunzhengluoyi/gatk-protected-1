package org.broadinstitute.hellbender.tools.coveragemodel.linalg;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.broadinstitute.hellbender.utils.test.BaseTest;
import org.junit.Assert;
import org.nd4j.linalg.factory.Nd4j;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link FourierLinearOperatorNd4j}
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */

public class FourierLinearOperatorNd4jUnitTest extends BaseTest {

    @DataProvider(name = "testData")
    public Object[][] getTestData() {
        return new Object[][] {
            {4,
                new double[] {1.0, 1.0, 1.0},
                new double[] {1.0, 2.0, 3.0, 4.0},
                new double[] {1.0, 2.0, 3.0, 4.0}},
            {4,
                new double[] {0.0, 1.0, 1.0},
                new double[] {1.0, 2.0, 3.0, 4.0},
                new double[] {-1.5, -0.5, 0.5, 1.5}},
            {4,
                new double[] {-1.0, 3.0, 4.0},
                new double[] {1.0, 2.0, 3.0, 4.0},
                new double[] {-7.5, -3.5, -1.5, 2.5}},
            {5,
                new double[] {1.0, 1.0, 1.0},
                new double[] {1.0, 2.0, 3.0, 4.0, 5.0},
                new double[] {1.0, 2.0, 3.0, 4.0, 5.0}},
            {5,
                new double[] {0.0, 1.0, 1.0},
                new double[] {1.0, 2.0, 3.0, 4.0, 5.0},
                new double[] {-2.0, -1.0, 0.0, 1.0, 2.0}},
            {5,
                new double[] {1.0, 2.0, 1.0},
                new double[] {1.0, 2.0, 3.0, 4.0, 5.0},
                new double[] {0, 3.81966011e-01, 3.0, 5.61803399e+00, 6.0}}
        };
    }

    /**
     * The main test routine
     * @param dim dimension of the operator
     * @param fourierFacts Fourier factors
     * @param x input data
     * @param y expected output data
     */
    @Test(dataProvider = "testData")
    public void performTest(final int dim, final double[] fourierFacts, final double[] x, final double[] y) {
        FourierLinearOperatorNd4j linOp = new FourierLinearOperatorNd4j(dim, fourierFacts);
        final double[] yCalc = linOp.operate(Nd4j.create(x)).data().asDouble();
        Assert.assertArrayEquals("", y, yCalc, 1e-6);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBadDimension_0() {
        /* negative dimensions not allowed */
        FourierLinearOperatorNd4j linop = new FourierLinearOperatorNd4j(-1, new double[5]);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBadDimension_1() {
        /* dimension >= 2 */
        FourierLinearOperatorNd4j linop = new FourierLinearOperatorNd4j(1, new double[5]);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBadDimension_2() {
        /* fourierFactors.length = floor(dimension/2) + 1 */
        FourierLinearOperatorNd4j linop = new FourierLinearOperatorNd4j(15, new double[3]);
    }

}