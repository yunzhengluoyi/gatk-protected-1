package org.broadinstitute.hellbender.tools.coveragemodel;

import org.broadinstitute.hellbender.utils.param.ParamUtils;

/**
 * Parameters for {@link TargetCoverageEMModeler}.
 *
 * TODO fourier factors
 *
 * @author Mehrtash Babadi &lt;mehrtash@broadinstitute.org&gt;
 */
public class TargetCoverageEMParams {

    /* maximum number of EM iterations */
    private int maxEMIterations = 50;

    /* stopping criterion w.r.t. change in the model log likelihood */
    private double logLikelihoodTol = 1e-4;

    /* number of sequantial maximization steps in the M step */
    private int numSequantialMaximizations = 1;

    /* use Fourier regularization or not */
    private boolean useFourierRegularization = false;

    /* Fourier regularization strength */
    private double fourierRegularizationStrength = 10000;

    /* M-step error tolerance in maximiming w.r.t. Psi */
    private double psiTol = 1e-6;

    /* M-step error tolerance in maximizing w.r.t. W (if Fourier regularization is enabled) */
    private double wTol = 1e-6;

    /********************************
     * accessor and mutator methods *
     ********************************/

    public TargetCoverageEMParams setMaxEMIterations(final int maxEMIterations) {
        this.maxEMIterations = ParamUtils.isPositive(maxEMIterations, "Maximum EM iterations must be positive.");
        return this;
    }

    public int getMaxEMIterations() { return maxEMIterations; }

    public TargetCoverageEMParams setLogLikelihoodTolerance(final double tol) {
        logLikelihoodTol = ParamUtils.isPositive(tol, "The required tolerance on log likelihood " +
                "must be positive.");
        return this;
    }

    public double getLogLikelihoodTolerance() { return logLikelihoodTol; }

    public TargetCoverageEMParams setNumSequantialMaximizations(final int numSequantialMaximizations) {
        this.numSequantialMaximizations = ParamUtils.isPositive(numSequantialMaximizations, "The number of " +
                "sequential partial maximimization steps must be positive.");
        return this;
    }

    public int getNumSequantialMaximizations() { return numSequantialMaximizations; }

    public TargetCoverageEMParams enableFourierRegularization() {
        useFourierRegularization = true;
        return this;
    }

    public TargetCoverageEMParams disableFourierRegularization() {
        useFourierRegularization = false;
        return this;
    }

    public boolean fourierRegularizationEnabled() { return useFourierRegularization; }

    public TargetCoverageEMParams setFourierRegularizationStrength(final double fourierRegularizationStrength) {
        this.fourierRegularizationStrength = ParamUtils.isPositive(fourierRegularizationStrength, "The Fourier " +
                "regularization strength must be positive");
        return this;
    }

    public double getFourierRegularizationStrength() { return fourierRegularizationStrength; }

    public TargetCoverageEMParams setPsiTolerance(final double tol) {
        this.psiTol = ParamUtils.isPositive(tol, "The tolerance for maximization of Psi must be positive");
        return this;
    }

    public double getPsiTolerance() { return psiTol; }

    public TargetCoverageEMParams setWTolerance(final double tol) {
        this.wTol = ParamUtils.isPositive(tol, "The tolerance for maximization of Psi must be positive");
        return this;
    }

    public double getWTolerance() { return this.wTol; }

}
