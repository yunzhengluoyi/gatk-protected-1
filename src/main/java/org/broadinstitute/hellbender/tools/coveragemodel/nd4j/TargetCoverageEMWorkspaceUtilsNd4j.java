package org.broadinstitute.hellbender.tools.coveragemodel.nd4j;

import org.broadinstitute.hellbender.exceptions.UserException;
import org.broadinstitute.hellbender.utils.Utils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.transforms.Log;
import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public class TargetCoverageEMWorkspaceUtilsNd4j {

    /**
     * Maximum likelihood estimator of the mean read depth for given read counts and total multiplicative bias
     *
     * @param readCount read count vector
     * @param totalMultBias total multiplicative bias vector
     * @return read depth maximum likelihood estimate
     */
    public static double estimateMeanReadDepth(final INDArray readCount, final INDArray totalMultBias,
                                               final INDArray mask) {
        Utils.nonNull(readCount, "The read count vector can not be null.");
        Utils.nonNull(totalMultBias, "The multiplciative bias vector can not be null.");
        Utils.nonNull(mask, "The mask vector can not be null.");
        if (readCount.length() == 0) {
            throw new UserException.BadInput("The copy ratio vector can not be empty.");
        }
        if (totalMultBias.length() != readCount.length() || totalMultBias.length() != mask.length()) {
            throw new UserException.BadInput("The lengths of the read count, multiplicative bias, " +
                    "and mask vectors must be equal.");
        }

        final double num = readCount.sub(0.5).muli(totalMultBias).muli(mask).sumNumber().doubleValue();
        final double denom = totalMultBias.mul(totalMultBias).muli(mask).sumNumber().doubleValue();
        return num/denom;
    }

}
