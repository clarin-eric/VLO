package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemporalCoveragePostProcessor implements PostProcessor {

    // Open Date Range Format
    private static final Pattern ODRF_PATTERN = Pattern.compile("(([0-9]{4}(-[0-9]{2})?(-[0-9]{2})?)?/([0-9]{4}(-[0-9]{2})?(-[0-9]{2})?)?)");
    // simple year
    private static final Pattern YEAR_PATTERN = Pattern.compile("([0-9]{4}(-[0-9]{2})?(-[0-9]{2})?)");

    /**
     * Tries to identify relevant year substrings in input
     *
     * @param value extracted year or date range String
     * @return List of accepted values
     */
    @Override
    public List<String> process(final String value) {
        Matcher odrfMatcher = ODRF_PATTERN.matcher(value);
        Matcher yearMatcher = YEAR_PATTERN.matcher(value);
        List<String> resultList = new ArrayList<String>();

        if (odrfMatcher.find()) {
            resultList.add(odrfMatcher.group(1));
        } else if (yearMatcher.find()) {
            resultList.add(yearMatcher.group(1));
        }

        return resultList;
    }
}
