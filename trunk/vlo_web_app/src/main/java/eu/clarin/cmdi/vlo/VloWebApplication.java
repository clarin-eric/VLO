package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.VloContextConfig;
import eu.clarin.cmdi.vlo.dao.SearchResultsDao;
import eu.clarin.cmdi.vlo.pages.FacetBoxPanel;
import eu.clarin.cmdi.vlo.pages.FacetHeaderPanel;
import eu.clarin.cmdi.vlo.pages.FacetLinkPanel;
import eu.clarin.cmdi.vlo.pages.FacetedSearchPage;
import eu.clarin.cmdi.vlo.pages.ShowResultPage;
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
 * {@literal VLO} web application<br><br>
 * 
 * Because the VloWebApplication class extends the WebApplication class, an
 * instance of the VLO web application normally resides inside a web server
 * container. However, by running the Start class, the application can also 
 * exist outside such a container.
 */
public class VloWebApplication extends WebApplication {

    /**
     * Remember the theme to be used. It is one of the elements in the list
     * of parameters persisting with the application object.
     */
    private String theme = "defaultTheme";
    
    /**
     * Get the theme stored<br><br>
     * 
     * @return the theme
     */
    public String getTheme (){
        return theme;
    }
    
    /**
     * Store the theme to be used 
     */
    public void setTheme (String theme){
        this.theme = theme;
    }
    
    PageParameters persistentParameters = new PageParameters ("theme", "defaultTheme");
    
    public PageParameters getPersistentParameters (){
        
        return persistentParameters;
    }
    
    /**
     * Add the parameters persisting with the application object to an a list
     * of parameters existing outside the object.<br><br>
     * 
     * Invoke this method from a component, after generating a list of 
     * parameters. 
     * 
     * @param parameters the existing list of parameters
     * @return the concatenation of the exising list and the parameters 
     * persisting with the application object. 
     */
    public PageParameters addPersistentParameters(PageParameters parameters) {

        // get the theme parameter from the application

        parameters.add("theme", getTheme());
        return parameters;
    }

    /**
     * Customized client request cycle<br><br>
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
                // no theme in the URL, do not change the value of the parameter
            } else {
                if (theme.equals(object[0])){
                    // keep the theme that was installed on a previous request
                } else {
                    // theme not installed yet, first: remember it
                    VloWebApplication.this.setTheme(object[0]);
                    // after that: determine the intended css and "install" it
                    // determine the intended picture and install it
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
                
        if (inContext) {
            
            /*
             * send messages to objects that need a static reference to this web
             * application object. While this is only required in the case of the
             * results page BookmarkablePageLink method, uniform approach might be
             * the most prefarable one.
             */
            ShowResultPage.setWebApp(this);
            FacetBoxPanel.setWebApp(this);
            FacetHeaderPanel.setWebApp(this);
            FacetLinkPanel.setWebApp(this);
            FacetedSearchPage.setWebApp(this);

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
