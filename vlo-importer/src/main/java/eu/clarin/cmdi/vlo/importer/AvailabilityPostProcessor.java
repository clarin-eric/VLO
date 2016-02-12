package eu.clarin.cmdi.vlo.importer;

import java.util.List;

/**
 *
 * @author teckart
 */
public class AvailabilityPostProcessor extends PostProcessorsWithVocabularyMap {
	
	private static final Integer MAX_LENGTH = 20;
    private static final String OTHER_VALUE = "Other";
    

    @Override
    public List<String> process(final String value) {  
        return normalize(value, value.length() > MAX_LENGTH? OTHER_VALUE : value.trim());
    }


	@Override
	public String getNormalizationMapURL() {
		return MetadataImporter.config.getLicenseAvailabilityMapUrl();
	}
}
