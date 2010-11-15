package eu.clarin.cmdi.vlo.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class CMDIDigester {

    private final FacetMapping facetMapping;
    private XMLReader xmlReader;

    public CMDIDigester(FacetMapping facetMapping) {
        this.facetMapping = facetMapping;
        try {
            xmlReader = createXmlReader();
        } catch (SAXException e) {
            throw new RuntimeException("Cannot instantiate xmlReader:", e);
        }
    }

    public CMDIData process(File file) throws IOException, SAXException {
        CMDIData result = null;
        InputSource inputSource = new InputSource(new FileInputStream(file));
        inputSource.setSystemId(file.toString());

        /**
         * Do not reuse the digester it holds state on bad parses. We can reuse the xmlReader. Creating a new Digester or reusing an
         * instance gives similar performance.
         * @see org.apache.commons.digester.Digester
         */
        result = (CMDIData) createDigester().parse(inputSource);
        return result;
    }

    private Digester createDigester() {
        Digester digester = new Digester(xmlReader);
        digester.setValidating(false);
        digester.addObjectCreate("CMD", CMDIData.class);
        digester.addBeanPropertySetter(facetMapping.getIdMapping(), "id");
        digester.addCallMethod("CMD/Resources/ResourceProxyList/ResourceProxy/", "addResource", 2);
        digester.addCallParam("CMD/Resources/ResourceProxyList/ResourceProxy/ResourceRef", 0);
        digester.addCallParam("CMD/Resources/ResourceProxyList/ResourceProxy/ResourceType", 1);
        Map<String, String> facetMap = facetMapping.getFacetMap();
        for (String facet : facetMap.keySet()) {
            matchDocumentField(digester, facetMap.get(facet), facet);
        }
        return digester;
    }

    private void matchDocumentField(Digester digester, String pattern, String fieldName) {
        String[] split = pattern.split(",@", 2);
        String path = split[0];
        String attribute = split.length == 2 ? split[1] : null;
        digester.addCallMethod(path, "addDocField", 2);
        digester.addObjectParam(path, 0, fieldName);
        digester.addCallParam(path, 1, attribute);
    }

    private XMLReader createXmlReader() throws SAXException {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setFeature("http://xml.org/sax/features/validation", true);
        xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
        xmlReader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        return xmlReader;
    }

}
