package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.dao.SearchResultsDao;
import eu.clarin.cmdi.vlo.pages.FacetedSearchPage;
import javax.servlet.ServletContext;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * {@literal VLO} web application.<br><br>
 * 
 * Because the class extends the WebApplication class, the VLO application 
 * will normally be deployed on a web server. However, by running the Start
 * class, the application outside of a server container. 
 * 
 */
public class VloApplication extends WebApplication {

    private final SearchResultsDao searchResults;
    
    // application configuration object
    
    static VloConfig config;
    
    // flag indicating whether or not the application object lives in a context
    
    boolean inContext;

    /**
     * Method that will be invoked when the application starts
     */
    @Override
    public void init() {

        if (inContext) {
            
            // add the context parameters to the configuration

            ServletContext servletContext;
            servletContext = this.getServletContext();
            config = VloConfig.addServletContext(config, servletContext);
        }
    }

    /**
     * Web application constructor<br><br>
     *
     * Create the application by invoking this constructor, whenever you want it
     * to be an application deployed in a web server container, that is: as an
     * application living in a web server context.
     */
    public VloApplication() {

        /**
         * Read the application's configuration. Because on instantiation a web
         * application cannot said to be living in a context, context parameters
         * can only be added to the configuration later, in this case: when the
         * {@literal init()} method will be invoked.
         */
        config = VloConfig.webApp();

        // let the {@literal init()} method know that there will be a context

        inContext = true;

        // start the application

        searchResults = new SearchResultsDao();
    }

    /**
     * Web application constructor<br><br>
     *
     * Introduce the idea of a channel: the environment in which a test
     * configuration object is created is in a way connected to this method.
     *
     */
    public VloApplication(VloConfig testConfig) {

        // let the application use the configuration created outside of it

        inContext = false;

        config = testConfig;

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
