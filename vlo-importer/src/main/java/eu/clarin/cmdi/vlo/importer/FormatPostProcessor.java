package eu.clarin.cmdi.vlo.importer;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class FormatPostProcessor implements PostProcessor {
    private static final Pattern MIMETYPE_PATTERN = Pattern.compile("^(application|audio|example|image|message|model|multipart|text|video)/.*");
    private static final String UNKNOWN_STRING = "unknown type";

    /**
     * Returns value if it is a valid MIMEtype or UNKNOWN_STRING otherwise
     * @param value potential MIMEType value
     * @return value if it is a valid MIMEtype or UNKNOWN_STRING otherwise
     */
    @Override
    public String process(String value) {
        Matcher mimeTypeMatcher = MIMETYPE_PATTERN.matcher(value);

        if (mimeTypeMatcher.matches()) {
            return value;
        } else {
            return UNKNOWN_STRING;
        }
    }
}
