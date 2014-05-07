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
    private Map<String, String> iso639_2TToISO639_3Map;

    /**
     * Returns the language code based on the mapping defined in the CMDI components: See http://trac.clarin.eu/ticket/40 for the mapping.
     * If no mapping is found the original value is returned.
     * @param value extracted language value (language code or language name) from CMDI file
     * @return ISO 639-3 code
     */
    @Override
    public String process(String value) {
        if (value != null)
            return extractLanguageCode(value);
        else
            return null;
    }

    protected String extractLanguageCode(String value) {
        String result = value;
        
        // deal with prefixes or language names
        if (value.length() != 2 && value.length() != 3) {
            if (value.startsWith(ISO639_3_PREFIX)) {
                return value.substring(ISO639_3_PREFIX.length()).toLowerCase();
            } else if (value.startsWith(SIL_CODE_PREFIX) || value.startsWith(SIL_CODE_PREFIX_alt)) {
                result = value.substring(value.lastIndexOf("-")+1);
                silToIso639Map = getSilToIso639Map();
                String isoCode = silToIso639Map.get(result.toLowerCase());
                if (isoCode != null) {
                    result = isoCode;
                }
            } else if(getLanguageNameToIso639Map().containsKey(value)) { // (english) language name?
                return getLanguageNameToIso639Map().get(value);
            }
        }
        
        // map 2-letter codes to ISO 639-3
        if(result.length() == 2) {
            if(silToIso639Map == null)
                silToIso639Map = getSilToIso639Map();
            result = silToIso639Map.get(result.toLowerCase());
        }
        
        // convert ISO 639-2/T codes to ISO 639-3
        if (getIso6392TToISO6393Map().containsKey(value.toLowerCase())) {
            result = getIso6392TToISO6393Map().get(value.toLowerCase());
        }
        
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

    protected Map<String, String> getTwoLetterCountryCodeMap() {
        if (twoLetterCodesMap == null) {
            twoLetterCodesMap = createCodeMap(MetadataImporter.config.getLanguage2LetterCodeComponentUrl());
        }
        return twoLetterCodesMap;
    }

    protected Map<String, String> getThreeLetterCountryCodeMap() {
        if (threeLetterCodesMap == null) {
            threeLetterCodesMap = createCodeMap(MetadataImporter.config.getLanguage3LetterCodeComponentUrl());
        }
        return threeLetterCodesMap;
    }

    protected Map<String, String> getLanguageNameToIso639Map() {
    	if (languageNameToIso639Map == null) {
    		languageNameToIso639Map = createReverseCodeMap(MetadataImporter.config.getLanguage3LetterCodeComponentUrl());
    	}
    	return languageNameToIso639Map;
    }

    private Map<String, String> getIso639ToLanguageNameMap() {
    	if (iso639ToLanguageNameMap == null) {
    		iso639ToLanguageNameMap = createCodeMap(MetadataImporter.config.getLanguage3LetterCodeComponentUrl());
    	}

    	return iso639ToLanguageNameMap;
    }
   
    /**
     * Returns map of ISO 639-2/B codes to ISO 639-3
     * 
     *  It is strongly advised to use ISO 639-3 codes, the support for ISO 639-2 may be discontinued in the future
     * 
     * @return map of ISO 639-2/B codes to ISO 639-3
     */
    private Map<String, String> getIso6392TToISO6393Map() {
        if (iso639_2TToISO639_3Map == null) {
            iso639_2TToISO639_3Map = new HashMap<String, String>();
            iso639_2TToISO639_3Map.put("alb", "sqi");
            iso639_2TToISO639_3Map.put("arm", "hye");
            iso639_2TToISO639_3Map.put("baq", "eus");
            iso639_2TToISO639_3Map.put("bur", "mya");
            iso639_2TToISO639_3Map.put("cze", "ces");
            iso639_2TToISO639_3Map.put("chi", "zho");
            iso639_2TToISO639_3Map.put("dut", "nld");
            iso639_2TToISO639_3Map.put("fre", "fra");
            iso639_2TToISO639_3Map.put("geo", "kat");
            iso639_2TToISO639_3Map.put("ger", "deu");
            iso639_2TToISO639_3Map.put("gre", "ell");
            iso639_2TToISO639_3Map.put("ice", "isl");
            iso639_2TToISO639_3Map.put("max", "mkd");
            iso639_2TToISO639_3Map.put("mao", "mri");
            iso639_2TToISO639_3Map.put("may", "msa");
            iso639_2TToISO639_3Map.put("per", "fas");
            iso639_2TToISO639_3Map.put("rum", "ron");
            iso639_2TToISO639_3Map.put("slo", "slk");
            iso639_2TToISO639_3Map.put("tib", "bod");
            iso639_2TToISO639_3Map.put("wel", "cym");
        }
        
        return iso639_2TToISO639_3Map;
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
            URL url = new URL(MetadataImporter.config.getSilToISO639CodesUrl());
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(url.openStream());
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xpath.evaluate("//lang", doc, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String silCode = node.getFirstChild().getTextContent();
                String isoCode = node.getLastChild().getTextContent();
                result.put(silCode, isoCode);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate postProcessor:", e);
        }
    }

}
