package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LicensePostProcessor implements PostProcessor {

    private static final Map<Pattern, String> licenseMap;
    private static final Integer MAX_LENGTH = 20;
    private static final String OTHER_VALUE = "Other";

    static {
        licenseMap = new HashMap<Pattern, String>();
        licenseMap.put(Pattern.compile(".*sldr.org.*licence_v1.*"), "SLDR v1");
        licenseMap.put(Pattern.compile("The Conditions of Use for the IPROSLA data set specify.*"), "IPROSLA");
    }

    @Override
    public List<String> process(final String value) {
        String result = value;
        List<String> resultList = new ArrayList<String>();
        
        // first letter should be upper case
        if(result.length() > 1) {
            result = result.substring(0, 1).toUpperCase().concat(result.substring(1, result.length()));
        }

        for (Pattern pattern : licenseMap.keySet()) {
            Matcher licenseMatcher = pattern.matcher(result.trim());
            if (licenseMatcher.matches()) {
                resultList.add(licenseMap.get(pattern));
                break;
            }
        }

        if (resultList.isEmpty()) {
            if (result.length() > MAX_LENGTH) {
                resultList.add(OTHER_VALUE);                        
            } else {
                resultList.add(result.trim());
            }
        }

        return resultList;
    }
}
