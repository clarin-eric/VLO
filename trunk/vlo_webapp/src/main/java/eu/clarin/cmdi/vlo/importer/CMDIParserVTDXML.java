package eu.clarin.cmdi.vlo.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
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

public class CMDIParserVTDXML implements CMDIDataProcessor {
    private final Map<String, PostProcessor> postProcessors;
    private Map<String, FacetMapping> facetMaps = new HashMap<String, FacetMapping>();

    public CMDIParserVTDXML( Map<String, PostProcessor> postProcessors) {
        this.postProcessors = postProcessors;
    }

    public CMDIData process(File file) throws VTDException, IOException {
        CMDIData result = null;
        VTDGen vg = new VTDGen();
        vg.setDoc(IOUtils.toByteArray(new FileInputStream(file)));
        vg.parse(true);
        VTDNav nav = vg.getNav();
        FacetMapping facetMapping = getFacetMapping(nav);
        result = new CMDIData();
        AutoPilot id = new AutoPilot(nav);
        id.selectXPath(facetMapping.getIdMapping());
        result.setId(id.evalXPathToString());
        processResources(result, nav);
        processFacets(result, nav, facetMapping);
        return result;
    }

    private FacetMapping getFacetMapping(VTDNav nav) throws VTDException {
        FacetMapping result;
            String xsd = getXsdFromHeader(nav);
            if (xsd == null) {
                xsd = getXsdFromSchemaLocation(nav);
            }
            if (xsd == null) {
                throw new RuntimeException("Cannot get xsd schema so cannot get a proper mapping. Parse failed!");
            }
            result = facetMaps.get(xsd);
            if (result == null) {
                result = FacetMappingFactory.getFacetMapping(xsd);
                facetMaps.put(xsd, result);
            }
        return result;
    }

    private String getXsdFromHeader(VTDNav nav) throws XPathParseException, XPathEvalException, NavException {
        String result = null;
        nav.toElement(VTDNav.ROOT);
        AutoPilot ap = new AutoPilot(nav);
        ap.selectXPath("/CMD/Header/MdProfile/text()");
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
        int index = nav.getAttrVal("xsi:schemaLocation");
        if (index != -1) {
            String schemaLocation = nav.toNormalizedString(index);
            result = schemaLocation.split(" ")[1];//"http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1271859438204/xsd";
        }
        return result;
    }

    private void processResources(CMDIData result, VTDNav nav) throws VTDException {
        AutoPilot resourceProxy = new AutoPilot(nav);
        resourceProxy.selectXPath("/CMD/Resources/ResourceProxyList/ResourceProxy");
        AutoPilot resourceRef = new AutoPilot(nav);
        resourceRef.selectXPath("ResourceRef");
        AutoPilot resourceType = new AutoPilot(nav);
        resourceType.selectXPath("ResourceType");
        AutoPilot resourceMimeType = new AutoPilot(nav);
        resourceMimeType.selectXPath("ResourceType/@mimetype");
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
                if (matchedPattern) {
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
