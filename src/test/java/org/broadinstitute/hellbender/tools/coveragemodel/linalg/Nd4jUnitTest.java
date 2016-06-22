package org.broadinstitute.hellbender.tools.coveragemodel.linalg;

import org.broadinstitute.hellbender.utils.test.BaseTest;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.buffer.util.DataTypeUtil;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

/**
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public class Nd4jUnitTest extends BaseTest {

    @Test
    public void testNd4jDup() {
        /* set the dType */
        DataTypeUtil.setDTypeForContext(DataBuffer.Type.DOUBLE);

        /* create NDArray from a double[][] */
        double data[][] = new double[50][50];
        IntStream.range(0, 50).forEach(s -> data[s] = IntStream.range(0, 50).mapToDouble(n -> 100.0).toArray());
        INDArray testNDArray = Nd4j.create(data);

        /* print the first row */
        System.out.println(testNDArray.getRow(0));

        /* set the dType again! */
        DataTypeUtil.setDTypeForContext(DataBuffer.Type.DOUBLE);

        /* print the first row */
        System.out.println(testNDArray.getRow(0));

        /* print the first row dup -- it should be off */
        System.out.println(testNDArray.getRow(0).dup());
    }
}
