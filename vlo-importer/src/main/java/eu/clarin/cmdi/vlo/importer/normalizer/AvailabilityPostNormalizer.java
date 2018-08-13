package eu.clarin.cmdi.vlo.importer.normalizer;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.DocFieldContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author teckart
 */
public class AvailabilityPostNormalizer extends AbstractPostNormalizerWithVocabularyMap {

    public AvailabilityPostNormalizer(VloConfig config) {
        super(config);
    }

    @Override
    public List<String> process(final String value, DocFieldContainer cmdiData) {
        String normalizedVal = normalize(value);
        //Availability variants can be normalized with multiple values, in vocabulary they are separated with ;
        return normalizedVal != null ? Arrays.asList(normalizedVal.split(";")) : new ArrayList<>();
    }

    @Override
    public String getNormalizationMapURL() {
        return getConfig().getLicenseAvailabilityMapUrl();
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }
}
