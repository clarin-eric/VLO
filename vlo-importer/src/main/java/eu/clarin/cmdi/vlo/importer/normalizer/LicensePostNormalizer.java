package eu.clarin.cmdi.vlo.importer.normalizer;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.DocFieldContainer;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LicensePostNormalizer extends AbstractPostNormalizerWithVocabularyMap {

    private final static Logger LOG = LoggerFactory.getLogger(LicensePostNormalizer.class);
    private final static Pattern LICENSE_URL_PATTERN = Pattern.compile("^https?:\\/\\/.*", Pattern.CASE_INSENSITIVE);

    public LicensePostNormalizer(VloConfig config) {
        super(config);
    }

    @Override
    public List<String> process(String value, DocFieldContainer cmdiData) {
        String normalizedVal = normalize(value);
        if (normalizedVal != null) {
            return Collections.singletonList(normalizedVal);
        } else {
            if (LICENSE_URL_PATTERN.matcher(value).matches()) {
                // We want to let candidates for URLs through, if even they
                // do not appear in the map
                // See https://github.com/clarin-eric/VLO/issues/149
                LOG.info("Found license URI canidate that is not in license mapping definition {}", value);
                return Collections.singletonList(value);
            } else {
                return Collections.emptyList();
            }
        }
    }

    @Override
    public String getNormalizationMapURL() {
        return getConfig().getLicenseURIMapUrl();
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }
}
