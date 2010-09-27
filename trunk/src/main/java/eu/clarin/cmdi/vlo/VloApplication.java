package eu.clarin.cmdi.vlo;

import org.apache.wicket.protocol.http.WebApplication;

import eu.clarin.cmdi.vlo.dao.SearchResultsDao;
import eu.clarin.cmdi.vlo.pages.FacetedSearchPage;


/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 */
public class VloApplication extends WebApplication {

    private final SearchResultsDao searchResults = new SearchResultsDao();

    /**
     * Constructor
     */
    public VloApplication() {
    }

    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    public Class<FacetedSearchPage> getHomePage() {
        return FacetedSearchPage.class;
    }

    public SearchResultsDao getSearchResultsDao() {
        return searchResults;
    }

}
