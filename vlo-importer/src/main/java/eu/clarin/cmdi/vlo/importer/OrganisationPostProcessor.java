package eu.clarin.cmdi.vlo.importer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OrganisationPostProcessor extends PostProcessorsWithVocabularyMap{


	 private static Map<String, String> organisationNamesMap = null;
	 
    /**
     * Splits values for organisation facet at delimiter ';' and replaces
     * organisation name variants with their official name from a controlled
     * vocabulary
     *
     * @param value extracted organisation name/names
     * @return List of organisation names (splitted at semicolon) and variations
     * replaced with controlled vocabulary
     */
    @Override
    public List<String> process(String value) {
        String[] splitArray = normalizeInputString(value).split(";");
        for (int i = 0; i < splitArray.length; i++) {
            String orgaName = splitArray[i];
            if (getVocabularyMap().containsKey(normalizeVariant(orgaName))) {
                splitArray[i] = getVocabularyMap().get(normalizeVariant(orgaName));
            }
        }
        
        return Arrays.asList(splitArray);
    }
    
    @Override
	protected Map<String, String> getVocabularyMap() {
		if(organisationNamesMap == null){
			organisationNamesMap = createControlledVocabularyMap(MetadataImporter.config.getOrganisationNamesUrl());
		}
		return organisationNamesMap;
	}
    
    private String normalizeInputString(String value) {
        return value.replaceAll("\\s+", " ");
    }
    
    private String normalizeVariant(String key) {
        return key.toLowerCase().replaceAll("-", " ");
    }

	
    
    
}
