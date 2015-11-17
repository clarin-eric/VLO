package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.List;


/**
 * Adds information about the affiliation of a metadata file to a national
 * project (like CLARIN-X etc.) into facet nationalProject
 *
 * @author Thomas Eckart
 *
 */
public class NationalProjectPostProcessor extends PostProcessorsWithControlledVocabulary {


    /**
     * Returns the national project based on the mapping in
     * Configuration.getNationalProjectMapUrl() If no mapping was found empty
     * String is returned
     *
     * @return
     */
    @Override
    public List<String> process(String value) {
        List<String> resultList = new ArrayList<String>();
        resultList.add(normalize(value.trim(), ""));
        
        return resultList;
    }
    

	@Override
	public String getNormalizationMapURL() {
		return MetadataImporter.config.getNationalProjectMapping();
	}
    
}
