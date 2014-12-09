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
        List<String> resultList = new ArrayList<String>();

        for (Pattern pattern : licenseMap.keySet()) {
            Matcher licenseMatcher = pattern.matcher(value.trim());
            if (licenseMatcher.matches()) {
                resultList.add(licenseMap.get(pattern));
                break;
            }
        }

        if (resultList.isEmpty()) {
            if (value.length() > MAX_LENGTH) {
                resultList.add(OTHER_VALUE);                        
            } else {
                resultList.add(value.trim());
            }
        }

        return resultList;
    }
}
