package eu.clarin.cmdi.vlo.importer;

import java.io.File;
import java.util.List;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

public class CMDIParserVTDXML implements CMDIDataProcessor {
    private final FacetMapping facetMapping;

    public CMDIParserVTDXML(FacetMapping facetMapping) {
        this.facetMapping = facetMapping;
    }

    public CMDIData process(File file) throws XPathParseException, XPathEvalException, NavException {
        CMDIData result = null;
        VTDGen vg = new VTDGen();
            if (vg.parseFile(file.getAbsolutePath(), true)) {
                VTDNav nav = vg.getNav();
                result = new CMDIData();
                AutoPilot id = new AutoPilot(nav);
                id.selectXPath(facetMapping.getIdMapping());
                result.setId(id.evalXPathToString());
                processResources(result, nav);
                processFacets(result, nav);
                return result;
            }
        return result;
    }

    private void processResources(CMDIData result, VTDNav nav) throws XPathParseException, XPathEvalException, NavException {
        AutoPilot resourceProxy = new AutoPilot(nav);
        resourceProxy.selectXPath("/CMD/Resources/ResourceProxyList/ResourceProxy");
        AutoPilot resourceRef = new AutoPilot(nav);
        resourceRef.selectXPath("ResourceRef");
        AutoPilot resourceType = new AutoPilot(nav);
        resourceType.selectXPath("ResourceType");
        while (resourceProxy.evalXPath() != -1) {
            String ref = resourceRef.evalXPathToString();
            String type = resourceType.evalXPathToString();
            if (ref != "" && type != "") {
                result.addResource(ref, type);
            }
        }
    }

    private void processFacets(CMDIData result, VTDNav nav) throws XPathParseException, XPathEvalException, NavException {
        List<FacetConfiguration> facetList = facetMapping.getFacets();
        for (FacetConfiguration config : facetList) {
            AutoPilot ap = new AutoPilot(nav);
            ap.selectXPath(config.getPattern());
            int index = ap.evalXPath();
            while (index != -1) {
                if (nav.getTokenType(index) == VTDNav.TOKEN_ATTR_NAME) {
                    //if it is an attribute you need to add 1 to the index to get the right value
                    index++;
                }
                String value = nav.toString(index);
                result.addDocField(config.getName(), value, config.isCaseInsensitive());
                index = ap.evalXPath();
            }
        }
    }
}
