package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.VloContextConfig;
import eu.clarin.cmdi.vlo.dao.SearchResultsDao;
import eu.clarin.cmdi.vlo.pages.FacetedSearchPage;
import java.util.Map;
import javax.servlet.ServletContext;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.request.RequestParameters;

/**
 * {@literal VLO} web application.
 *
 * The web application class defines an object representing the VLO web
 * application. 
 * 
 * Because the VloWebApplication class extends WebApplication, a class instance
 * will reside inside a web server container. By running the Start class
 * however, an instance of the application will reside outside a server
 * container.
 */
public class VloWebApplication extends WebApplication {

    private String theme = null;
    
    /**
     * 
     * @return 
     */
    public String getTheme (){
        return theme;
    }
    
    /**
     * 
     */
    public void setTheme (String theme){
        this.theme = theme;
    }
    
    /**
     * Client request interception<br><br>
     * 
     * Intercept client requests by adding to the default behavior of the web
     * request handling. Define a request cycle that retrieves the URL
     * parameters from the client requests. The URL parameters can then be
     * reflected back in the pages created by the application.
     */
    private class VloRequestCycle extends WebRequestCycle {        
        
        // find out why this is necessary
        VloRequestCycle (WebApplication app, WebRequest req, Response res){
            super (app, req, res);
        }   

        /**
         * Redefine the beginning of the processing of a request
         * 
         */
        @Override
        public void onBeginRequest() {
            /* Invoke the superclass method in order to have the default
             * processing.
             */
            super.onBeginRequest();
            /* After that, get the request parameters. Note that these are
             * more elaborate than the parameters supplied in the URL of the
             * original request.
             */            
            RequestParameters reqParam = this.request.getRequestParameters();
            // then, get the parameters in the URL from those
            Map <String, String[]> map = this.getWebRequest().getParameterMap();
            // check if there is a theme parameter among the URL parameters
            
            Object object = map.get("theme");
            if (object == null) {
                // no theme parameters included in the URL, reset stored value   
            } else {
                String value;
                value = map.get("theme").toString();
                // the client included the theme parameter in the URL, save it
                // by referencing the set method in the outer class
                VloWebApplication.this.setTheme(value);
            }
        }

        /**
         * 
         */
        @Override
        public void onEndRequest() {
            super.onEndRequest();
        }
    }

    /**
     * Install the custom request cycle. Note that the cast assumed to be safe.
     * 
     * @param req
     * @param res
     * @return
     */
    @Override
    public RequestCycle newRequestCycle(Request req, Response res){
        VloRequestCycle cycle = new VloRequestCycle(this, (WebRequest)req, res);
        return cycle;
    }

    private SearchResultsDao searchResults;
    
    /**
     * Flag indicating whether or not the application object lives in a web
     * server context.
     */
    boolean inContext;

    /**
     * Method that will be invoked when the application starts.
     */
    @Override
    public void init() {
        
        // this.setRequestCycleProvider(IRequestCycleProvider);
        
                

        if (inContext) {
            
            // get the servlet's context
            
            ServletContext servletContext;
            servletContext = this.getServletContext();
            
            /*
             * Send the application context to the configuration object to
             * enable it to read an external {@literal VloConfig.xml}
             * configuration file.
             */
            
            VloContextConfig.switchToExternalConfig(servletContext);
        }

        // start the application

        searchResults = new SearchResultsDao();        
    }

    /**
     * Web application constructor.
     *
     * Create an application instance configured to be living inside a web
     * server container.
     */
    public VloWebApplication() {

        /*
         * Read the application's packaged configuration. 
         * 
         * Because on instantiation a web application cannot said to be living 
         * in a web server context, parameters defined in the context can only 
         * be added to the configuration later, in this case: when the {@literal
         * init()} method will be invoked.
         */
        
        VloConfig.readPackagedConfig();

        // let the {@literal init()} method know that there will be a context

        inContext = true;  
    }

    /**
     * Web application constructor.
     *
     * Allows for the creation of an application instance that does not rely on
     * a web server context. When send the message 'false', this constructor
     * will create an object that will not look for an external configuration
     * file; it will exclusively rely on the packaged configuration. Typically,
     * the application's tests will send false to the application constructor.
     * 
     * @param inContext If and only if this parameter equals true. later on, the
     * {@literal init} method will try to determine the web server's container 
     * context so that, if it is defined in it, an external configuration can be
     * switched to. 
     */
    public VloWebApplication(Boolean inContext) {

        // remember that the application does not live in a web server context
        
        this.inContext = inContext;

        searchResults = new SearchResultsDao();
    }

    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<FacetedSearchPage> getHomePage() {
        return FacetedSearchPage.class;
    }

    public SearchResultsDao getSearchResultsDao() {
        return searchResults;
    }
}
