package eu.clarin.cmdi.vlo.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

import eu.clarin.cmdi.vlo.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMDIParserVTDXML implements CMDIDataProcessor {
    private final Map<String, PostProcessor> postProcessors;
    private final static Logger LOG = LoggerFactory.getLogger(CMDIParserVTDXML.class);


    public CMDIParserVTDXML(Map<String, PostProcessor> postProcessors) {
        this.postProcessors = postProcessors;
    }

    public CMDIData process(File file) throws VTDException, IOException {
        CMDIData result = new CMDIData();
        VTDGen vg = new VTDGen();
        vg.setDoc(IOUtils.toByteArray(new FileInputStream(file)));
        vg.parse(true);
        VTDNav nav = vg.getNav();
        setNameSpace(nav);//setting namespace once, all other instance of AutoPilot keep the setting (a bit tricky).
        FacetMapping facetMapping = getFacetMapping(nav.cloneNav(), file.getAbsolutePath());
        /** New nice error log to find erroneous files */
        if(facetMapping.getFacets().size() == 0){
            LOG.error("Problems mapping facets for file: " + file.getAbsolutePath());
        }

        nav.toElement(VTDNav.ROOT);
        processResources(result, nav);
        processFacets(result, nav, facetMapping);
        return result;
    }

    static void setNameSpace(VTDNav nav) {
        AutoPilot ap = new AutoPilot(nav);
        ap.declareXPathNameSpace("c", "http://www.clarin.eu/cmd/");
    }

    private FacetMapping getFacetMapping(VTDNav nav, String tolog) throws VTDException {
        String xsd = extractXsd(nav);
        if (xsd == null) {
            throw new RuntimeException("Cannot get xsd schema so cannot get a proper mapping. Parse failed!");
        }
        if (xsd.indexOf("http") != xsd.lastIndexOf("http")){
            System.out.println("FILE WITH WEIRD HTTP THINGY! " + tolog);
        }
        if(xsd.indexOf("p_1307535113335") > -1){
            System.out.println("FOUND a p_1307535113335 XSD: " + nav + " FILE: " + tolog);
        }
        return FacetMappingFactory.getFacetMapping(xsd);
    }

    String extractXsd(VTDNav nav) throws VTDException {
        String xsd = getXsdFromHeader(nav);
        if (xsd == null) {
            xsd = getXsdFromSchemaLocation(nav);
        }
        return xsd;
    }

    private String getXsdFromHeader(VTDNav nav) throws XPathParseException, XPathEvalException, NavException {
        String result = null;
        nav.toElement(VTDNav.ROOT);
        AutoPilot ap = new AutoPilot(nav);
        ap.selectXPath("/c:CMD/c:Header/c:MdProfile/text()");
        int index = ap.evalXPath();
        if (index != -1) {
            String profileId = nav.toString(index).trim();
            result = Configuration.getInstance().getComponentRegistryProfileSchema(profileId);
        }
        return result;
    }

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

    private void processResources(CMDIData result, VTDNav nav) throws VTDException {
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
            if (ref != "" && type != "") {
                result.addResource(ref, type, mimeType);
            }
        }
    }

    private void processFacets(CMDIData result, VTDNav nav, FacetMapping facetMapping) throws VTDException {
        List<FacetConfiguration> facetList = facetMapping.getFacets();
        for (FacetConfiguration config : facetList) {
            List<String> patterns = config.getPatterns();
            for (String pattern : patterns) {
                boolean matchedPattern = matchPattern(result, nav, config, pattern);
                if (matchedPattern && !config.getAllowMultipleValues()) {
                    break;
                }
            }
        }
    }

    private boolean matchPattern(CMDIData result, VTDNav nav, FacetConfiguration config, String pattern) throws VTDException {
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
            result.addDocField(config.getName(), value, config.isCaseInsensitive());
            index = ap.evalXPath();
        }
        return matchedPattern;
    }

    private String postProcess(String name, String value) {
        String result = value;
        if (postProcessors.containsKey(name)) {
            PostProcessor processor = postProcessors.get(name);
            result = processor.process(value);
        }
        return result;
    }

}
