package eu.clarin.cmdi.vlo.importer;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.xml.sax.SAXException;

public class OrganisationPostProcessor implements PostProcessor {

    private static Map<String, String> organisationNamesMap = null;
    private final static Logger LOG = LoggerFactory.getLogger(OrganisationPostProcessor.class);

    /**
     * Splits values for organisation facet at delimiter ';' and replaces
     * organisation name variants with their official name from a controlled
     * vocabulary
     *
     * @param value extracted organisation name/names
     * @return List of organisation names (splitted at semicolon) and variations
     * replaced with controlled vocabulary
     */
    @Override
    public List<String> process(String value) {
        String[] splitArray = normalizeString(value).split(";");
        for (int i = 0; i < splitArray.length; i++) {
            String orgaName = splitArray[i];
            if (getNormalizedOrganisationNamesMap().containsKey(orgaName)) {
                splitArray[i] = getNormalizedOrganisationNamesMap().get(orgaName);
            }
        }
        
        return Arrays.asList(splitArray);
    }
    
    private String normalizeString(String value) {
        return value.replaceAll("\\s+", " ");
    }

    private Map<String, String> getNormalizedOrganisationNamesMap() {
        if (organisationNamesMap == null) {
            try {
                // load records from file, in the future this should be loaded from CLAVAS directly and the file only used as fallback
                organisationNamesMap = createControlledVocabularyMap(MetadataImporter.config.getOrganisationNamesUrl());
            } catch (Exception e) {
                throw new RuntimeException("Cannot instantiate postProcessor:", e);
            }
        }
        return organisationNamesMap;
    }

    private Map<String, String> createControlledVocabularyMap(String urlToVocabularyFile) throws ParserConfigurationException, MalformedURLException, SAXException, XPathExpressionException, IOException {
        Map<String, String> result = new HashMap<String, String>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);

        InputStream mappingFileAsStream;
        mappingFileAsStream = NationalProjectPostProcessor.class.getResourceAsStream(urlToVocabularyFile);

        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(mappingFileAsStream);
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xpath.evaluate("//Organisation", doc, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String organisationName = node.getAttributes().getNamedItem("name").getTextContent();
            NodeList childNodeList = node.getChildNodes();
            for (int j = 0; j < childNodeList.getLength(); j++) {
                String variation = childNodeList.item(j).getTextContent();
                result.put(variation, organisationName);
            }
        }
        return result;
    }
}
