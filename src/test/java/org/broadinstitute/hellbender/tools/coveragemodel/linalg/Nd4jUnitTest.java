package org.broadinstitute.hellbender.tools.coveragemodel.linalg;

import org.broadinstitute.hellbender.utils.test.BaseTest;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.buffer.util.DataTypeUtil;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

/**
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public class Nd4jUnitTest extends BaseTest {

    @Test
    public void testIndexing() {
        INDArray arr = Nd4j.linspace(0, 8, 9).reshape(3, 3);
        System.out.println(arr);
        System.out.println(arr.get(
                NDArrayIndex.interval(1, 2),
                NDArrayIndex.interval(0, 3))
        );
    }

}
