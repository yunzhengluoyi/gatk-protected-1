package org.broadinstitute.hellbender.tools.coveragemodel;

import org.broadinstitute.hellbender.tools.coveragemodel.nd4j.TargetCoverageModelBlockNd4J;
import org.broadinstitute.hellbender.utils.test.BaseTest;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.testng.annotations.Test;
import org.junit.*;

/**
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public class TargetCoverageModelBlockNd4jUnitTest extends BaseTest {

    @Test
    public void testwtdw() {
        final int N_TARGET = 1000;
        final int N_LATENT = 10;
        TargetCoverageModelBlockNd4J model = new TargetCoverageModelBlockNd4J(new TargetSpaceBlock(0, N_TARGET), N_LATENT);
        INDArray w = Nd4j.linspace(0, N_TARGET*N_LATENT-1, N_TARGET*N_LATENT).reshape(N_TARGET, N_LATENT);
        model.setPrincipalLinearMap(w);
        INDArray d = Nd4j.linspace(0, N_TARGET-1, N_TARGET);
        INDArray dMat = Nd4j.diag(d);
        INDArray expected = w.transpose().mmul(dMat).mmul(w);
        INDArray result = model.wtdw(d);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testwv() {
        /* TODO */
    }

    @Test
    public void testwtv() {
        /* TODO */
    }

}
