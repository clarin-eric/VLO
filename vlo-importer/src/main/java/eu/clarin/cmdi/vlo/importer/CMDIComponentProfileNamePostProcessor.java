package eu.clarin.cmdi.vlo.importer;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes the value of the componentprofileid and uses the componentregistry REST
 * service to transform this to the name of the componentprofile.
 */
public class CMDIComponentProfileNamePostProcessor extends AbstractPostProcessor {

    private static final String XPATH = "/ComponentSpec/Header/Name/text()";
    private String BASE_URL = null;

    private final static Logger LOG = LoggerFactory.getLogger(CMDIComponentProfileNamePostProcessor.class);
    private final ConcurrentMap<String, List<String>> cache = new ConcurrentHashMap<>();

    public CMDIComponentProfileNamePostProcessor(VloConfig config) {
        super(config);
    }

    @Override
    public List<String> process(String profileId, CMDIData cmdiData) {
        if (profileId != null) {
            return cache.computeIfAbsent(profileId, (key) -> {
                try {
                    return Collections.singletonList(calculate(key));
                } catch (VTDException ex) {
                    LOG.error("Error while looking up profile name for profile {}", profileId, ex);
                    return null;
                }
            });
        } else {
            return Collections.singletonList("");
        }
    }

    private String calculate(String profileId) throws VTDException {
        final AutoPilot ap = new AutoPilot();
        ap.selectXPath(XPATH);
        final VTDGen vg = new VTDGen();
        BASE_URL = getConfig().getComponentRegistryRESTURL();

        LOG.debug("PARSING PROFILE: " + BASE_URL + profileId);
        // get the name of the profile from the expanded xml in the component registry
        if (vg.parseHttpUrl(BASE_URL + profileId + "/xml", true)) {
            LOG.debug("PARSED PROFILE: " + BASE_URL + profileId);
            final VTDNav vn = vg.getNav();
            ap.bind(vn);
            int idx;
            idx = ap.evalXPath();
            LOG.debug("EVALUATED XPATH: " + XPATH + " found idx: " + idx);
            if (idx == -1) { // idx represent the nodeId in the xml file, if -1 the xpath evaluates to nothing.
                LOG.warn("No profile name in definition for {}", profileId);
                return "";
            }
            return vn.toString(idx);
        } else {
            LOG.error("Cannot open and/or parse XML Schema: {}.", BASE_URL + profileId);
            throw new VTDException("Cannot open and/or parse XML Schema");
        }
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }
}
