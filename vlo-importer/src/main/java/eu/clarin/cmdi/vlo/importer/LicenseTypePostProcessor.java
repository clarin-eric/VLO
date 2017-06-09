package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Twan Goosen
 */
public class LicenseTypePostProcessor extends PostProcessorsWithVocabularyMap {

    @Override
    public List<String> process(final String value, CMDIData cmdiData) {
        if(value == null) {
            //TODO: take values from availability facet
            return Collections.emptyList();
        }
        String normalizedVal = normalize(value);
        //Availability variants can be normalized with multiple values, in vocabulary they are separated with ;
        if (normalizedVal != null) {
            return Arrays.asList(normalizedVal.split(";"));
        } else {
            //TODO: take values from availability facet
            return new ArrayList<>();
        }
    }

    @Override
    public String getNormalizationMapURL() {
        return MetadataImporter.config.getLicenseTypeMapUrl();
    }

    @Override
    public boolean doesProcessNoValue() {
        return true;
    }
}
