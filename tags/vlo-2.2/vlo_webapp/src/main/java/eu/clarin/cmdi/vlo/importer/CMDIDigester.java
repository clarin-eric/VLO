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

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @deprecated Dom parsing implementation, use the @see CMDIParserVTDXML it is much faster. Keeping this for now just in case we run into
 *             issues with the vlt parsing. patdui 15 December 2010
 */
public class CMDIDigester implements CMDIDataProcessor {
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
            Node nodeType = (Node) xpath.evaluate("ResourceType", resourceNode, XPathConstants.NODE);
            if (ref != null && nodeType != null) {
                String mimeType = null;
                NamedNodeMap attributes = nodeType.getAttributes();
                if (attributes != null) {
                    Node n = attributes.getNamedItem("mimetype");
                    if (n != null) {
                        mimeType = n.getNodeValue();
                    }
                }
                String type = nodeType.getTextContent();
                result.addResource(ref.getNodeValue(), type, mimeType);
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
        List<String> patterns = facetConfig.getPatterns();
        for (String pattern : patterns) {
            boolean matchedPattern = matchPattern(result, facetConfig, doc, xpath, pattern);
            if (matchedPattern) {
                break;
            }
        }
    }

    private boolean matchPattern(CMDIData result, FacetConfiguration facetConfig, Document doc, XPath xpath, String pattern)
            throws XPathExpressionException {
        boolean matchedPattern = false;
        NodeList nodes = (NodeList) xpath.evaluate(pattern, doc, XPathConstants.NODESET);
        if (nodes != null) {
            matchedPattern = true;
            for (int i = 0; i < nodes.getLength(); i++) {
                result.addDocField(facetConfig.getName(), nodes.item(i).getNodeValue(), facetConfig.isCaseInsensitive());
            }
        } // else do nothing it is perfectly acceptable that not all data is in a cmdi file so not everything will be matched. E.G xpath expression evaluation CMDI session files will never match on CMD corpus files.
        return matchedPattern;
    }
}
