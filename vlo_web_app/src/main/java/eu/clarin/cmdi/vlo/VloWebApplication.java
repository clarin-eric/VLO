package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.VloContextConfig;
import eu.clarin.cmdi.vlo.dao.SearchResultsDao;
import eu.clarin.cmdi.vlo.pages.BasePage;
import eu.clarin.cmdi.vlo.pages.BasePanel;
import eu.clarin.cmdi.vlo.pages.FacetedSearchPage;
import java.util.Map;
import javax.servlet.ServletContext;
import org.apache.wicket.Application;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebSession;
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
                if (object[0].matches(((ThemedSession)getSession()).getCurrentTheme().name)) {
                    // current theme requested, nothing to do
                } else {
                    // different theme requested, compose it
                    ((ThemedSession)getSession()).setCurrentTheme(new Theme (object[0]));
                    // remember the theme as a persistent parameter
                    ((ThemedSession)getSession()).persistentParameters.put("theme", object[0]);
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
     * Compose a map to be included in the HTML document, designating the 
     * positions of the links to partner web sites
     * 
     * @return 
     */
    private String getClarinDPartnerLinkMap(){
        String map;
        
        map = "<map name=\"partnerLinks\">\n";
        map = map + "</map>";
       
        return map;
    }
    
    /**
     * Compose a map to be included in the HTML document, designating the 
     * positions of the links to partner web sites
     * 
     * @return 
     */
    private String getDefaultPartnerLinkMap(){
        String map;
        
        map = "<map name=\"partnerLinks\">\n";
        map = appendToPartnerLinkMap (map,
                "114.00,65,167.50,104",
                "http://www.clarin.eu)",
                "alt=\"clarin link");
        map = appendToPartnerLinkMap (map,
                "177.00,65,214,104",
                "http://wals.info",
                "wals link");
        map = appendToPartnerLinkMap (map,
                "229,65,279,104",
                "http://linguistlist.org",
                "linguistlist link");
        map = appendToPartnerLinkMap (map,
                "290,65,320,104",
                "http://www.elra.info",
                "elra link");
        map = appendToPartnerLinkMap (map,
                "328,65,370,104",
                "http://www.mpi.nl/dobes",
                "dobes link");
        map = appendToPartnerLinkMap (map,
                "379,65,428,104",
                "http://www.dfki.de/web",
                "dfki link");
        map = appendToPartnerLinkMap (map,
                "434,65,484,104",
                "http://www.delaman.org",
                "deleman link");
        
        map = map + "</map>";
       
        return map;
    }
    
    /**
     * Add a link location to the map indicating the partner links
     * 
     * @param map
     * @return 
     */
    private String appendToPartnerLinkMap(String map, String coordinates,
            String URL, String alt) {
        
        if (map == null) {
          map = "<map name=\"partnerLinks\">\n";
        } else if (map.equals("")) {
            map = "<map name=\"partnerLinks\">\n";
        }
        
        map = map + "<AREA SHAPE=\"rect\" COORDS=\"" + coordinates + "\" HREF=\"" + 
                URL + "\" alt=\"" + alt + "\"\n";
        
        return map;
    }
    
    public class ThemedSession extends WebSession {
        
        /**
         * Remember the parameters that should persist in URLs to VLO pages
         * <br><br>
         */
        public PageParameters persistentParameters = new PageParameters();
        
        /**
         *
         * @return
         */
        public PageParameters getPersistentParameters (){
            return persistentParameters; 
        }

        /**
         * Reflect the persistent parameters in the page parameter map<br><br>
         *
         * @param parameters a page parameter map
         * @return the page parameter map including the current persistent
         * parameters
         */
        public PageParameters reflectPersistentParameters(PageParameters parameters) {

            parameters.putAll(persistentParameters);

            // parameters.add("theme", "defaultTheme");
            return parameters;
        }

        /**
         * Theme currently applied in the VLO web application
         */
        private Theme currentTheme = new Theme ("defaultTheme"); 

        public ThemedSession(Application application, Request request) {
            super(application, request);
        }

        public Theme getCurrentTheme() {
            return currentTheme;
        }

        public void setCurrentTheme(Theme currentTheme) {
            this.currentTheme = currentTheme;
        }
    }
    

    @Override
    public ThemedSession newSession(Request request, Response response) {
        
        return new ThemedSession(this, request);
    }
    
    /**
     * A theme is composed from a page title, a CSS file, two image files, and a
     * partner link map relating coordinates in the right image to partner links
     */
    public class Theme {

        public String name, pageTitle, topLeftImage, topRightImage, cssFile, 
                partnerLinkMap;

        /**
         * Compose a theme<br><br>
         *
         * @param name the name of the theme to be composed
         */
        public Theme(String themeName) {

            if (themeName.matches("CLARIN-D")) {
                // select the CLARIN-D theme's components
                
                pageTitle = "CLARIN-D Virtual Language Observatory - Resources";
                topLeftImage = "Images/topleft-clarin-d.png";
                topRightImage = "Images/topright-clarin-d.png";
                cssFile = "css/clarin-d.css";
                partnerLinkMap = getClarinDPartnerLinkMap();
                name = "CLARIN-D";
            } else {
                // select the default theme elements
                pageTitle = "CLARIN Virtual Language Observatory - Resources";
                topLeftImage = "Images/topleftvlo.gif";
                topRightImage = "Images/toprightvlo.gif";
                if (VloConfig.getExpectReverseProxy()) {
                    cssFile = VloConfig.getReverseProxyPrefix() + "css/main.css";
                } else {
                    cssFile = "css/main.css";
                }
                partnerLinkMap = getDefaultPartnerLinkMap();
                name = "defaultTheme";
            }
            // remember the theme as a persistent parameter
            // getPersistentParameters.put("theme", name);
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
