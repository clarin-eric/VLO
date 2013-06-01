package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.VloContextConfig;
import eu.clarin.cmdi.vlo.dao.SearchResultsDao;
import eu.clarin.cmdi.vlo.pages.BasePage;
import eu.clarin.cmdi.vlo.pages.BasePanel;
import eu.clarin.cmdi.vlo.pages.FacetedSearchPage;
import java.util.Map;
import javax.servlet.ServletContext;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.request.RequestParameters;

/**
 * Virtual Language Observatory web application<br><br>
 * 
 * <describe VLO>
 * 
 * While the application is intended to run inside a web server container, 
 * running the Start class enables you to run it without outside one.
 */
public class VloWebApplication extends WebApplication {
    
    /**
     * Remember the parameters that should persist in URLs to VLO pages <br><br>
     */
    public PageParameters persistentParameters = new PageParameters();
            
    /**
     * Reflect the persistent parameters in the page parameter map<br><br>
     * 
     * @param parameters a page parameter map
     * @return the page parameter map including the current persistent parameters
     */
    public PageParameters reflectPersistentParameters(PageParameters parameters) {
        
        parameters.putAll(persistentParameters);

        // parameters.add("theme", "defaultTheme");
        return parameters;
    }

    /**
     * Customized client request cycle<br><br>
     * 
     * <intercept resquest in order to update session parameter list>
     * 
     * Add behavior to the web request handling by retrieving persistent
     * parameters to the application from from client requests, and store
     * the in the application object.
     */
    private class CustomCycle extends WebRequestCycle {        
        
        // find out why this is necessary
        CustomCycle (WebApplication app, WebRequest req, Response res){
            super (app, req, res);
        }   

        /**
         * Add the behavior to the beginning of the processing of a request
         */
        @Override
        public void onBeginRequest() {
            // first, invoke the default behavior
            super.onBeginRequest();
            // after that, get the parameters of the request itself
            RequestParameters reqParam = this.request.getRequestParameters();
            // from these, get the parameters represented in the URL
            Map <String, String[]> map = this.getWebRequest().getParameterMap();
            // check if there is a theme parameter        
            String[] object = map.get("theme");
                        
            if (object == null) {
                // no theme choosen, keep the current one
            } else {
                // check if the users requests a different theme 
                if (object[0].matches(currentTheme.name)) {
                    // current theme requested, nothing to do
                } else {
                    // different theme requested, compose it
                    currentTheme = new Theme (object[0]);
                }
            }
        }
    }

    /**
     * Put the the customized request cycle up for installation<br><br>
     * 
     * Note that casting the request to a WebRequest is assumed to be safe.
     * 
     * @param request the request to be passed on the the new handler
     * @param response the response to be passed on the the new handler
     * @return the new handler
     */
    @Override
    public RequestCycle newRequestCycle(Request request, Response response){
        
        /* Pass on the application object and  parameters to new the request 
         * cycle when creating it.
         */
        CustomCycle cycle = new CustomCycle(this, (WebRequest)request, response);
        
        return cycle;
    }
    
    /**
     * Theme currently applied in the VLO web application
     */
    public Theme currentTheme = new Theme ("defaultTheme"); 

    /**
     * A theme is defined by a CSS file, and two image files
     */
    public class Theme {

        public String name, topLeftImage, topRightImage, cssFile;

        /**
         * Compose a theme<br><br>
         *
         * @param name the name of the theme to be composed
         */
        public Theme(String themeName) {

            if (themeName.matches("Clarin-d")) {
                // select the Clarin-d theme's components
                
                topLeftImage = "Images/topleftvlo.gif";
                topRightImage = "Images/toprightvlo.gif";
                cssFile = "css/main.css";
                name = "Clarin-d";
            } else {
                // select the default theme elements
                
                topLeftImage = "Images/topleftvlo.gif";
                topRightImage = "Images/toprightvlo.gif";
                cssFile = "css/main.css";
                name = "defaultTheme";
            }
            // remember the theme as a persistent parameter
            persistentParameters.put("theme", name);
        }
    }

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
                
        if (inContext) {
            
            /*
             * send messages to objects that need a static reference to this web
             * application object. While this, at a one point in time, was only 
             * required in the case of the results page BookmarkablePageLink 
             * method, uniform approach might be the most prefarable one.
             */
            BasePage.setWebApp(this);
            BasePanel.setWebApp(this);
            
            // install theme -> compose theme

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

        // creata an object referring to the search results
        searchResults = new SearchResultsDao();        

        // hand over control to the application
    }
    
    // remember the search results
    private SearchResultsDao searchResults;
    
    /**
     * Web application constructor<br><br>
     *
     * Create an application instance configured to be living inside a web
     * server container.
     */
    public VloWebApplication() {

        /*
         * Read the application's packaged configuration
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
     * Web application constructor<br><br>
     *
     * Allows for the creation of an application instance that does not rely on
     * a web server context. When send the message 'false', this constructor
     * will create an object that will not look for an external configuration
     * file; it will exclusively rely on the packaged configuration. Typically,
     * the application's tests will send false to the application constructor.
     * <br><br>
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
