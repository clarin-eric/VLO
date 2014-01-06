package eu.clarin.cmdi.vlo.dao;

import eu.clarin.cmdi.vlo.VloWebApplication;
import org.apache.wicket.Application;


public final class DaoLocator {

    public static SearchResultsDao getSearchResultsDao()
    {
        VloWebApplication app = (VloWebApplication)Application.get();
        return app.getSearchResultsDao();
    }
    
}
