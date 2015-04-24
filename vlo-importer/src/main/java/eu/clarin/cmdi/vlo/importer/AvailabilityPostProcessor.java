package eu.clarin.cmdi.vlo.importer;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author teckart
 */
public class AvailabilityPostProcessor implements PostProcessor {

    private static Map<String, String> availabilityMap;
    private static final Integer MAX_LENGTH = 20;
    private static final String OTHER_VALUE = "Other";

    @Override
    public List<String> process(final String value) {
        String result = value;
        List<String> resultList = new ArrayList<String>();

        if (getLicenseAvailabilityMap().containsKey(value.toLowerCase())) {
            resultList.add(getLicenseAvailabilityMap().get(value.toLowerCase()));
        } else {
            if (result.length() > MAX_LENGTH) {
                resultList.add(OTHER_VALUE);
            } else {
                resultList.add(result.trim());
            }
        }

        return resultList;
    }

    private Map<String, String> getLicenseAvailabilityMap() {
        if (availabilityMap == null) {
            try {
                // load records from file
                availabilityMap = createControlledVocabularyMap(MetadataImporter.config.getLicenseAvailabilityMapUrl());
            } catch (Exception e) {
                throw new RuntimeException("Cannot instantiate postProcessor:", e);
            }
        }
        return availabilityMap;
    }

    private Map<String, String> createControlledVocabularyMap(String languageNamesUrl) throws ParserConfigurationException, MalformedURLException, SAXException, XPathExpressionException, IOException {
        Map<String, String> result = new HashMap<String, String>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);

        InputStream mappingFileAsStream;
        mappingFileAsStream = NationalProjectPostProcessor.class.getResourceAsStream(languageNamesUrl);

        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(mappingFileAsStream);
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xpath.evaluate("//Availability", doc, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String availabilityName = node.getAttributes().getNamedItem("name").getTextContent();
            NodeList childNodeList = node.getChildNodes();
            for (int j = 0; j < childNodeList.getLength(); j++) {
                String license = childNodeList.item(j).getTextContent().toLowerCase();
                result.put(license, availabilityName);
            }
        }
        return result;
    }
}
