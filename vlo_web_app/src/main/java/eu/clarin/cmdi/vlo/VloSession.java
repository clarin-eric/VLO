
package eu.clarin.cmdi.vlo;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

/**
 * Class associating VLO sessions with a VLO theme and VLO page parameters. 
 * 
 * While it is never explicitly created, it is always part of a cast, the public

* field of objects of this class seems to hold the expected values.
 *
 * @author keeloo
 */
public class VloSession extends WebSession {

    /**
     * Remember the parameters that should persist in URLs to VLO pages
     * <br><br>
     */
    public VloPageParameters vloSessionPageParameters = new VloPageParameters();

    /**
     *
     * @return
     */
    public VloPageParameters getVloSessionPageParameters() {
        return vloSessionPageParameters;
    }

    /**
     * Add page parameters with the VLO session related ones
     *
     * @param parameters a page parameter map
     *
     */
    public void addVloSessionPageParameters(VloPageParameters parameters) {
        
        vloSessionPageParameters.mergeWith(parameters);
    }
    /**
     * Theme currently applied in the VLO web application
     */
    private Theme currentTheme = new Theme("defaultTheme");

    /**
     * Constructor with request parameter
     * 
     * @param request 
     */
    public VloSession(Request request) {
        // only the parameterless constructors are invoked implicitly
        super(request);
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    /**
     * 
     * @param currentTheme 
     */
    public void setCurrentTheme(Theme currentTheme) {
        this.currentTheme = currentTheme;
    }
}
