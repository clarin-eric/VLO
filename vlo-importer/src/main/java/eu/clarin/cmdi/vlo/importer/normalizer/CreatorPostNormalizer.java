package eu.clarin.cmdi.vlo.importer.normalizer;

import com.google.common.collect.ImmutableSet;

import eu.clarin.cmdi.vlo.importer.DocFieldContainer;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreatorPostNormalizer extends AbstractPostNormalizer {
    private final static Integer MIN_LENGTH = 6;
    private final static Set<String> INVALID_CREATOR_SET
            = ImmutableSet.<String>builder()
                    .add("nicht vorhanden")
                    .add("nicht dokumentiert")
                    .add("unspecified")
                    .add("unknown")
                    .build();
    Pattern numberPattern = Pattern.compile("^[0-9].*");
 
   /**
     * Filters/reformats invalid creator information
     * @param value unfiltered creator information
     * @param cmdiData
     * @return filtered creator information
     */
    @Override
    public List<String> process(String value, DocFieldContainer cmdiData) {
        if (value == null || value.length() < MIN_LENGTH || INVALID_CREATOR_SET.contains(value.toLowerCase())) {
            return Collections.singletonList(null);
        } else {
            value = value.trim();
            // treatment of input like "ROLE : NAME"
            if(value.contains(":") && !value.contains("http")) {
                value = value.substring(value.indexOf(":") + 1).trim();
            }
            // reject URLs
            if(value.startsWith("http")) {
                return Collections.singletonList(null);
            }

            // reject numbers (often used for anonymisation)
            Matcher matcher = numberPattern.matcher(value);
            if (matcher.matches()) {
                return Collections.singletonList(null);
            }

            return Collections.singletonList(value);
        }
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }
}
