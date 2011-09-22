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
import eu.clarin.cmdi.vlo.FacetConstants;

public class LanguageLinkPostProcessor extends LanguageCodePostProcessor {

    private final static Logger LOG = LoggerFactory.getLogger(LanguageLinkPostProcessor.class);


    /**
     * Returns the lanaguage name based on the mapping defined in the CMDI components: See http://trac.clarin.eu/ticket/40 for the mapping.
     * If no mapping is found the original value is returned.
     */
    @Override
    public String process(String value) {
        String result = value;
        if (value != null) {
            String langCode = extractLanguageCode(value);
            result = FacetConstants.LANGUAGE_LINK_PREFIX + langCode;
        }
        return result;
    }
}
