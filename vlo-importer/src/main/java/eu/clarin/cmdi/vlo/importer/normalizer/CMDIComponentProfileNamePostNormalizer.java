package eu.clarin.cmdi.vlo.importer.normalizer;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CMDIData;

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
public class CMDIComponentProfileNamePostNormalizer extends AbstractPostNormalizer {

    private static final String XPATH = "/ComponentSpec/Header/Name/text()";
    private final String registryBaseURl;

    private final static Logger LOG = LoggerFactory.getLogger(CMDIComponentProfileNamePostNormalizer.class);
    private final ConcurrentMap<String, List<String>> cache = new ConcurrentHashMap<>();

    public CMDIComponentProfileNamePostNormalizer(VloConfig config) {
        super(config);
        registryBaseURl = config.getComponentRegistryRESTURL();
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

    /**
     * Gets the name of the profile from the expanded xml in the component
     * registry
     *
     * @param profileId
     * @return
     * @throws VTDException
     */
    private String calculate(String profileId) throws VTDException {
        LOG.debug("PARSING PROFILE: " + registryBaseURl + profileId);

        final VTDGen vg = new VTDGen();
        if (vg.parseHttpUrl(registryBaseURl + profileId + "/xml", true)) {
            LOG.debug("PARSED PROFILE: " + registryBaseURl + profileId);
            final VTDNav vn = vg.getNav();

            final AutoPilot ap = new AutoPilot();
            ap.selectXPath(XPATH);
            ap.bind(vn);

            final int idx = ap.evalXPath();
            LOG.debug("EVALUATED XPATH: " + XPATH + " found idx: " + idx);
            if (idx == -1) { // idx represent the nodeId in the xml file, if -1 the xpath evaluates to nothing.
                LOG.warn("No profile name in definition for {}", profileId);
                return "";
            }
            return vn.toString(idx);
        } else {
            LOG.error("Cannot open and/or parse XML Schema: {}.", registryBaseURl + profileId);
            throw new VTDException("Cannot open and/or parse XML Schema");
        }
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }
}
