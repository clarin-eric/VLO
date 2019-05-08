package eu.clarin.cmdi.vlo.importer.normalizer;

import java.util.List;
import java.util.regex.Pattern;

import eu.clarin.cmdi.vlo.importer.DocFieldContainer;
import java.util.Collections;

import java.util.regex.Matcher;

public class FormatPostNormalizer extends AbstractPostNormalizer {

    private static final Pattern MIMETYPE_PATTERN = Pattern.compile("^((application|audio|example|image|message|model|multipart|text|video)\\/([^\\s;]+))( ?;.*)?");
    private static final String UNKNOWN_STRING = "unknown type";

    /**
     * Returns value if it is a valid MIMEtype or UNKNOWN_STRING otherwise
     *
     * @param value potential MIMEType value
     * @return value if it is a valid MIMEtype or UNKNOWN_STRING otherwise
     */
    @Override
    public List<String> process(String value, DocFieldContainer cmdiData) {
        final Matcher mimeTypeMatcher = MIMETYPE_PATTERN.matcher(value);

        if (mimeTypeMatcher.matches() && mimeTypeMatcher.groupCount() > 0) {
            return Collections.singletonList((mimeTypeMatcher.group(1)));
        } else {
            return Collections.singletonList(UNKNOWN_STRING);
        }
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }
}
