package org.broadinstitute.hellbender.tools.coveragemodel;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.broadinstitute.hellbender.tools.coveragemodel.linalg.FourierLinearOperator;
import org.broadinstitute.hellbender.tools.coveragemodel.linalg.GeneralLinearOperator;
import org.broadinstitute.hellbender.utils.Utils;
import org.broadinstitute.hellbender.utils.hdf5.HDF5File;
import org.broadinstitute.hellbender.utils.param.ParamUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public final class TargetCoverageModelNd4j extends TargetCoverageModel<INDArray, INDArray> {

    private final int numTargets, numLatents;

    private final INDArray targetMeanBias;
    private final INDArray targetUnexplainedVariance;
    private final INDArray principalLinearMap;

    public TargetCoverageModelNd4j(final int numTargets, final int numLatents) {
        this.numTargets = ParamUtils.isPositive(numTargets, "Number of targets must be positive.");
        this.numLatents = ParamUtils.inRange(numLatents, 1, numTargets, "Number of latent variables must be " +
                ">= 1 and <= number of targets.");

        /* create containers */
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
        /* TODO */
    }

    @Override
    public INDArray getTargetMeanBias() {
        /* TODO */
        return null;
    }

    @Override
    public INDArray getTargetUnexplainedVariance() {
        /* TODO */
        return null;
    }

    @Override
    public GeneralLinearOperator<INDArray> getPrincipalLinearMap() {
        /* TODO */
        throw new UnsupportedOperationException();
    }

    @Override
    public INDArray wtdw(final INDArray diag)
            throws UnsupportedOperationException, DimensionMismatchException {
        /* TODO */
        return null;
    }

    @Override
    public INDArray wtfw(final FourierLinearOperator<INDArray> fop)
            throws UnsupportedOperationException, DimensionMismatchException {
        /* TODO */
        return null;
    }

    @Override
    public INDArray wv(final INDArray v)
            throws UnsupportedOperationException, DimensionMismatchException {
        /* TODO */
        return null;
    }

    @Override
    public INDArray wtv(final INDArray v)
            throws UnsupportedOperationException, DimensionMismatchException {
        /* TODO */
        return null;
    }

    @Override
    public void setTargetMeanBias(final INDArray newTargetMeanBias) {
        /* TODO */
    }

    @Override
    public void setTargetUnexplainedVariance(final INDArray newTargetUnexplainedVariance)
            throws UnsupportedOperationException, DimensionMismatchException {
        /* TODO */
    }

    @Override
    public void setPrincipalLinearMapPerTarget(final int targetIndex, final INDArray newTargetPrincipalLinearMap)
            throws UnsupportedOperationException {
        /* TODO */
    }

    @Override
    public void setPrincipalLinearMapPerLatent(final int latentIndex, final INDArray newLatentPrincipalLinearMap)
            throws UnsupportedOperationException {
        /* TODO */
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
