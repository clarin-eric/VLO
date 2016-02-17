package eu.clarin.cmdi.vlo.importer;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.normalization.NormalizationService;
import eu.clarin.cmdi.vlo.normalization.NormalizationVocabulary;
import eu.clarin.cmdi.vlo.pojo.VariantsMap;
import eu.clarin.cmdi.vlo.transformers.VariantsMapMarshaller;

/* 
 * abstract class that encapsulates common map creation from mapping files
 * for some postprocessors like LanguageCodePostProcessor*
 * 
 * brings one more level in class hierarchy between interface PostPorcessor and concrete implementations
 * 
 * @author dostojic
 * 
 */

public abstract class PostProcessorsWithVocabularyMap implements PostProcessor, NormalizationService {

	private final static Logger _logger = LoggerFactory.getLogger(PostProcessorsWithVocabularyMap.class);

	private NormalizationVocabulary vocabulary;

	public String normalize(String value) {
		if (vocabulary == null)
			initVocabulary();

		//all variant values are kept in lower case
		return vocabulary.normalize(value.toLowerCase());
	}

	public String normalize(String value, String fallBackValue) {
		String normalizedVals = normalize(value);
		return normalizedVals != null ? normalizedVals : fallBackValue;
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

		_logger.info("Reading vocabulary file from: {}", mapUrl);
		// load records from file
		// in the future this should be loaded from CLAVAS directly and the
		// file only used as fallback

		InputStream is = PostProcessorsWithVocabularyMap.class.getClassLoader().getResourceAsStream(mapUrl);
		if (is == null)
			throw new RuntimeException("Cannot instantiate postProcessor, " + mapUrl + " is not on the classpath");

		try {
			return VariantsMapMarshaller.unmarshal(is);
		} catch (Exception e) {
			throw new RuntimeException("Cannot instantiate postProcessor: ", e);
		}

	}

	// for debug
	public void printMap() {
		_logger.info("map contains {} entries", vocabulary.getEntries().length);
		for (int i = 0; i < vocabulary.getEntries().length; i++)
			_logger.info(vocabulary.getEntries()[i].toString());

	}
}
