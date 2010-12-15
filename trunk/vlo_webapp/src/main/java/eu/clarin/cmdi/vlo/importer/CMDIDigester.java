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
    }

    public CMDIData process(File file) throws IOException, SAXException, XPathExpressionException {
        CMDIData result = null;
        InputSource inputSource = new InputSource(new FileInputStream(file));
        inputSource.setSystemId(file.toString());
        XPath xpath = XPathFactory.newInstance().newXPath();
        result = createCMDIData(xpath, inputSource);
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
            matchDocumentField(result, facetConfiguration, doc, xpath);
        }
        return result;
    }

    private void matchDocumentField(CMDIData result, FacetConfiguration facetConfig, Document doc, XPath xpath)
            throws XPathExpressionException {
        NodeList nodes = (NodeList) xpath.evaluate(facetConfig.getPattern(), doc, XPathConstants.NODESET);
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                result.addDocField(facetConfig.getName(), nodes.item(i).getNodeValue(), facetConfig.isCaseInsensitive());
            }
        } // else do nothing it is perfectly acceptable that not all data is in a cmdi file so not everything will be matched. E.G xpath expression evaluation CMDI session files will never match on CMD corpus files.
    }
}
