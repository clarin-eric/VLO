package eu.clarin.cmdi.vlo.transformers;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.pojo.VariantsMap;

public class VariantsMapMarshaller {

    private final static Logger LOG = LoggerFactory.getLogger(VariantsMapMarshaller.class);

    public static VariantsMap unmarshal(InputStream input) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(VariantsMap.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (VariantsMap) unmarshaller.unmarshal(input);

    }

    public static void marshal(VariantsMap map, OutputStream output) throws JAXBException, UnsupportedEncodingException {
        //Writer out = new OutputStreamWriter(output, StandardCharsets.UTF_8);

        JAXBContext jc = JAXBContext.newInstance(VariantsMap.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(map, output);
    }

}
