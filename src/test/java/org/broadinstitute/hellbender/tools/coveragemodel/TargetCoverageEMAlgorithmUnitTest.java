package org.broadinstitute.hellbender.tools.coveragemodel;

import htsjdk.samtools.util.Log;
import org.broadinstitute.hellbender.tools.coveragemodel.nd4j.TargetCoverageEMAlgorithmNd4j;
import org.broadinstitute.hellbender.tools.coveragemodel.nd4j.TargetCoverageEMWorkspaceNd4j;
import org.broadinstitute.hellbender.tools.exome.ReadCountCollection;
import org.broadinstitute.hellbender.tools.exome.ReadCountCollectionUtils;
import org.broadinstitute.hellbender.utils.LoggingUtils;
import org.broadinstitute.hellbender.utils.test.BaseTest;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.buffer.util.DataTypeUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public class TargetCoverageEMAlgorithmUnitTest extends BaseTest {
    private static final String TEST_SUB_DIR = publicTestDir + "org/broadinstitute/hellbender/tools/coveragemodel";
    private static final File TEST_RCC_FILE = new File(TEST_SUB_DIR, "synthetic_rcc.tsv");

    private ReadCountCollection testReadCounts;

    private static final TargetCoverageEMParams params = new TargetCoverageEMParams();
    private TargetCoverageEMWorkspace ws;
    private TargetCoverageEMAlgorithm algo;

    @BeforeSuite @Override
    public void setTestVerbosity(){
        LoggingUtils.setLoggingLevel(Log.LogLevel.DEBUG);
    }

    @BeforeClass
    void loadReadCounts() throws IOException {
        DataTypeUtil.setDTypeForContext(DataBuffer.Type.DOUBLE);
        testReadCounts = ReadCountCollectionUtils.parse(TEST_RCC_FILE);
        ws = new TargetCoverageEMWorkspaceNd4j(testReadCounts, params, 5);
        algo = new TargetCoverageEMAlgorithmNd4j(params, (TargetCoverageEMWorkspaceNd4j) ws);
    }

    @Test
    void basicTest() {

    }

}
