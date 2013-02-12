
package eu.clarin.cmdi.vlo.config;

import javax.servlet.ServletContext;
import org.apache.wicket.protocol.http.WebApplication;

/**
 *
 * @author keeloo
 */
public class WebAppParam {
    
    static WebApplication webApp = null;
    
    static ServletContext servletContext = null; 

    /**
     * 
     * @param param
     * @return 
     */
    public static String getContextParam(String param) {
        // throw new UnsupportedOperationException("Not yet implemented");

        if (webApp == null) {
            webApp = WebApplication.get();
            servletContext = webApp.getServletContext();
            if (servletContext == null) {
                // handle the error
            }
        }

        String value = servletContext.getInitParameter(param);
        
        return value;
    }
}
