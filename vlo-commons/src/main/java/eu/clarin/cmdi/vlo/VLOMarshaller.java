package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.mapping.FacetConceptMapping;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class VLOMarshaller {

    /**
     * Get facet concepts mapping from a facet concept mapping file
     * 
     * @param facetConcepts name of the facet concepts file
     * @return the facet concept mapping
     */
    public static FacetConceptMapping getFacetConceptMapping(
            String facetConcepts) {

        InputStream inputStream =
                VLOMarshaller.class.getResourceAsStream(facetConcepts);
        
        return unmarshal(inputStream);
    }

    /**
     * Get object from input stream
     * 
     * @param inputStream
     * @return 
     */
    public static FacetConceptMapping unmarshal(InputStream inputStream) {
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
    public static String marshal(FacetConceptMapping outputFile) {
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
