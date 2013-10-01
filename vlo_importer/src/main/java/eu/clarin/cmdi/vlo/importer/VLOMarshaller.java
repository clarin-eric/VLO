package eu.clarin.cmdi.vlo.importer;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class VLOMarshaller {
    private final static String FACETCONCEPTS_FILENAME = "facetConcepts.xml";

    public static FacetConceptMapping getFacetConceptMapping() {
        InputStream inputStream = VLOMarshaller.class.getResourceAsStream("/" + FACETCONCEPTS_FILENAME);
        return unmarshal(inputStream);

    }

    static FacetConceptMapping unmarshal(InputStream in) {
        try {
            JAXBContext jc = JAXBContext.newInstance(FacetConceptMapping.class);
            Unmarshaller u = jc.createUnmarshaller();
            FacetConceptMapping result = (FacetConceptMapping) u.unmarshal(in);
            result.check();
            return result;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    static String marshal(FacetConceptMapping f) {
        try {
            JAXBContext jc = JAXBContext.newInstance(FacetConceptMapping.class);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(f, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

}
