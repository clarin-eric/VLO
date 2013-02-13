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

    public VloApplication() {
        // BeanFactory factory = new ClassPathXmlApplicationContext(new String[] { Configuration.CONFIG_FILE });
        // factory.getBean("configuration"); //Use Configuration.getInstance to get the Configuration just loading the instance here.
        WebAppConfig.setServletContext(this.getServletContext());
        String test = WebAppConfig.open().getSolrUrl();
        
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
