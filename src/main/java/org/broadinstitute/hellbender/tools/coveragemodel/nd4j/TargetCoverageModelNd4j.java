package org.broadinstitute.hellbender.tools.coveragemodel.nd4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadinstitute.hellbender.exceptions.UserException;
import org.broadinstitute.hellbender.tools.coveragemodel.TargetCoverageModel;
import org.broadinstitute.hellbender.tools.coveragemodel.linalg.FourierLinearOperator;
import org.broadinstitute.hellbender.tools.coveragemodel.linalg.GeneralLinearOperator;
import org.broadinstitute.hellbender.utils.Utils;
import org.broadinstitute.hellbender.utils.hdf5.HDF5File;
import org.broadinstitute.hellbender.utils.param.ParamUtils;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.buffer.util.DataTypeUtil;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public final class TargetCoverageModelNd4j extends TargetCoverageModel<INDArray, INDArray> {

    private final Logger logger = LogManager.getLogger(TargetCoverageModelNd4j.class);

    private final INDArray targetMeanBias;
    private final INDArray targetUnexplainedVariance;
    private final INDArray principalLinearMap;

    public TargetCoverageModelNd4j(final int numTargets, final int numLatents) {
        super(numTargets, numLatents);

        /* create containers */
        logger.debug("Allocating memory for containers ...");
        targetMeanBias = Nd4j.zeros(1, numTargets);
        targetUnexplainedVariance = Nd4j.zeros(1, numTargets);
        principalLinearMap = Nd4j.zeros(numTargets, numLatents);

        /* initialize */
        initialize();
    }

//    public TargetCoverageModelNd4j(final HDF5File input) {
//        loadFromFile(Utils.nonNull(input, "The model HDF5 file can not be null."));
//    }

    @Override
    public void saveToFile(final HDF5File output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initialize() {
        /* nothing to do there at the moment -- consider removing */
    }

    /**
     * Note: returns a clone since ND4j objects are mutable
     * @return
     */
    @Override
    public INDArray getTargetMeanBias() {
        return targetMeanBias.dup();
    }

    /**
     * Note: returns a clone since ND4j objects are mutable
     * @return
     */
    @Override
    public INDArray getTargetUnexplainedVariance() {
        return targetUnexplainedVariance.dup();
    }

    @Override
    public GeneralLinearOperator<INDArray> getPrincipalLinearMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public INDArray wtdw(final INDArray diag) {
        /* TODO */
        return null;
    }

    @Override
    public INDArray wtfw(final FourierLinearOperator<INDArray> fop) {
        /* TODO */
        return null;
    }

    @Override
    public INDArray wv(final INDArray v) {
        /* TODO */
        return null;
    }

    @Override
    public INDArray wtv(final INDArray v) {
        /* TODO */
        return null;
    }

    @Override
    public void setTargetMeanBias(final INDArray newTargetMeanBias) {
        Utils.nonNull(newTargetMeanBias);
        if (newTargetMeanBias.length() != getNumTargets() || !newTargetMeanBias.isVector()) {
            throw new UserException("Either the provited INDArray is not a vector or has the wrong size.");
        }
        targetMeanBias.putRow(0, newTargetMeanBias.dup());
    }

    @Override
    public void setTargetUnexplainedVariance(final INDArray newTargetUnexplainedVariance) {
        Utils.nonNull(newTargetUnexplainedVariance);
        if (newTargetUnexplainedVariance.length() != getNumTargets() || !newTargetUnexplainedVariance.isVector()) {
            throw new UserException("Either the provited INDArray is not a vector or has the wrong size.");
        }
        targetUnexplainedVariance.putRow(0, newTargetUnexplainedVariance.dup());
    }

    @Override
    public void setPrincipalLinearMapPerTarget(final int targetIndex, final INDArray newTargetPrincipalLinearMap) {
        Utils.nonNull(newTargetPrincipalLinearMap);
        if (newTargetPrincipalLinearMap.length() != getNumLatents() || !newTargetPrincipalLinearMap.isVector()) {
            throw new UserException("Either the provited INDArray is not a vector or has the wrong size.");
        }
        assertTargetIndex(targetIndex);
        principalLinearMap.putRow(targetIndex, newTargetPrincipalLinearMap.dup());
    }

    @Override
    public void setPrincipalLinearMapPerLatent(final int latentIndex, final INDArray newLatentPrincipalLinearMap) {
        Utils.nonNull(newLatentPrincipalLinearMap);
        if (newLatentPrincipalLinearMap.length() != getNumTargets() || !newLatentPrincipalLinearMap.isVector()) {
            throw new UserException("Either the provited INDArray is not a vector or has the wrong size.");
        }
        assertLatentIndex(latentIndex);
        principalLinearMap.putColumn(latentIndex, newLatentPrincipalLinearMap.dup());
    }

    @Override
    public int getNumTargets() {
        return numTargets;
    }

    @Override
    public int getNumLatents() {
        return numLatents;
    }
}
