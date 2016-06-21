package org.broadinstitute.hellbender.tools.coveragemodel;

import org.broadinstitute.hellbender.utils.test.BaseTest;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for {@link TargetCoverageEMWorkspace} and derivatives
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public class TargetCoverageEMWorkspaceUnitTest extends BaseTest {

    @Test
    public void testEstimateMeanReadDepth() {
        INDArray testReadCounts = Nd4j.create(new double[]{1, 2, 3, 4});
        INDArray testTotalMultBias = Nd4j.create(new double[]{1, 1, 1, 1});
        
        Assert.assertEquals(TargetCoverageEMWorkspaceNd4j.estimateMeanReadDepth(testReadCounts, testTotalMultBias),
                2.0, 1e-8);
    }
}
