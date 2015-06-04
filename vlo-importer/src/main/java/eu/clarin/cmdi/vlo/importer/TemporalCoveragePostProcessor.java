package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemporalCoveragePostProcessor implements PostProcessor {

    // Open Date Range Format
    private static final Pattern ODRF_PATTERN = Pattern.compile("^(([0-9]{4}(-[0-9]{2})?(-[0-9]{2})?)?/([0-9]{4}(-[0-9]{2})?(-[0-9]{2})?)?)");
    // Simplified W3C DateTime
    private static final Pattern DATETIME_PATTERN = Pattern.compile("^([0-9]{4}(-[0-9]{2})?(-[0-9]{2})?)");

    /**
     * Tries to identify relevant temporal substrings in input
     *
     * @param value extracted date or date range String
     * @return List of accepted values
     */
    @Override
    public List<String> process(final String value) {
        String coverageString = value.trim();
        
        Matcher odrfMatcher = ODRF_PATTERN.matcher(coverageString);
        Matcher yearMatcher = DATETIME_PATTERN.matcher(coverageString);
        List<String> resultList = new ArrayList<String>();

        if (odrfMatcher.find()) {
            resultList.add(odrfMatcher.group(1));
        } else if (yearMatcher.find()) {
            resultList.add(yearMatcher.group(1));
        }

        return resultList;
    }
}
