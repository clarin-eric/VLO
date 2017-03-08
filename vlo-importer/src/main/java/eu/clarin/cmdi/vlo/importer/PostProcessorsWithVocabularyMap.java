package eu.clarin.cmdi.vlo.importer;

import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.normalization.NormalizationService;
import eu.clarin.cmdi.vlo.normalization.NormalizationVocabulary;
import eu.clarin.cmdi.vlo.normalization.VocabularyEntry;
import eu.clarin.cmdi.vlo.pojo.VariantsMap;
import eu.clarin.cmdi.vlo.transformers.VariantsMapMarshaller;
import javax.xml.bind.JAXBException;

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

    private final static Logger LOG = LoggerFactory.getLogger(PostProcessorsWithVocabularyMap.class);

    private NormalizationVocabulary vocabulary;

    @Override
    public String normalize(String value) {
        if (vocabulary == null) {
            initVocabulary();
        }

        //all variant values are kept in lower case
        return vocabulary.normalize(value.toLowerCase());
    }

    public String normalize(String value, String fallBackValue) {
        String normalizedVals = normalize(value);
        return normalizedVals != null ? normalizedVals : fallBackValue;
    }

    @Override
    public Map<String, String> getCrossMappings(String value) {
        if (vocabulary == null) {
            initVocabulary();
        }

        return vocabulary.getCrossMappings(value);
    }

    public abstract String getNormalizationMapURL();

    private void initVocabulary() {
        VariantsMap varinatsRawMap = getMappingFromFile(getNormalizationMapURL());
        vocabulary = varinatsRawMap.getMap();
    }

    protected VariantsMap getMappingFromFile(String mapUrl) {

        LOG.info("Reading vocabulary file from: {}", mapUrl);

        InputStream is = PostProcessorsWithVocabularyMap.class.getClassLoader().getResourceAsStream(mapUrl);
        if (is == null) {
            throw new RuntimeException("Cannot instantiate postProcessor, " + mapUrl + " is not on the classpath");
        }

        try {
            return VariantsMapMarshaller.unmarshal(is);
        } catch (JAXBException ex) {
            throw new RuntimeException("Cannot instantiate postProcessor: ", ex);
        }

    }

    // for debug
    public static void printMap(PostProcessorsWithVocabularyMap processor) {
        LOG.info("map contains {} entries", processor.vocabulary.getEntries().length);
        for (VocabularyEntry entry : processor.vocabulary.getEntries()) {
            LOG.info(entry.toString());
        }

    }
}
