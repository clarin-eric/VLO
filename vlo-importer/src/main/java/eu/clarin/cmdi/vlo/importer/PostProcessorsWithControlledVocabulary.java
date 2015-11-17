package eu.clarin.cmdi.vlo.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.normalization.pojo.VariantsMap;
import eu.clarin.cmdi.vlo.normalization.service.NormalizationService;
import eu.clarin.cmdi.vlo.normalization.service.NormalizationVocabulary;
import eu.clarin.cmdi.vlo.normalization.service.VariantsMapMarshaller;

/* 
 * abstract class that encapsulates common map creation from mapping files
 * for some postprocessors like LanguageCodePostProcessor*
 * 
 * brings one more level in class hierarchy between interface PostPorcessor and concrete implementations
 * 
 * @author dostojic
 * 
 */

public abstract class PostProcessorsWithControlledVocabulary implements PostProcessor, NormalizationService {

	private final static Logger _logger = LoggerFactory.getLogger(PostProcessorsWithControlledVocabulary.class);

	private NormalizationVocabulary vocabulary;

	public String normalize(String value) {
		return normalize(value, value);
	}

	public String normalize(String value, String fallBackValue) {
		if (vocabulary == null)
			initVocabulary();

		return vocabulary.normalize(value, fallBackValue);
	}

	public Map<String, String> getCrossMappings(String value) {
		if (vocabulary == null)
			initVocabulary();

		return vocabulary.getCrossMappings(value);
	}

	public abstract String getNormalizationMapURL();

	private void initVocabulary() {
		VariantsMap varinatsRawMap = getMappingFromFile(getNormalizationMapURL());
		vocabulary = varinatsRawMap.getMap();

		//printMap();
	}

	protected VariantsMap getMappingFromFile(String mapUrl) {
		
		InputStream is = null;
		File mapUrlFile = new File(mapUrl);
		_logger.info("Reading vocabulary file from: {}", mapUrl);
		// load records from file
		// in the future this should be loaded from CLAVAS directly and the
		// file only used as fallback
		
		
		//try from file and if not exists fetch it from classpath (root of the vlo-vocabularies project)
		Path p = Paths.get(mapUrl);
		
		try {
			is = new FileInputStream(mapUrlFile);
		} catch (Exception e) {
			_logger.warn("File {} not found, trying to fetch it from classpath ...", mapUrl);
			
			is = PostProcessorsWithControlledVocabulary.class.getClassLoader().getResourceAsStream(mapUrlFile.getName());
			if(is == null)
				throw new RuntimeException("Cannot instantiate postProcessor, " + mapUrl + " is not on the classpath");
		}
		
		try{
			return VariantsMapMarshaller.unmarshal(is);
		} catch (Exception e) {
			throw new RuntimeException("Cannot instantiate postProcessor: ", e);
		}
			
		
	}

	// for debug
	public void printMap() {
		_logger.info("map contains {} entries", vocabulary.getEntries().length);
		for(int i = 0; i < vocabulary.getEntries().length; i++)
			_logger.info(vocabulary.getEntries()[i].toString());
			

	}
}
