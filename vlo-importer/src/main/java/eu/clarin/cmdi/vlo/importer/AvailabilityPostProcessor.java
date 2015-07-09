package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author teckart
 */
public class AvailabilityPostProcessor extends PostProcessorsWithVocabularyMap {

	private static Map<String, String> availabilityMap;
    
	private static final Integer MAX_LENGTH = 20;
    private static final String OTHER_VALUE = "Other";
    

    @Override
    public List<String> process(final String value) {
        String result = value;
        List<String> resultList = new ArrayList<String>();

        if (getVocabularyMap().containsKey(value)) {
            resultList.add(getVocabularyMap().get(value));
        } else {
            if (result.length() > MAX_LENGTH) {
                resultList.add(OTHER_VALUE);
            } else {
                resultList.add(result.trim());
            }
        }

        return resultList;
    }
    
    public Map<String, String> getVocabularyMap(){
    	if(availabilityMap == null){
    		availabilityMap = createControlledVocabularyMap(MetadataImporter.config.getLicenseAvailabilityMapUrl());
		 }
    	
		 return availabilityMap;
    }
}
