package eu.clarin.cmdi.vlo.importer;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathParseException;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes the value of the componentprofileid and uses the componentregistry REST service to transform this to the name of the componentprofile.
 */
public class CMDIComponentProfileNamePostProcessor implements PostProcessor{

    private static String XPATH = "/CMD_ComponentSpec/Header/Name/text()";
    private String BASE_URL = null;
    AutoPilot ap = null;
    VTDGen vg = null;
    VTDNav vn = null;

    private static final String _EMPTY_STRING = "";
    private final static Logger LOG = LoggerFactory.getLogger(CMDIComponentProfileNamePostProcessor.class);
    private final HashMap<String, String> cache = new HashMap<String, String>();

    @Override
    public String process(String value) {
        String result = _EMPTY_STRING;
        if(value != null){
            if(cache.containsKey(value)){
                result = cache.get(value);
            }
            else{
                setup();
                if(vg.parseHttpUrl(BASE_URL + value, true)){
                    LOG.info("PARSED: "+BASE_URL+value);
                    vn = vg.getNav();
                    ap.bind(vn);
                    int idx;
                    try {
                        idx = ap.evalXPath();
                        LOG.info("EVALUATED XPATH: "+XPATH+ " found idx: "+idx);
                        if(idx == -1){ // idx represent the nodeId in the xml file, if -1 the xpath evaluates to nothing.
                            return result;
                        }
                        result = vn.toString(idx);
                        cache.put(value, result);
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage());
                        return result;
                    }
                }
                else{
                    LOG.error("CANNOT OPEN AND/OR PARSE: " + BASE_URL + value);
                }
            }
        }
        return result;
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
        BASE_URL = VloConfig.get().getComponentRegistryRESTURL();
    }
}
