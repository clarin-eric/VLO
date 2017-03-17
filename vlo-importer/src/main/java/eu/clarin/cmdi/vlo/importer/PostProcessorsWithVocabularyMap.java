package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.MappingDefinitionResolver;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.normalization.NormalizationService;
import eu.clarin.cmdi.vlo.normalization.NormalizationVocabulary;
import eu.clarin.cmdi.vlo.normalization.VocabularyEntry;
import eu.clarin.cmdi.vlo.pojo.VariantsMap;
import eu.clarin.cmdi.vlo.transformers.VariantsMapMarshaller;
import java.io.IOException;
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

    private final MappingDefinitionResolver mappingDefinitionResolver = new MappingDefinitionResolver(PostProcessorsWithVocabularyMap.class);

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
        //TODO: cache unmarshalled map https://github.com/clarin-eric/VLO/issues/68
        LOG.info("Reading vocabulary file from: {}", mapUrl);

        final InputStream stream;
        try {
            stream = mappingDefinitionResolver.tryResolveUrlFileOrResourceStream(mapUrl);

            if (stream == null) {
                throw new RuntimeException("Cannot instantiate postProcessor, " + mapUrl + " is not an absolute URL, file path or packaged resource location");
            } else {
                try {
                    return VariantsMapMarshaller.unmarshal(stream);
                } catch (JAXBException ex) {
                    throw new RuntimeException("Cannot instantiate postProcessor: ", ex);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Cannot instantiate postProcessor, failed to read input stream for " + mapUrl);
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
