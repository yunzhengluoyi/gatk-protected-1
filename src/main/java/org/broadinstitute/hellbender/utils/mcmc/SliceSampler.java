package org.broadinstitute.hellbender.utils.mcmc;


import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.broadinstitute.hellbender.utils.Utils;
import org.broadinstitute.hellbender.utils.param.ParamUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Implements slice sampling of a continuous, univariate, unnormalized probability density function,
 * which is assumed to be unimodal.  See Neal 2003 at https://projecteuclid.org/euclid.aos/1056562461 for details.
 *
 * @author Samuel Lee &lt;slee@broadinstitute.org&gt;
 */
public final class SliceSampler {
    private static final int MAXIMUM_NUMBER_OF_DOUBLINGS = 16;
    private static final int MAXIMUM_NUMBER_OF_SLICE_SAMPLINGS = 100;
    private static final double EPSILON = 1E-10;

    private final RandomGenerator rng;
    private final Function<Double, Double> logPDF;
    private final double xMin;
    private final double xMax;
    private final double width;
    private final ExponentialDistribution exponentialDistribution;

    /**
     * Creates a new sampler, given a random number generator, a continuous, univariate, unimodal, unnormalized
     * log probability density function, hard limits on the random variable, and a step width.
     * @param rng      random number generator
     * @param logPDF   continuous, univariate, unimodal log probability density function (up to additive constant)
     * @param xMin     minimum allowed value of the random variable
     * @param xMax     maximum allowed value of the random variable
     * @param width    step width for slice expansion
     */
    public SliceSampler(final RandomGenerator rng, final Function<Double, Double> logPDF,
                        final double xMin, final double xMax, final double width) {
        Utils.nonNull(rng);
        Utils.nonNull(logPDF);
        Utils.validateArg(xMin <= xMax, "Maximum bound must be greater than or equal to minimum bound.");
        ParamUtils.isPositive(width, "Slice-sampling width must be positive.");
        this.rng = rng;
        this.logPDF = logPDF;
        this.xMin = xMin;
        this.xMax = xMax;
        this.width = width;
        this.exponentialDistribution = new ExponentialDistribution(rng, 1.);
    }

    /**
     * Creates a new sampler, given a random number generator, a continuous, univariate, unimodal, unnormalized
     * log probability density function, and a step width.
     * @param rng      random number generator
     * @param logPDF   continuous, univariate, unimodal log probability density function (up to additive constant)
     * @param width    step width for slice expansion
     */
    public SliceSampler(final RandomGenerator rng, final Function<Double, Double> logPDF, final double width) {
        this(rng, logPDF, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, width);
    }

    /**
     * Generate a single sample from the probability density function, given an initial value to use in slice construction.
     * @param xInitial      initial value to use in slice construction; if outside [xMin, xMax], forced to be within
     * @return              sample drawn from the probability density function
     */
    public double sample(final double xInitial) {
        final double xSample = Math.min(Math.max(xInitial, xMin + EPSILON), xMax - EPSILON);
        //randomly pick height of slice from uniform distribution under PDF
        //(equivalently, from exponential distribution under logPDF)
        final double logSliceHeight = logPDF.apply(xSample) - exponentialDistribution.sample();

        //randomly position slice with given width so that it brackets xSample; position is uniformly distributed
        double xLeft = xSample - width * rng.nextDouble();
        double xRight = xLeft + width;

        int k = MAXIMUM_NUMBER_OF_DOUBLINGS;
        //expand slice by doubling until it brackets logPDF
        while (k > 0 && (logPDF.apply(xLeft) > logSliceHeight || logPDF.apply(xRight) > logSliceHeight) &&
                xLeft > xMin && xRight < xMax) {
            if (rng.nextBoolean()) {
                xLeft = Math.max(xLeft - (xRight - xLeft), xMin);
            } else {
                xRight = Math.min(xRight + (xRight - xLeft), xMax);
            }
            k--;
        }

        //sample uniformly from slice until sample under logPDF found, shrink slice on each iteration if not found
        //limited to MAXIMUM_NUMBER_OF_SLICE_SAMPLINGS, after which last proposed sample is returned
        //(shouldn't happen if width is chosen appropriately)
        int numIterations = 1;
        double xProposed = rng.nextDouble() * (xRight - xLeft) + xLeft;
        while (logPDF.apply(xProposed) < logSliceHeight && numIterations <= MAXIMUM_NUMBER_OF_SLICE_SAMPLINGS) {
            if (xProposed < xSample) {
                xLeft = xProposed;
            } else {
                xRight = xProposed;
            }
            xProposed = rng.nextDouble() * (xRight - xLeft) + xLeft;
            numIterations++;
        }

        return xProposed;
    }

    /**
     * Generate multiple samples from the probability density function, given an initial value to use in slice construction.
     * @param xInitial      initial value to use in slice construction; if outside [xMin, xMax], forced to be within
     * @param numSamples    number of samples to generate
     * @return              samples drawn from the probability density function
     */
    public List<Double> sample(final double xInitial, final int numSamples) {
        ParamUtils.isPositive(numSamples, "Number of samples must be positive.");
        final List<Double> samples = new ArrayList<>(numSamples);
        double xSample = Math.min(Math.max(xInitial, xMin + EPSILON), xMax - EPSILON);
        for (int i = 0; i < numSamples; i++) {
            xSample = sample(xSample);
            samples.add(xSample);
        }
        return samples;
    }
}