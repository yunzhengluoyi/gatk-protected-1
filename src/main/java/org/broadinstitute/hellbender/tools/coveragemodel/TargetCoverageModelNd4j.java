package org.broadinstitute.hellbender.tools.coveragemodel;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadinstitute.hellbender.exceptions.UserException;
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

    private final int numTargets, numLatents;

    private final INDArray targetMeanBias;
    private final INDArray targetUnexplainedVariance;
    private final INDArray principalLinearMap;

    public TargetCoverageModelNd4j(final int numTargets, final int numLatents) {
        DataTypeUtil.setDTypeForContext(DataBuffer.Type.DOUBLE);

        this.numTargets = ParamUtils.isPositive(numTargets, "Number of targets must be positive.");
        this.numLatents = ParamUtils.inRange(numLatents, 1, numTargets, "Number of latent variables must be " +
                ">= 1 and <= number of targets.");

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
        /* nothing to do there */
    }

    /**
     * Returns a clone since ND4j objects are mutable
     * @return
     */
    @Override
    public INDArray getTargetMeanBias() {
        return targetMeanBias.dup();
    }

    /**
     * Returns a clone since ND4j objects are mutable
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
        ParamUtils.inRange(targetIndex, 0, getNumTargets() - 1, "Target index out of range.");
        principalLinearMap.putRow(targetIndex, newTargetPrincipalLinearMap.dup());
    }

    @Override
    public void setPrincipalLinearMapPerLatent(final int latentIndex, final INDArray newLatentPrincipalLinearMap) {
        Utils.nonNull(newLatentPrincipalLinearMap);
        if (newLatentPrincipalLinearMap.length() != getNumTargets() || !newLatentPrincipalLinearMap.isVector()) {
            throw new UserException("Either the provited INDArray is not a vector or has the wrong size.");
        }
        ParamUtils.inRange(latentIndex, 0, getNumLatents() - 1, "Latent index out of range.");
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
