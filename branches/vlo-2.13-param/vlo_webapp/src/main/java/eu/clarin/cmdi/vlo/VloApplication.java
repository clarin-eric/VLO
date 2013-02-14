package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.config.WebAppConfig;
import eu.clarin.cmdi.vlo.dao.SearchResultsDao;
import eu.clarin.cmdi.vlo.pages.FacetedSearchPage;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 */
public class VloApplication extends WebApplication {

    private final SearchResultsDao searchResults;

    static WebAppConfig config;
    
    public VloApplication() {
        /**
         * The new way of doing things. In case of the web application tests:
         * just extend this class, and override the constructor by invoking the
         * open method of the extended test web application configuration class.
         * Also, change the way in which the get methods are being invoked. From
         * now on, you only have to invoke config.get ...
         */
        config = WebAppConfig.open();
        // String test;
        // test = config.getVloHomeLink();

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
