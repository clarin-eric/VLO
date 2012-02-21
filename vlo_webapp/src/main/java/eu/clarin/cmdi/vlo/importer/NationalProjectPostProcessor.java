package eu.clarin.cmdi.vlo.importer;

import java.io.File;
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
import org.w3c.dom.NodeList;

/**
 * Adds information about the affiliation of a metadata file to a national project (like CLARIN-X etc.) into facet nationalProject
 * @author Thomas Eckart
 *
 */
public class NationalProjectPostProcessor extends LanguageCodePostProcessor {
	private final static Logger LOG = LoggerFactory.getLogger(NationalProjectPostProcessor.class);
	
	private static Map<String, String> nationalProjectMap = null;
	
	/**
	 * Returns the national project based on the mapping in Configuration.getNationalProjectMapUrl()
	 * If no mapping was found empty String is returned
	 * @return 
	 */
	@Override
    public String process(String value) {
        String result = value.trim();
        if (result != null && getMapping().containsKey(result)) {
            result = getMapping().get(result);
        } else {
        	result = "";
        }
        return result;
    }
	
	private Map<String, String> getMapping() {
		if(nationalProjectMap == null)
			nationalProjectMap = getNationalProjectMapping();
		return  nationalProjectMap;
	}
	
	private Map<String, String> getNationalProjectMapping() {
		LOG.debug("Creating national project map.");
        try {
            Map<String, String> result = new HashMap<String, String>();
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);

            File mappingFile = new File("nationalProjectsMapping.xml");
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(mappingFile);
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xpath.evaluate("//nationalProjectMapping", doc, XPathConstants.NODESET);
            for (int i = 1; i <= nodeList.getLength(); i++) {
                String mdCollectionDisplayName = xpath.evaluate("//nationalProjectMapping["+i+"]/MdCollectionDisplayName", doc).trim();
                String nationalProject = xpath.evaluate("//nationalProjectMapping["+i+"]/NationalProject", doc).trim();
                result.put(mdCollectionDisplayName, nationalProject);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate postProcessor:", e);
        }
	}
}
