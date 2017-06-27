package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author teckart
 */
public class AvailabilityPostProcessor extends PostProcessorsWithVocabularyMap {

    public AvailabilityPostProcessor(VloConfig config) {
        super(config);
    }

    @Override
    public List<String> process(final String value, CMDIData cmdiData) {
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
