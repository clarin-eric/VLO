package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.dao.SearchResultsDao;
import eu.clarin.cmdi.vlo.pages.FacetedSearchPage;
import javax.servlet.ServletContext;
import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Application object for your web application. If you want to run this 
 * application without deploying, run the Start class.
 * 
 */
public class VloApplication extends WebApplication {

    private final SearchResultsDao searchResults;
    
    static VloConfig config;  
    
    /**
     * 
     */
    public VloApplication() {

        /**
         * 
         */
        ServletContext servletContext;
        servletContext = this.getServletContext();
        config = VloConfig.setWebApp(servletContext);
        
        String test;
        test = VloConfig.get().getSolrUrl();
        
        searchResults = new SearchResultsDao();        
    }
    
    /**
     * Invoke the application in test mode
     */
    public VloApplication(VloConfig testConfig) {
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
