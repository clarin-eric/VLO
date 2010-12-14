package eu.clarin.cmdi.vlo.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CMDIDigester {
    private final static Logger LOG = LoggerFactory.getLogger(CMDIDigester.class);
    private final FacetMapping facetMapping;
    //    private XMLReader xmlReader;
    private DocumentBuilder builder;

    public CMDIDigester(FacetMapping facetMapping) {
        this.facetMapping = facetMapping;
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        try {
            builder = domFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Cannot instantiate documentBuilder:", e);
        }
        //        try {
        //            xmlReader = createXmlReader();
        //        } catch (SAXException e) {
        //            throw new RuntimeException("Cannot instantiate xmlReader:", e);
        //        }
    }

    public CMDIData process(File file) throws IOException, SAXException, XPathExpressionException {
        CMDIData result = null;
        InputSource inputSource = new InputSource(new FileInputStream(file));
        inputSource.setSystemId(file.toString());
        XPath xpath = XPathFactory.newInstance().newXPath();
        result = createCMDIData(xpath, inputSource);

        /**
         * Do not reuse the digester it holds state on bad parses. We can reuse the xmlReader. Creating a new Digester or reusing an
         * instance gives similar performance.
         * @see org.apache.commons.digester.Digester
         */
        //result = (CMDIData) createDigester().parse(inputSource);
        return result;
    }

    private CMDIData createCMDIData(XPath xpath, InputSource inputSource) throws XPathExpressionException, SAXException, IOException {
        CMDIData result = new CMDIData();
        Document doc = builder.parse(inputSource);
        Node node = (Node) xpath.evaluate(facetMapping.getIdMapping(), doc, XPathConstants.NODE);
        if (node != null) {
            result.setId(node.getNodeValue());
        }
        NodeList nodes = (NodeList) xpath.evaluate("CMD/Resources/ResourceProxyList/ResourceProxy", doc, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node resourceNode = nodes.item(i);
            Node ref = (Node) xpath.evaluate("ResourceRef/text()", resourceNode, XPathConstants.NODE);
            Node type = (Node) xpath.evaluate("ResourceType/text()", resourceNode, XPathConstants.NODE);
            if (ref != null && type != null) {
                result.addResource(ref.getNodeValue(), type.getNodeValue());
            }
        }
        List<FacetConfiguration> facetList = facetMapping.getFacets();
        for (FacetConfiguration facetConfiguration : facetList) {
            matchDocumentField(result, facetConfiguration.getPattern(), facetConfiguration.getName(), doc, xpath);
        }
        return result;
    }

    private void matchDocumentField(CMDIData result, String pattern, String fieldName, Document doc, XPath xpath)
            throws XPathExpressionException {
        NodeList nodes = (NodeList) xpath.evaluate(pattern, doc, XPathConstants.NODESET);
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                result.addDocField(fieldName, nodes.item(i).getNodeValue());
            }
        } // else do nothing it is perfectly acceptable that not all data is in a cmdi file so not everything will be matched. E.G xpath expression evaluation CMDI session files will never match on CMD corpus files.
    }

    //    private Digester createDigester() {
    //        Digester digester = new Digester(xmlReader);
    //        digester.setValidating(false);
    //        digester.addObjectCreate("CMD", CMDIData.class);
    //        digester.addBeanPropertySetter(facetMapping.getIdMapping(), "id");
    //        digester.addCallMethod("CMD/Resources/ResourceProxyList/ResourceProxy/", "addResource", 2);
    //        digester.addCallParam("CMD/Resources/ResourceProxyList/ResourceProxy/ResourceRef", 0);
    //        digester.addCallParam("CMD/Resources/ResourceProxyList/ResourceProxy/ResourceType", 1);
    //        //        Map<String, String> facetMap = facetMapping.getFacetMap();
    //        //        for (String facet : facetMap.keySet()) {
    //        //            matchDocumentField(digester, facetMap.get(facet), facet);
    //        //        }
    //        return digester;
    //    }
    //
    //    private void matchDocumentField(Digester digester, String pattern, String fieldName) {
    //        String[] split = pattern.split(",@", 2);
    //        String path = split[0];
    //        String attribute = split.length == 2 ? split[1] : null;
    //        digester.addCallMethod(path, "addDocField", 2);
    //        digester.addObjectParam(path, 0, fieldName);
    //        digester.addCallParam(path, 1, attribute);
    //    }
    //
    //    private XMLReader createXmlReader() throws SAXException {
    //        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
    //        xmlReader.setFeature("http://xml.org/sax/features/validation", true);
    //        xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
    //        xmlReader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
    //        return xmlReader;
    //    }

}
