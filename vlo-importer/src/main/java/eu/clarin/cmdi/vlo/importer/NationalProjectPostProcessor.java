package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.importer.VariantsMap.Variant;

/**
 * Adds information about the affiliation of a metadata file to a national
 * project (like CLARIN-X etc.) into facet nationalProject
 *
 * @author Thomas Eckart
 *
 */
public class NationalProjectPostProcessor extends PostProcessorsWithVocabularyMap {

    private final static Logger LOG = LoggerFactory.getLogger(NationalProjectPostProcessor.class);

    private static Map<String, String> nationalProjectMap = null;
    private static Map<Pattern, String> nationalProjectRegExpMap = null;

    /**
     * Returns the national project based on the mapping in
     * Configuration.getNationalProjectMapUrl() If no mapping was found empty
     * String is returned
     *
     * @return
     */
    @Override
    public List<String> process(String value) {
        String input = value.trim();
        List<String> resultList = new ArrayList<String>();

        if (input != null && getVocabularyMap().containsKey(input)) {
            resultList.add(getVocabularyMap().get(input));
            return resultList;
        }

        for (Pattern pattern : getRegExpMapping().keySet()) {
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                resultList.add(getRegExpMapping().get(pattern));
                return resultList;
            }
        }

        resultList.add("");
        return resultList;
    }
    
    @Override
	protected Map<String, String> getVocabularyMap() {
		if(nationalProjectMap == null){
			createControlledVocabularyMap(getMappingFileUrl());
		}
		
		return nationalProjectMap;
	}
    
    private Map<Pattern, String> getRegExpMapping() {
    	if (nationalProjectRegExpMap == null) {
        	createControlledVocabularyMap(getMappingFileUrl());
        }
        return nationalProjectRegExpMap;
    }
    
    
	protected String getMappingFileUrl() {
    	String projectsMappingFile = MetadataImporter.config.getNationalProjectMapping();

        if (projectsMappingFile.length() == 0) {
            // use the packaged project mapping file
            projectsMappingFile = "/nationalProjectsMapping.xml";
        }
        
    	return projectsMappingFile;
	}
	
	@Override
	protected Map<String, String> createControlledVocabularyMap(String mapUrl) {
		if(nationalProjectMap == null){
    		nationalProjectMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    	}
    	
    	if(nationalProjectRegExpMap == null){
    		nationalProjectRegExpMap = new HashMap<Pattern, String>();
    	}
    	
    	VariantsMap map = getMappingFromFile(mapUrl);
        	
    	for(Entry<String, List<Variant>> entry: map.getMap().entrySet()){
    		for(Variant variant: entry.getValue()){
    			if(variant.isRegExp()){
    				nationalProjectRegExpMap.put(Pattern.compile(variant.getValue()), entry.getKey());
    			}else{
    				nationalProjectMap.put(variant.getValue(), entry.getKey());
    			}        			
    		}
        }
    	
    	return nationalProjectMap;
	}
    
}
