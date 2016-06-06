package eu.clarin.cmdi.vlo.importer;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes the value of the componentprofileid and uses the componentregistry REST service to transform this to the name of the componentprofile.
 */
public class CMDIComponentProfileNamePostProcessor implements PostProcessor {
    private static final String XPATH = "/ComponentSpec/Header/Name/text()";
    private String BASE_URL = null;
    private AutoPilot ap = null;
    private VTDGen vg = null;
    private VTDNav vn = null;

    private static final String _EMPTY_STRING = "";
    private final static Logger LOG = LoggerFactory.getLogger(CMDIComponentProfileNamePostProcessor.class);
    private final HashMap<String, String> cache = new HashMap<String, String>();

    @Override
    public List<String> process(String profileId) {
        String result = _EMPTY_STRING;
        if(profileId != null){
            if(cache.containsKey(profileId)){
                result = cache.get(profileId);
            }
            else {
                setup();
                // get the name of the profile from the expanded xml in the component registry
                if(vg.parseHttpUrl(BASE_URL + profileId + "/xml", true)){
                    LOG.debug("PARSED: "+BASE_URL+profileId);
                    vn = vg.getNav();
                    ap.bind(vn);
                    int idx;
                    try { 
                        idx = ap.evalXPath();
                        LOG.debug("EVALUATED XPATH: "+XPATH+ " found idx: "+idx);
                        if(idx == -1){ // idx represent the nodeId in the xml file, if -1 the xpath evaluates to nothing.
                            List<String> resultList = new ArrayList<String>();
                            resultList.add(result);
                            return resultList;
                        }
                        result = vn.toString(idx);
                        cache.put(profileId, result);
                    } catch (NavException e) {
                        LOG.error(e.getLocalizedMessage());
                        List<String> resultList = new ArrayList<String>();
                        resultList.add(result);
                        return resultList;
                    } catch (XPathEvalException e) {
                        LOG.error(e.getLocalizedMessage());
                        List<String> resultList = new ArrayList<String>();
                        resultList.add(result);
                        return resultList;
                    }
                }
                else {
                    LOG.error("Cannot open and/or parse XML Schema: {}.", BASE_URL + profileId);
                }
            }
        }
        List<String> resultList = new ArrayList<String>();
        resultList.add(result);
        return resultList;
    }

    private void setup() {
        ap = new AutoPilot();
        try {
            ap.selectXPath(XPATH);
        } catch (XPathParseException e) {
            LOG.error(e.getLocalizedMessage());
            ap = null;
        }
        vg = new VTDGen();
        BASE_URL = MetadataImporter.config.getComponentRegistryRESTURL();
    }
}
