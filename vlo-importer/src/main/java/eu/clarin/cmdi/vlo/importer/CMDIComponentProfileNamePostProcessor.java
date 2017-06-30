package eu.clarin.cmdi.vlo.importer;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes the value of the componentprofileid and uses the componentregistry REST
 * service to transform this to the name of the componentprofile.
 */
public class CMDIComponentProfileNamePostProcessor extends AbstractPostProcessor {

    private static final String XPATH = "/ComponentSpec/Header/Name/text()";
    private String BASE_URL = null;

    private static final String EMPTY_STRING = "";
    private final static Logger LOG = LoggerFactory.getLogger(CMDIComponentProfileNamePostProcessor.class);
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public CMDIComponentProfileNamePostProcessor(VloConfig config) {
        super(config);
    }

    @Override
    public List<String> process(String profileId, CMDIData cmdiData) {
        String result = EMPTY_STRING;
        if (profileId != null) {
            if (cache.containsKey(profileId)) {
                result = cache.get(profileId);
            } else {
                VTDGen vg;
                VTDNav vn;

                final AutoPilot ap= new AutoPilot();
                try {
                    ap.selectXPath(XPATH);
                } catch (XPathParseException e) {
                    LOG.error(e.getLocalizedMessage());
                    return Collections.singletonList(result);
                }
                vg = new VTDGen();
                BASE_URL = getConfig().getComponentRegistryRESTURL();

                LOG.debug("PARSING PROFILE: " + BASE_URL + profileId);
                // get the name of the profile from the expanded xml in the component registry
                if (vg.parseHttpUrl(BASE_URL + profileId + "/xml", true)) {
                    LOG.debug("PARSED PROFILE: " + BASE_URL + profileId);
                    vn = vg.getNav();
                    ap.bind(vn);
                    int idx;
                    try {
                        idx = ap.evalXPath();
                        LOG.debug("EVALUATED XPATH: " + XPATH + " found idx: " + idx);
                        if (idx == -1) { // idx represent the nodeId in the xml file, if -1 the xpath evaluates to nothing.
                            Collections.singletonList(result);
                        }
                        result = vn.toString(idx);
                        cache.put(profileId, result);
                    } catch (NavException | XPathEvalException e) {
                        LOG.error(e.getLocalizedMessage());
                        Collections.singletonList(result);
                    }
                } else {
                    LOG.error("Cannot open and/or parse XML Schema: {}.", BASE_URL + profileId);
                }
            }
        }
        return Collections.singletonList(result);
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }
}
