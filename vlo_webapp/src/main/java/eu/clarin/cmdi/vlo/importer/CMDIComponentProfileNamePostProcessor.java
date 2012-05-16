package eu.clarin.cmdi.vlo.importer;

import com.ximpleware.*;
import eu.clarin.cmdi.vlo.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Takes the value of the componentprofileid and uses the componentregistry REST service to transform this to the name of the componentprofile.
 */
public class CMDIComponentProfileNamePostProcessor implements PostProcessor{

    private static String XPATH = "/c:CMD_ComponentSpec/c:Header/c:Name/text()";
    private String BASE_URL = null;
    AutoPilot ap = null;
    VTDGen vg = null;
    VTDNav vn = null;

    private static final String _EMPTY_STRING = "";

    private final static Logger LOG = LoggerFactory.getLogger(CMDIComponentProfileNamePostProcessor.class);

    @Override
    public String process(String value) {
        String result = _EMPTY_STRING;
        if(value != null){
            setup();
            if(vg.parseHttpUrl(BASE_URL + value, true)){
                vn = vg.getNav();
                ap.bind(vn);
                int idx;
                try {
                    idx = ap.evalXPath();
                    if(idx == -1){ // idx represent the nodeId in the xml file, if -1 the xpath evaluates to nothing.
                        return result;
                    }
                    result = vn.toString(idx);
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage());
                    return result;
                }
            }
            else{
                LOG.error("CANNOT OPEN AND/OR PARSE: " + BASE_URL + value);
            }
        }
        return result;
    }

    private void setup() {
        if(ap == null){
            ap = new AutoPilot();
            try {
                ap.selectXPath(XPATH);
            } catch (XPathParseException e) {
                LOG.error(e.getLocalizedMessage());
                ap = null;
            }
            vg = new VTDGen();
            BASE_URL = Configuration.getInstance().getComponentRegistryRESTURL();
        }
    }
}
