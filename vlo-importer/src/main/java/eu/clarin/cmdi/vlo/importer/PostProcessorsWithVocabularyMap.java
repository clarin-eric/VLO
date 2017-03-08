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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
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

        final InputStream stream;
        try {
            //first try as absolute URL
            final InputStream urlStream = getUrlStream(mapUrl);
            if (urlStream != null) {
                stream = urlStream;
            } else {
                //not an absolute URL try absolute file path
                final InputStream fileStream = getFileStream(mapUrl);
                if (fileStream != null) {
                    stream = fileStream;
                } else {
                    //not an absolute file path - try resource
                    stream = getResourceStream(mapUrl);
                }
            }

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

    private InputStream getUrlStream(String potentialUrl) throws IOException {
        try {
            LOG.info("Testing URL {}", potentialUrl);
            final URL url = new URL(potentialUrl);
            if (url.toURI().isAbsolute()) {
                return url.openStream();
            }
        } catch (MalformedURLException | URISyntaxException ex) {
            LOG.debug("Not a valid vocabulary URL / URI: {}", potentialUrl);
        }
        //conditions not met - not a valid absolute URL
        return null;
    }

    private InputStream getFileStream(String potentialPath) {
        final File file = new File(potentialPath);
        try {
            if (file.isAbsolute()) {
                return new FileInputStream(file);
            } else {
                LOG.debug("Not an absolute file path: {}", potentialPath);
            }
        } catch (FileNotFoundException ex) {
            LOG.debug("Not a local file that exists: {}", potentialPath);
        }
        //conditions not met - not a valid absolute path
        return null;
    }

    private InputStream getResourceStream(String mapUrl) {
        return PostProcessorsWithVocabularyMap.class.getClassLoader().getResourceAsStream(mapUrl);
    }

    // for debug
    public static void printMap(PostProcessorsWithVocabularyMap processor) {
        LOG.info("map contains {} entries", processor.vocabulary.getEntries().length);
        for (VocabularyEntry entry : processor.vocabulary.getEntries()) {
            LOG.info(entry.toString());
        }

    }
}
