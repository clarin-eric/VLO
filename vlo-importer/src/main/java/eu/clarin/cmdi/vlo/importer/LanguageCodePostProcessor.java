package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class LanguageCodePostProcessor implements PostProcessor{

    private final static Logger LOG = LoggerFactory.getLogger(LanguageCodePostProcessor.class);
    
    private static Map<String, String> languageNameVariantsMap = null;

    protected static final String CODE_PREFIX = "code:";
    protected static final String LANG_NAME_PREFIX = "name:";
    protected static final String ISO639_2_PREFIX = "ISO639-2:";
    protected static final String ISO639_3_PREFIX = "ISO639-3:";
    protected static final String SIL_CODE_PREFIX = "RFC1766:x-sil-";
    protected static final String SIL_CODE_PREFIX_alt = "RFC-1766:x-sil-";
    
    private static final Pattern RFC1766_Pattern = Pattern.compile("^([a-z]{2,3})[-_][a-zA-Z]{2}$");

    /**
     * Returns the language code based on the mapping defined in the CMDI components: See http://trac.clarin.eu/ticket/40 for the mapping.
     * If no mapping is found the original value is returned.
     * @param value extracted language value (language code or language name) from CMDI file
     * @return ISO 639-3 code
     */
    @Override
    public List<String> process(String value) {
        List<String> resultList = new ArrayList<String>();
        
        if (value != null)
            resultList.add(extractLanguageCode(value));
        else
            resultList.add(null);
        return resultList;
    }

    protected String extractLanguageCode(String value) {
        final LanguageCodeUtils languageCodeUtils = MetadataImporter.languageCodeUtils;
        String result = value;
        
        result = result.replaceFirst(ISO639_2_PREFIX, "").replaceFirst(ISO639_3_PREFIX, "").replaceFirst(SIL_CODE_PREFIX, "").replaceFirst(SIL_CODE_PREFIX_alt, "");
        
        // map known language name variants to their offical name
        if(getLanguageNamesVariantsMap().containsKey(result))
            result = getLanguageNamesVariantsMap().get(result);
        
        // input is already ISO 639-3?
        if(languageCodeUtils.getIso639ToLanguageNameMap().keySet().contains(result.toUpperCase())) {
            return CODE_PREFIX + result.toLowerCase();
        }        
        // input is 2-letter code -> map to ISO 639-3
        if(languageCodeUtils.getSilToIso639Map().containsKey(result.toLowerCase())) {
            return CODE_PREFIX + languageCodeUtils.getSilToIso639Map().get(result.toLowerCase());
        }

        if(languageCodeUtils.getLanguageNameToIso639Map().containsKey(result)) { // (english) language name?
            return CODE_PREFIX + languageCodeUtils.getLanguageNameToIso639Map().get(result);
        }

        // convert ISO 639-2/T codes to ISO 639-3
        if (languageCodeUtils.getIso6392TToISO6393Map().containsKey(result.toLowerCase())) {
            return CODE_PREFIX + languageCodeUtils.getIso6392TToISO6393Map().get(result.toLowerCase());
        }
        
        Matcher matcher = RFC1766_Pattern.matcher(result);
        if(matcher.find()) {
            return extractLanguageCode(matcher.group(1));
        }
            
        // language code not identified? -> language name
        if(!result.equals(""))
            result = LANG_NAME_PREFIX + result;
        return result;
    }
    
        private Map<String, String> getLanguageNamesVariantsMap() {
        if (languageNameVariantsMap == null) {
            try {
                // load records from file, in the future this should be loaded from CLAVAS directly and the file only used as fallback
                languageNameVariantsMap = createControlledVocabularyMap(MetadataImporter.config.getLanguageNameVariantsUrl());
            } catch (Exception e) {
                throw new RuntimeException("Cannot instantiate postProcessor:", e);
            }
        }
        return languageNameVariantsMap;
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
        NodeList nodeList = (NodeList) xpath.evaluate("//Language", doc, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String languageName = node.getAttributes().getNamedItem("name").getTextContent();
            NodeList childNodeList = node.getChildNodes();
            for (int j = 0; j < childNodeList.getLength(); j++) {
                String variation = childNodeList.item(j).getTextContent();
                result.put(variation, languageName);
            }
        }
        return result;
    }
}
