package org.broadinstitute.hellbender.tools.coveragemodel;

import org.broadinstitute.hellbender.tools.coveragemodel.nd4j.TargetCoverageEMWorkspaceNd4jUtils;
import org.broadinstitute.hellbender.utils.test.BaseTest;
import org.nd4j.linalg.factory.Nd4j;
import org.junit.Assert;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

/**
 * Unit test for {@link TargetCoverageEMWorkspace} and derivatives
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public class TargetCoverageEMWorkspaceNd4JUtilsUnitTest extends BaseTest {

    @Test
    public void testEstimateMeanReadDepth_0() {
        Assert.assertEquals(org.broadinstitute.hellbender.tools.coveragemodel.nd4j.TargetCoverageEMWorkspaceNd4jUtils.estimateMeanReadDepth(
                Nd4j.create(new double[]{1, 2, 3, 4}),
                Nd4j.create(new double[]{1, 1, 1, 1}),
                Nd4j.create(new double[]{1, 1, 1, 1})),
                2.0, 1e-8);
        Assert.assertEquals(TargetCoverageEMWorkspaceNd4jUtils.estimateMeanReadDepth(
                Nd4j.create(new double[]{1, 2, 3, 4}),
                Nd4j.create(new double[]{1, 1, 1, 1}),
                Nd4j.create(new double[]{1, 0, 1, 0})),
                1.5, 1e-8);
    }

    @Test
    public void testEstimateMeanReadDepth_1() {
        final int dataLength = 10000;
        double[] readCount = IntStream.range(0, dataLength).mapToDouble(n -> 1000.0).toArray();
        double[] multBias = IntStream.range(0, dataLength).mapToDouble(n -> 2.0).toArray();
        double[] mask = IntStream.range(0, dataLength).mapToDouble(n -> 1.0).toArray();
        double res = org.broadinstitute.hellbender.tools.coveragemodel.nd4j.TargetCoverageEMWorkspaceNd4jUtils.estimateMeanReadDepth(Nd4j.create(readCount),
                Nd4j.create(multBias), Nd4j.create(mask));
        Assert.assertEquals(res, 499.75, 1e-8);
    }

}
