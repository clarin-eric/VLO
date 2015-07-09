package eu.clarin.cmdi.vlo.importer;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* 
 * abstract class that encapsulates common map creation from mapping files
 * for some postprocessors like LanguageCodePostProcessor*
 * 
 * brings one more level in class hierarchy between interface PostPorcessor and concrete implementations
 * 
 */

public abstract class PostProcessorsWithVocabularyMap implements PostProcessor{
	
	 private final static Logger LOG = LoggerFactory.getLogger(PostProcessorsWithVocabularyMap.class);
	 	 
	 
	 /*
	  * returns specific static map and should call createControlledVocabularyMap 
	  */
	 protected abstract Map<String, String> getVocabularyMap();
	 
	 	 
	 protected Map<String, String> createControlledVocabularyMap(String mapUrl) {
		 VariantsMap map = getMappingFromFile(mapUrl);
     	 return map.getInvertedMap();
	  }
	 
	 protected VariantsMap getMappingFromFile(String mapUrl){
		 try {
	        	
	        	LOG.info("Reading configuration file from: {}", mapUrl);
	            // load records from file
	        	// in the future this should be loaded from CLAVAS directly and the file only used as fallback            	
	        	InputStream is = PostProcessorsWithVocabularyMap.class.getResourceAsStream(mapUrl); 	
	        	return (VariantsMap) VLOMarshaller.unmarshal(is, VariantsMap.class);
	     } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate postProcessor:", e);
         }
	 }
	 
	 // for debug purposes
	 public void printMap(){
		 LOG.info("map contains {} entries", getVocabularyMap().size());
		 for(Entry<String, String> e: getVocabularyMap().entrySet()){
			 LOG.info("Key <{}> will be mapped to <{}>", e.getKey(), e.getValue());
		 }
	 }
	 
	


}
