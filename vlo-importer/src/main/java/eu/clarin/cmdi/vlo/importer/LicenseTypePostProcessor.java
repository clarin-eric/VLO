package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Twan Goosen
 */
public class LicenseTypePostProcessor extends PostProcessorsWithVocabularyMap {

    @Override
    public List<String> process(final String value) {
        String normalizedVal = normalize(value);
        //Availability variants can be normalized with multiple values, in vocabulary they are separated with ;
        return normalizedVal != null ? Arrays.asList(normalizedVal.split(";")) : new ArrayList<>();
    }

    @Override
    public String getNormalizationMapURL() {
        return MetadataImporter.config.getLicenseTypeMapUrl();
    }
}
