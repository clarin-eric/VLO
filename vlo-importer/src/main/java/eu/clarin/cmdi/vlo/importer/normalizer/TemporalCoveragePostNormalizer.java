package eu.clarin.cmdi.vlo.importer.normalizer;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.clarin.cmdi.vlo.importer.DocFieldContainer;
import java.util.Collection;

public class TemporalCoveragePostNormalizer extends AbstractPostNormalizer {
    private final FieldNameService fieldNameService;

    // Open Date Range Format (like "2019-12-02/2020-01-01")
    private static final Pattern ODRF_PATTERN = Pattern.compile("^(([0-9]{4})(-[0-9]{2}-[0-9]{2})?)?/(([0-9]{4})(-[0-9]{2}-[0-9]{2})?)?");
    // Simple Year Range Format (like "1980-1990")
    private static final Pattern YEAR_RANGE_PATTERN = Pattern.compile("([0-9]{4})( )?\\-( )?([0-9]{4})");
    // Simplified W3C DateTime (like "2020-02-01")
    private static final Pattern DATETIME_PATTERN = Pattern.compile("^([0-9]{4})(-[0-9]{2})?(-[0-9]{2})?");
    // Solr DateRange format (like "[1980 TO 2000]");
    private static final Pattern SOLR_DATERANGE_PATTERN = Pattern.compile("\\[([0-9]+) TO ([0-9]+)\\]");

    public TemporalCoveragePostNormalizer(VloConfig config) {
        super(config);
        this.fieldNameService = new FieldNameServiceImpl(config);
    }

    /**
     * Tries to identify relevant temporal substrings in input
     *
     * @param value extracted date or date range String
     * @param cmdiData
     * @return List of accepted values
     */
    @Override
    public List<String> process(final String value, DocFieldContainer cmdiData) {
        String coverageString = value.trim();
        Integer startYear = null;
        Integer endYear = null;

        List<String> resultList = new ArrayList<>();
        Matcher odrfMatcher = ODRF_PATTERN.matcher(coverageString);
        Matcher yearRangeMatcher;
        Matcher yearMatcher;

        if (odrfMatcher.find()) {
            String tmp = odrfMatcher.group(2);
            if(tmp != null)
                startYear = Integer.parseInt(tmp);
            tmp = odrfMatcher.group(5);
            if(tmp != null)
                endYear = Integer.parseInt(tmp);
        } else if ((yearRangeMatcher = YEAR_RANGE_PATTERN.matcher(coverageString)).find()) {
            startYear = Integer.parseInt(yearRangeMatcher.group(1));
            endYear = Integer.parseInt(yearRangeMatcher.group(4));
        } else if ((yearMatcher = DATETIME_PATTERN.matcher(coverageString)).find()) {
            startYear = Integer.parseInt(yearMatcher.group(1));
        }

        // only one boundary is set --> startYear = endYear
        if (startYear != null && endYear == null) {
            endYear = startYear;
        } else if (startYear == null && endYear != null) {
            startYear = endYear;
        }

        if (startYear != null) {
            // use and delete old value (if any)
            if (cmdiData != null) {
                final Collection<Object> temporalCoverageValues = cmdiData.getDocField(fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE));
                if (temporalCoverageValues != null && !temporalCoverageValues.isEmpty()) {
                    // extract old values
                    Integer[] oldValues = extractDateRange(temporalCoverageValues.toArray()[0].toString());
                    if(oldValues != null) {
                        if(oldValues[0] < startYear)
                            startYear = oldValues[0];
                        if(oldValues[1] > endYear)
                            endYear = oldValues[1];
                    }
                    // delete old value
                    cmdiData.removeField(fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE));
                }
            }

            // set new value
            resultList.add(createDateRange(startYear, endYear));
        }

        return resultList;
    }

    public static Integer[] extractDateRange(final String value) {
        Matcher solrMatcher = SOLR_DATERANGE_PATTERN.matcher(value);
        Integer[] rangeArray = null;
        if (solrMatcher.find()) {
            rangeArray = new Integer[2];
            rangeArray[0] = Integer.parseInt(solrMatcher.group(1));
            rangeArray[1] = Integer.parseInt(solrMatcher.group(2));
        }
        return rangeArray;
    }

    private String createDateRange(final Integer startYear, final Integer endYear) {
        return String.format("[%d TO %d]", startYear, endYear);
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }
}
