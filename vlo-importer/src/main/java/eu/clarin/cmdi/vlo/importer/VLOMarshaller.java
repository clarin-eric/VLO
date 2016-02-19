package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.config.VloConfig;

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

    private final static Logger logger = LoggerFactory.getLogger(VLOMarshaller.class);

    /**
     * Get facet concepts mapping from a facet concept mapping file
     *
     * @param facetConcepts name of the facet concepts file
     * @return the facet concept mapping
     */
    public static FacetConceptMapping getFacetConceptMapping(String facetConcepts) {
    	
    	FacetConceptMapping result;
    	InputStream is = null;

    	try {
    		is = (facetConcepts == null || "".equals(facetConcepts))?
    			VLOMarshaller.class.getResourceAsStream(VloConfig.DEFAULT_FACET_CONCEPTS_RESOURCE_FILE) :
    			new FileInputStream(facetConcepts);
        } catch (FileNotFoundException e) {
                logger.error("Could not find facets file: {}", facetConcepts);
                return null;
        }
    	
    	return unmarshal(is);
    }
    

    /**
     * Get object from input stream
     *
     * @param inputStream
     * @return
     */
    static FacetConceptMapping unmarshal(InputStream inputStream) {
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
