package eu.clarin.cmdi.vlo.importer;

import java.util.Arrays;
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
    	String normalizedVal = normalize(value);
    	if(normalizedVal == null)
    		return Arrays.asList(value.length() > MAX_LENGTH? OTHER_VALUE : value.trim());
    	//Availability variants can be normalized with multiple values, in vocabulary they are separated with ;
    	else{
    		return Arrays.asList(normalizedVal.split(";"));
    	}
        
    }


	@Override
	public String getNormalizationMapURL() {
		return MetadataImporter.config.getLicenseAvailabilityMapUrl();
	}
}
