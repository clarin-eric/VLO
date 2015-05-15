package eu.clarin.cmdi.vlo.importer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VLOMarshaller {

    public static final String DEFAULT_FACET_CONCEPTS_FILE = "/facetConcepts.xml";
    private final static Logger logger = LoggerFactory.getLogger(VLOMarshaller.class);

    /**
     * Get facet concepts mapping from a facet concept mapping file
     *
     * @param facetConcepts name of the facet concepts file
     * @return the facet concept mapping
     */
    public static FacetConceptMapping getFacetConceptMapping(
            String facetConcepts) {

        if (facetConcepts == null || "".equals(facetConcepts)) {
            return unmarshal(VLOMarshaller.class.getResourceAsStream(DEFAULT_FACET_CONCEPTS_FILE));
        } else {
            try {
                return unmarshal(new FileInputStream(facetConcepts));
            } catch (FileNotFoundException ex) {
                logger.error("Could not find facets file: {}", facetConcepts);
                return null;
            }
        }
    }

    /**
     * Get object from input stream
     *
     * @param inputStream
     * @return
     */
    static FacetConceptMapping unmarshal(InputStream inputStream) {
        try {
            JAXBContext jc = JAXBContext.newInstance(FacetConceptMapping.class);
            Unmarshaller u = jc.createUnmarshaller();
            FacetConceptMapping result = (FacetConceptMapping) u.unmarshal(inputStream);
            result.check();
            return result;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Put facet mapping object in output file
     *
     * @param outputFile
     * @return
     */
    static String marshal(FacetConceptMapping outputFile) {
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
}
