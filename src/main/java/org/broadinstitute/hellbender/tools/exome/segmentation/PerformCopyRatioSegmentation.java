package org.broadinstitute.hellbender.tools.exome.segmentation;

import org.broadinstitute.hellbender.cmdline.*;
import org.broadinstitute.hellbender.cmdline.programgroups.CopyNumberProgramGroup;
import org.broadinstitute.hellbender.exceptions.UserException;
import org.broadinstitute.hellbender.tools.exome.*;
import org.broadinstitute.hellbender.tools.exome.allelefraction.AllelicPanelOfNormals;
import org.broadinstitute.hellbender.tools.exome.segmentation.AlleleFractionSegmenter;
import org.broadinstitute.hellbender.utils.SimpleInterval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by davidben on 5/23/16.
 */
@CommandLineProgramProperties(
        summary = "Segment genomic data into regions of constant minor allele fraction.  Only supports one sample input.",
        oneLineSummary = "Segment genomic data into regions of constant minor allele fraction",
        programGroup = CopyNumberProgramGroup.class
)
public class PerformCopyRatioSegmentation extends CommandLineProgram {
    protected static final String NUM_STATES_LONG_NAME = "numberOfStates";
    protected static final String NUM_STATES_SHORT_NAME = "numStates";


    @Argument(
            doc = "Tangent normalized read counts file",
            shortName = ExomeStandardArgumentDefinitions.TARGET_FILE_SHORT_NAME,
            fullName =  ExomeStandardArgumentDefinitions.TARGET_FILE_LONG_NAME,
            optional = false
    )
    protected String coverageFile;


    @Argument(
            doc = "Initial number of hidden allele fraction states",
            fullName = NUM_STATES_LONG_NAME,
            shortName = NUM_STATES_SHORT_NAME,
            optional = false
    )
    protected int initialNumStates;


    @Argument(
            doc = "Output file for copy ratio segments.",
            fullName = ExomeStandardArgumentDefinitions.SEGMENT_FILE_LONG_NAME,
            shortName = ExomeStandardArgumentDefinitions.SEGMENT_FILE_SHORT_NAME,
            optional = false
    )
    protected File outputSegmentsFile;

    @Override
    public Object doWork() {
        final String sampleName = ReadCountCollectionUtils.getSampleNameForCLIsFromReadCountsFile(new File(coverageFile));
        final ReadCountCollection counts;
        try {
            counts = ReadCountCollectionUtils.parse(new File(coverageFile));
        } catch (final IOException ex) {
            throw new UserException.BadInput("could not read input file");
        }

        final List<Double> coverage = Arrays.stream(counts.counts().getColumn(0)).mapToObj(x->x).collect(Collectors.toList());
        final List<SimpleInterval> intervals = counts.targets().stream().map(Target::getInterval).collect(Collectors.toList());
        final CopyRatioSegmenter segmenter = new CopyRatioSegmenter(initialNumStates, intervals, coverage);
        final List<ModeledSegment> segments = segmenter.findSegments();
        SegmentUtils.writeModeledSegmentFile(outputSegmentsFile, segments, sampleName, true);

        return "SUCCESS";
    }
}
