package eu.clarin.cmdi.vlo.importer;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMDIParserVTDXML implements CMDIDataProcessor {
    private final Map<String, PostProcessor> postProcessors;
    private final static Logger LOG = LoggerFactory.getLogger(CMDIParserVTDXML.class);

    public CMDIParserVTDXML(Map<String, PostProcessor> postProcessors) {
        this.postProcessors = postProcessors;
    }

    @Override
    public CMDIData process(File file) throws VTDException, IOException {
        CMDIData cmdiData = new CMDIData();
        VTDGen vg = new VTDGen();
        FileInputStream fileInputStream = new FileInputStream(file); 
        vg.setDoc(IOUtils.toByteArray(fileInputStream));
        vg.parse(true);
        fileInputStream.close();
        
        VTDNav nav = vg.getNav();
        setNameSpace(nav); //setting namespace once, all other instance of AutoPilot keep the setting (a bit tricky).
        FacetMapping facetMapping = getFacetMapping(nav.cloneNav(), file.getAbsolutePath());

        if(facetMapping.getFacets().isEmpty()){
            LOG.error("Problems mapping facets for file: {}", file.getAbsolutePath());
        }

        nav.toElement(VTDNav.ROOT);
        processResources(cmdiData, nav);
        processFacets(cmdiData, nav, facetMapping);
        return cmdiData;
    }

    static void setNameSpace(VTDNav nav) {
        AutoPilot ap = new AutoPilot(nav);
        ap.declareXPathNameSpace("c", "http://www.clarin.eu/cmd/");
    }

    /**
     * Extracts valid XML patterns for all facet definitions
     * @param nav VTD Navigator
     * @param cmdiFilePath Absolute path of the XML file for which nav was created
     * @return the facet mapping used to map meta data to facets
     * @throws VTDException 
     */
    private FacetMapping getFacetMapping(VTDNav nav, String cmdiFilePath) throws VTDException {
        String xsd = extractXsd(nav);
        if (xsd == null) {
            throw new RuntimeException("Cannot get xsd schema so cannot get a proper mapping. Parse failed!");
        }
        if (xsd.indexOf("http") != xsd.lastIndexOf("http")){
            LOG.info("No valid CMDI schema URL was extracted. This is an indication of a broken CMDI file (like false content in //MdProfile element). {}", cmdiFilePath);
        }
        String facetConceptsFile = MetadataImporter.config.getFacetConceptsFile();
        if (facetConceptsFile.length() == 0){
            // use the packaged facet mapping file
            facetConceptsFile = "/facetConcepts.xml";
        }
        return FacetMappingFactory.getFacetMapping(facetConceptsFile, xsd);
    }

    /**
     * Try two approaches to extract the XSD schema information from the CMDI file
     * @param nav VTD Navigator
     * @return URL of CMDI schema, or null if neither the CMDI header nor the XMLSchema-instance's attributes contained the information
     * @throws VTDException 
     */
    String extractXsd(VTDNav nav) throws VTDException {
        String xsd = getXsdFromHeader(nav);
        if (xsd == null) {
            xsd = getXsdFromSchemaLocation(nav);
        }
        return xsd;
    }

    /**
     * Extract XSD schema information from CMDI header (using element //Header/MdProfile)
     * @param nav VTD Navigator
     * @return URL to CMDI schema, or null if content of //Header/MdProfile element could not be read
     * @throws XPathParseException
     * @throws XPathEvalException
     * @throws NavException 
     */
    private String getXsdFromHeader(VTDNav nav) throws XPathParseException, XPathEvalException, NavException {
        String result = null;
        nav.toElement(VTDNav.ROOT);
        AutoPilot ap = new AutoPilot(nav);
        ap.selectXPath("/c:CMD/c:Header/c:MdProfile/text()");
        int index = ap.evalXPath();
        if (index != -1) {
            String profileId = nav.toString(index).trim();
            result = MetadataImporter.config.getComponentRegistryProfileSchema(profileId);
        }
        return result;
    }

    /**
     * Extract XSD schema information from schemaLocation or noNamespaceSchemaLocation attributes
     * @param nav VTD Navigator
     * @return URL to CMDI schema, or null if attributes don't exist
     * @throws NavException 
     */
    private String getXsdFromSchemaLocation(VTDNav nav) throws NavException {
        String result = null;
        nav.toElement(VTDNav.ROOT);
        int index = nav.getAttrValNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
        if (index != -1) {
            String schemaLocation = nav.toNormalizedString(index);
            result = schemaLocation.split(" ")[1];
        } else {
            index = nav.getAttrValNS("http://www.w3.org/2001/XMLSchema-instance", "noNamespaceSchemaLocation");
            if (index != -1) {
                result = nav.toNormalizedString(index);
            }
        }
        return result;
    }
    
    /**
     * Extract ResourceProxies from ResourceProxyList
     * @param cmdiData representation of the CMDI document
     * @param nav VTD Navigator
     * @throws VTDException 
     */
    private void processResources(CMDIData cmdiData, VTDNav nav) throws VTDException {
        
        AutoPilot resourceProxy = new AutoPilot(nav);
        resourceProxy.selectXPath("/c:CMD/c:Resources/c:ResourceProxyList/c:ResourceProxy");
        
        AutoPilot resourceRef = new AutoPilot(nav);
        resourceRef.selectXPath("c:ResourceRef");
        AutoPilot resourceType = new AutoPilot(nav);
        resourceType.selectXPath("c:ResourceType");
        AutoPilot resourceMimeType = new AutoPilot(nav);
        resourceMimeType.selectXPath("c:ResourceType/@mimetype");
        
        while (resourceProxy.evalXPath() != -1) {
            String ref = resourceRef.evalXPathToString();
            String type = resourceType.evalXPathToString();
            String mimeType = resourceMimeType.evalXPathToString();
            
            if (!ref.equals("") && !type.equals("")) {
                // note that the mime type could be empty
                cmdiData.addResource(ref, type, mimeType);
            }
        }
    }

    /**
     * Extracts facet values according to the facetMapping
     * @param cmdiData representation of the CMDI document
     * @param nav VTD Navigator
     * @param facetMapping the facet mapping used to map meta data to facets
     * @throws VTDException 
     */
    private void processFacets(CMDIData cmdiData, VTDNav nav, FacetMapping facetMapping) throws VTDException {
        List<FacetConfiguration> facetList = facetMapping.getFacets();
        for (FacetConfiguration config : facetList) {
            List<String> patterns = config.getPatterns();
            for (String pattern : patterns) {
                boolean matchedPattern = matchPattern(cmdiData, nav, config, pattern, config.getAllowMultipleValues());
                if (matchedPattern && !config.getAllowMultipleValues()) {
                    break;
                }
            }
        }
    }

    /**
     * Extracts content from CMDI file for a specific facet based on a single XPath expression
     * @param cmdiData representation of the CMDI document
     * @param nav VTD Navigator
     * @param config facet configuration
     * @param pattern XPath expression
     * @param allowMultipleValues information if multiple values are allowed in this facet
     * @return pattern matched a node in the CMDI file?
     * @throws VTDException 
     */
    private boolean matchPattern(CMDIData cmdiData, VTDNav nav, FacetConfiguration config, String pattern, Boolean allowMultipleValues) throws VTDException {
        boolean matchedPattern = false;
        AutoPilot ap = new AutoPilot(nav);
        ap.selectXPath(pattern);
        int index = ap.evalXPath();
        while (index != -1) {
            matchedPattern = true;
            if (nav.getTokenType(index) == VTDNav.TOKEN_ATTR_NAME) {
                //if it is an attribute you need to add 1 to the index to get the right value
                index++;
            }
            String value = nav.toString(index);
            value = postProcess(config.getName(), value);
            cmdiData.addDocField(config.getName(), value, config.isCaseInsensitive());
            index = ap.evalXPath();
            
            if(!allowMultipleValues)
            	break;
        }
        return matchedPattern;
    }

    /**
     * Applies registered PostProcessor to extracted values
     * @param facetName name of the facet for which value was extracted
     * @param extractedValue extracted value from CMDI file
     * @return value after applying matching PostProcessor or the original value if no PostProcessor was registered for the facet 
     */
    private String postProcess(String facetName, String extractedValue) {
        String result = extractedValue;
        if (postProcessors.containsKey(facetName)) {
            PostProcessor processor = postProcessors.get(facetName);
            result = processor.process(extractedValue);
        }
        return result.trim();
    }
}
