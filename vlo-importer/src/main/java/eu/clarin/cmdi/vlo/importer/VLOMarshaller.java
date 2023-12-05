package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.MappingDefinitionResolver;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.facets.FacetsConfigurationsMarshaller;
import eu.clarin.cmdi.vlo.facets.configuration.FacetsConfiguration;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class VLOMarshaller {

    private final static Logger logger = LoggerFactory.getLogger(VLOMarshaller.class);

    private final Map<String, FacetConceptMapping> mappingCache = new ConcurrentHashMap<>();

    public VLOMarshaller() {
    }

    /**
     * Get facet concepts mapping from a facet concept mapping file.
     * Unmarshalled mappings are cached statically for this class.
     *
     * @param facetConcepts name of the facet concepts file
     * @return the facet concept mapping
     */
    public final FacetConceptMapping getFacetConceptMapping(String facetConcepts) {
        return mappingCache.computeIfAbsent(facetConcepts, (key) -> {
            // unmarshall map for file
            final MappingDefinitionResolver mappingDefinitionResolver
                    = new MappingDefinitionResolver(VLOMarshaller.class);

            FacetConceptMapping result;
            InputStream is = null;

            try {
                if (facetConcepts == null || "".equals(facetConcepts)) {
                    is = VLOMarshaller.class.getResourceAsStream(VloConfig.DEFAULT_FACET_CONCEPTS_RESOURCE_FILE);
                } else {
                    final InputSource resolvedStream = mappingDefinitionResolver.tryResolveUrlFileOrResourceStream(facetConcepts);
                    is = (resolvedStream == null) ? null : resolvedStream.getByteStream();
                }
            } catch (FileNotFoundException e) {
                logger.error("Could not find facets file: {}", facetConcepts);
                return null;
            } catch (IOException e) {
                logger.error("Could not process facets file: {}", facetConcepts);
                return null;
            }
            logger.info("Unmarshalling facet concepts definition from file {}", facetConcepts);
            return unmarshal(is);
        });
    }

    /**
     * Get object from input stream
     *
     * @param inputStream
     * @return
     */
    public final FacetConceptMapping unmarshal(InputStream inputStream) {
        FacetConceptMapping result;

        try {
            JAXBContext jc = JAXBContext.newInstance(FacetConceptMapping.class);
            Unmarshaller u = jc.createUnmarshaller();
            result = (FacetConceptMapping) u.unmarshal(inputStream);
        } catch (JAXBException e) {
            throw new RuntimeException();
        }

        result.check();
        return result;
    }

    /**
     * Put facet mapping object in output file
     *
     * @param outputFile
     * @return
     */
    public final String marshal(FacetConceptMapping outputFile) {
        try {
            JAXBContext jc = JAXBContext.newInstance(FacetConceptMapping.class);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(outputFile, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public FacetsConfiguration getFacetsConfiguration(String facetsConfigFile) {

        final MappingDefinitionResolver mappingDefinitionResolver
                = new MappingDefinitionResolver(VLOMarshaller.class);

        try {
            final InputStream is;
            if (facetsConfigFile == null || "".equals(facetsConfigFile)) {
                is = VLOMarshaller.class.getResourceAsStream(VloConfig.DEFAULT_FACETS_CONFIG_RESOURCE_FILE);
            } else {
                final InputSource resolvedStream = mappingDefinitionResolver.tryResolveUrlFileOrResourceStream(facetsConfigFile);
                is = (resolvedStream == null) ? null : resolvedStream.getByteStream();
            }

            try {
                logger.info("Unmarshalling facet concepts definition from file {}", facetsConfigFile);
                final FacetsConfigurationsMarshaller facetsConfigurationsMarshaller = new FacetsConfigurationsMarshaller();
                return facetsConfigurationsMarshaller.unmarshal(new StreamSource(is));
            } finally {
                if (is != null) {
                    is.close();
                }
            }

        } catch (FileNotFoundException e) {
            logger.error("Could not find facets configuration file: {}", facetsConfigFile);
            return null;
        } catch (JAXBException | IOException e) {
            logger.error("Could not process facets configuration file: {}", facetsConfigFile, e);
            return null;
        }

    }
}
