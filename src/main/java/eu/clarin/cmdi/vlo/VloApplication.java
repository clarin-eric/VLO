package eu.clarin.cmdi.vlo;

import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
        BeanFactory factory = new ClassPathXmlApplicationContext(new String[] { "applicationContext.xml" });
        factory.getBean("configuration"); //Use Configuration.getInstance to get the Configuration just loading the instance here.
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
