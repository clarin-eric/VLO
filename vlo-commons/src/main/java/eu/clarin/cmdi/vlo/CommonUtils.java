package eu.clarin.cmdi.vlo;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

public final class CommonUtils {

    /**
     * Set to true to make run/import possible without a network connection
     * (todo: make environment variable?)
     */
    public static final Boolean SWALLOW_LOOKUP_ERRORS = false; //ONLY COMMIT AS FALSE!

    private final static Set<String> ANNOTATION_MIMETYPES = new HashSet<String>();

    static {
        ANNOTATION_MIMETYPES.add("text/x-eaf+xml");
        ANNOTATION_MIMETYPES.add("text/x-shoebox-text");
        ANNOTATION_MIMETYPES.add("text/x-toolbox-text");
        ANNOTATION_MIMETYPES.add("text/x-chat");
        ANNOTATION_MIMETYPES.add("text/x-chat");
        ANNOTATION_MIMETYPES.add("application/mediatagger");
        ANNOTATION_MIMETYPES.add("mt");
        ANNOTATION_MIMETYPES.add("application/smil+xml");
    }
    private final static Set<String> TEXT_MIMETYPES = new HashSet<String>();

    static {
        TEXT_MIMETYPES.add("application/pdf");
        TEXT_MIMETYPES.add("txt");
    }
    private final static Set<String> VIDEO_MIMETYPES = new HashSet<String>();

    static {
        VIDEO_MIMETYPES.add("application/mxf");
    }
    private final static Set<String> AUDIO_MIMETYPES = new HashSet<String>();

    static {
        AUDIO_MIMETYPES.add("application/ogg");
        AUDIO_MIMETYPES.add("wav");
    }

    private CommonUtils() {
    }

    public static String normalizeMimeType(String mimeType) {
        String type = mimeType;
        if (type != null) {
            type = type.toLowerCase();
        } else {
            type = "";
        }
        String result = "unknown type";
        if (ANNOTATION_MIMETYPES.contains(type)) {
            result = FacetConstants.RESOURCE_TYPE_ANNOTATION;
        } else if (type.startsWith("audio") || AUDIO_MIMETYPES.contains(type)) {
            result = FacetConstants.RESOURCE_TYPE_AUDIO;
        } else if (type.startsWith("video") || VIDEO_MIMETYPES.contains(type)) {
            result = FacetConstants.RESOURCE_TYPE_VIDEO;
        } else if (type.startsWith("image")) {
            result = FacetConstants.RESOURCE_TYPE_IMAGE;
        } else if (type.startsWith("audio")) {
            result = FacetConstants.RESOURCE_TYPE_AUDIO;
        } else if (type.startsWith("text") || TEXT_MIMETYPES.contains(type)) {
            result = FacetConstants.RESOURCE_TYPE_TEXT;
        }
        return result;
    }

    /**
     * Create a mapping out of simple CMDI components for instance: lists of
     * items: <item AppInfo="Tigrinya (ti)">ti</item> Will become key (after
     * removal of trailing 2 or 3 letter codes), values: ti, Tigrinya
     *
     * @param urlToComponent
     * @return Map with item_value, AppInfo_value pairs
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static Map<String, String> createCMDIComponentItemMap(String urlToComponent) throws XPathExpressionException, SAXException,
            IOException, ParserConfigurationException {
        Map<String, String> result = new HashMap<String, String>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        URL url = new URL(urlToComponent);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(url.openStream());
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xpath.evaluate("//item", doc, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String shortName = node.getTextContent();
            String longName = node.getAttributes().getNamedItem("AppInfo").getNodeValue().replaceAll(" \\([a-zA-Z]+\\)$", "");
            result.put(shortName.toUpperCase(), longName);
        }
        return result;
    }

    /**
     * Create a mapping out of simple CMDI components for instance: lists of
     * items: <item AppInfo="Tigrinya">ti</item> Will become key (after removal
     * of trailing 2 or 3 letter codes), values: Tigrinya, ti
     *
     * @param urlToComponent
     * @return Map with item_value, AppInfo_value pairs
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static Map<String, String> createReverseCMDIComponentItemMap(String urlToComponent) throws XPathExpressionException, SAXException,
            IOException, ParserConfigurationException {
        Map<String, String> result = new HashMap<String, String>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        URL url = new URL(urlToComponent);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(url.openStream());
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xpath.evaluate("//item", doc, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String shortName = node.getTextContent();
            String longName = node.getAttributes().getNamedItem("AppInfo").getNodeValue().replaceAll(" \\([a-zA-Z]+\\)$", "");
            result.put(longName, shortName);
        }
        return result;
    }

}
