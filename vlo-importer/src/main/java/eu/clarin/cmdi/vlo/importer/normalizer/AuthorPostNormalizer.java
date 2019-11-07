package eu.clarin.cmdi.vlo.importer.normalizer;

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

    /**
     * Filters invalid author information
     * @param value unfiltered author information
     * @param cmdiData
     * @return filtered author information
     */
    @Override
    public List<String> process(final String value, DocFieldContainer cmdiData) {
        if (value == null || value.length() < MIN_LENGTH || INVALID_AUTHOR_SET.contains(value.toLowerCase())) {
            return Collections.singletonList(null);
        } else {
            return Collections.singletonList(value);
        }
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }
}
