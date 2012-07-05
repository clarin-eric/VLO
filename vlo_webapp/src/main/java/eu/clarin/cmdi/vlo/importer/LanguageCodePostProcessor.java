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

import eu.clarin.cmdi.vlo.CommonUtils;
import eu.clarin.cmdi.vlo.Configuration;

public class LanguageCodePostProcessor implements PostProcessor{

    private final static Logger LOG = LoggerFactory.getLogger(LanguageCodePostProcessor.class);

    protected static final String ISO639_3_PREFIX = "ISO639-3:";
    protected static final String SIL_CODE_PREFIX = "RFC1766:x-sil-";
    protected static final String SIL_CODE_PREFIX_alt = "RFC-1766:x-sil-";

    private Map<String, String> twoLetterCodesMap;
    private Map<String, String> threeLetterCodesMap;
    private Map<String, String> silToIso639Map;
    private Map<String, String> languageNameToIso639Map;
    private Map<String, String> iso639ToLanguageNameMap;

    /**
     * Returns the language name based on the mapping defined in the CMDI components: See http://trac.clarin.eu/ticket/40 for the mapping.
     * If no mapping is found the original value is returned.
     */
    @Override
    public String process(String value) {
        String result = value;
        if (value != null) {
            String langCode = extractLanguageCode(value);
            if (langCode.length() == 2) {
                Map<String, String> twoLetterCodesMap = getTwoLetterCountryCodeMap();
                String name = twoLetterCodesMap.get(langCode.toUpperCase());
                if (name != null) {
                    result = name;
                }
            } else if (langCode.length() == 3) {
                Map<String, String> threeLetterCodesMap = getThreeLetterCountryCodeMap();
                String name = threeLetterCodesMap.get(langCode.toUpperCase());
                if (name != null) {
                    result = name;
                }
            }
        }
        return result;
    }

    protected String extractLanguageCode(String value) {
        String result = value;
        if (value.length() != 2 && value.length() != 3) {
            if (value.startsWith(ISO639_3_PREFIX)) {
                result = value.substring(ISO639_3_PREFIX.length());
            } else if (value.startsWith(SIL_CODE_PREFIX) || value.startsWith(SIL_CODE_PREFIX_alt)) {
                result = value.substring(value.lastIndexOf("-")+1);
                Map<String, String> silToISOMap = getSilToIso639Map();
                String isoCode = silToISOMap.get(result.toUpperCase());
                if (isoCode != null) {
                    result = isoCode;
                }
            }
        }
        // Convert to lowercase to capture erroneously capitalized language codes in the CMDI files.
        // NOTE: In the mappings themselves we do not capitalize.
        result = result.toLowerCase();
        return result;
    }

    public String getLanguageNameForLanguageCode(String langCode) {
    	String result = getIso639ToLanguageNameMap().get(langCode);

    	if(result == null)
    		result = langCode;

    	return result;
    }

    protected Map<String, String> getSilToIso639Map() {
        if (silToIso639Map == null) {
            silToIso639Map = createSilToIsoCodeMap();
        }
        return silToIso639Map;
    }

    private Map<String, String> getTwoLetterCountryCodeMap() {
        if (twoLetterCodesMap == null) {
            twoLetterCodesMap = createCodeMap(Configuration.getInstance().getLanguage2LetterCodeComponentUrl());
        }
        return twoLetterCodesMap;
    }

    private Map<String, String> getThreeLetterCountryCodeMap() {
        if (threeLetterCodesMap == null) {
            threeLetterCodesMap = createCodeMap(Configuration.getInstance().getLanguage3LetterCodeComponentUrl());
        }
        return threeLetterCodesMap;
    }

    protected Map<String, String> getLanguageNameToIso639Map() {
    	if (languageNameToIso639Map == null) {
    			languageNameToIso639Map = createReverseCodeMap(Configuration.getInstance().getLanguage3LetterCodeComponentUrl());
    	}
    	return languageNameToIso639Map;
    }

    private Map<String, String> getIso639ToLanguageNameMap() {
    	if (iso639ToLanguageNameMap == null) {
    		iso639ToLanguageNameMap = createCodeMap(Configuration.getInstance().getLanguage3LetterCodeComponentUrl());
    	}

    	return iso639ToLanguageNameMap;
    }

    private Map<String, String> createCodeMap(String url) {
        LOG.debug("Creating language code map.");
        try {
            Map<String, String> result = CommonUtils.createCMDIComponentItemMap(url);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate postProcessor:", e);
        }
    }

    private Map<String, String> createReverseCodeMap(String url) {
        LOG.debug("Creating language code map.");
        try {
            Map<String, String> result = CommonUtils.createReverseCMDIComponentItemMap(url);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate postProcessor:", e);
        }
    }

    private Map<String, String> createSilToIsoCodeMap() {
        LOG.debug("Creating silToIso code map.");
        try {
            Map<String, String> result = new HashMap<String, String>();
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            URL url = new URL(Configuration.getInstance().getSilToISO639CodesUrl());
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(url.openStream());
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xpath.evaluate("//lang", doc, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String silCode = node.getFirstChild().getTextContent();
                String isoCode = node.getLastChild().getTextContent();
                result.put(silCode.toUpperCase(), isoCode);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate postProcessor:", e);
        }
    }

}
