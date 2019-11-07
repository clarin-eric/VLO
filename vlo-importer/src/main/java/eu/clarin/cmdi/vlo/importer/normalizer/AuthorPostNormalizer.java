package eu.clarin.cmdi.vlo.importer.normalizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import eu.clarin.cmdi.vlo.importer.DocFieldContainer;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AuthorPostNormalizer extends AbstractPostNormalizer {
    private final static Integer MIN_LENGTH = 6;
    private final static Set<String> INVALID_AUTHOR_SET
            = ImmutableSet.<String>builder()
                    .add("nicht vorhanden")
                    .add("nicht dokumentiert")
                    .add("unspecified")
                    .add("unknown")
                    .build();
    private final static List<String> AUTHORITY_FILES_LIST
            = ImmutableList.<String>builder()
                    .add("orcid.org")
                    .add("d-nb.info/gnd")
                    .add("viaf.org")
                    .add("isni.org")
                    .build();
 
   /**
     * Filters invalid author information
     * @param value unfiltered author information
     * @param cmdiData
     * @return filtered author information
     */
    @Override
    public List<String> process(String value, DocFieldContainer cmdiData) {
        if (value == null || value.length() < MIN_LENGTH || INVALID_AUTHOR_SET.contains(value.toLowerCase())) {
            return Collections.singletonList(null);
        } else {
            value = value.trim();
            // treatment of input like "ROLE : NAME"
            if(value.contains(":") && !value.startsWith("http")) {
                value = value.substring(value.indexOf(":") + 1).trim();
            }
            // only accept links to common person authority files
            if(value.startsWith("http")) {
                Boolean acceptableUrl = false;
                for(String authorityFile : AUTHORITY_FILES_LIST)
                    if(value.contains(authorityFile)) {
                        acceptableUrl = true;
                        break;
                    }
                if(!acceptableUrl)
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
