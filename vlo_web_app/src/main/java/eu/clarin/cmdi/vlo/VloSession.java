
package eu.clarin.cmdi.vlo;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * A web session containing a VLO theme and parameters that are considered to
 * be persistent in a VLO session.
 * 
 * Note that these parameters can include the specification of the theme.
 * 
 * @author keeloo
 */
public class VloSession extends WebSession {

    // remember the parameters that need to persist in URLs to VLO pages in this session
    public PageParameters vloSessionPageParameters = new PageParameters();

    // remember this session's theme
    private Theme currentTheme = new Theme("defaultTheme");

    /**
     * Construct a session object with a request parameter
     * 
     * @param request 
     */
    public VloSession(Request request) {
        // only the parameterless constructors are invoked implicitly
        super(request);
    }

    /**
     * Get the session's theme
     * 
     * @return the session's theme 
     */
    public Theme getCurrentTheme() {
        return this.currentTheme;
    }

    /**
     * Set the session's theme
     * 
     * @param theme the session's theme
     */
    public void setCurrentTheme(Theme theme) {
        this.currentTheme = theme;
    }
    
    /**
     * Return the session's persistent parameters
     * 
     * @return session parameters
     */
    public PageParameters getVloSessionPageParameters() {
        return vloSessionPageParameters;
    }

    /**
     * Add parameters to the session's persistent parameters
     * 
     * @param parameters a page parameter map
     *
     */
    public void addVloSessionPageParameters(PageParameters parameters) {
        
        vloSessionPageParameters.mergeWith(parameters);
    }
}
