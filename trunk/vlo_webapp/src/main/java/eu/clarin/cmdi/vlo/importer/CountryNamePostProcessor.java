package eu.clarin.cmdi.vlo.importer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.clarin.cmdi.vlo.Configuration;

public class CountryNamePostProcessor implements PostProcessor {
    
    private final static Logger LOG = LoggerFactory.getLogger(CountryNamePostProcessor.class);

    private Map<String, String> countryCodeMap;

    /**
     * Returns the country code based on the mapping defined in the CMDI component:
     * http://catalog.clarin.eu/ds/ComponentRegistry/?item=clarin.eu:cr1:c_1271859438104 If no mapping is found the original value is
     * returned.
     */
    @Override
    public String process(String value) {
        Map<String, String> countryCodeMap = getCountryCodeMap();
        String result = countryCodeMap.get(value.toUpperCase());
        if (result == null) {
            result = value;
        }
        return result;
    }

    private Map<String, String> getCountryCodeMap() {
        if (countryCodeMap == null) {
            countryCodeMap = createCountryCodeMap();
        }
        return countryCodeMap;
    }

    private Map<String, String> createCountryCodeMap() {
        LOG.debug("Creating country code map.");
        Map<String, String> result = new HashMap<String, String>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        try {
            URL url = new URL(Configuration.getInstance().getCountryComponentUrl());
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(url.openStream());
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xpath.evaluate("//item", doc, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String shortName = node.getTextContent();
                String longName = node.getAttributes().getNamedItem("AppInfo").getNodeValue();
                result.put(shortName.toUpperCase(), longName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate postProcessor:", e);
        }
        return result;
    }

}
