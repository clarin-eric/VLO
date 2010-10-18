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

    private Digester digester;
    private final FacetMapping facetMapping;

    public CMDIDigester(FacetMapping facetMapping) {
        this.facetMapping = facetMapping;
        try {
            digester = createDigester();
        } catch (SAXException e) {
            throw new RuntimeException("Cannot instantiate Digester", e);
        }
    }

    public CMDIData process(File file) throws IOException, SAXException {
        CMDIData result = null;
        InputSource inputSource = new InputSource(new FileInputStream(file));
        inputSource.setSystemId(file.toString());
        result = (CMDIData) digester.parse(inputSource);
        return result;
    }

    private Digester createDigester() throws SAXException {
        Digester digester = new Digester(createXmlReader());
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
        digester.addCallMethod(pattern, "addDocField", 2);
        digester.addObjectParam(pattern, 0, fieldName);
        digester.addCallParam(pattern, 1);
    }

    private XMLReader createXmlReader() throws SAXException {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setFeature("http://xml.org/sax/features/validation", true);
        xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
        xmlReader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        return xmlReader;
    }

}
