package eu.clarin.cmdi.vlo.dao;

import eu.clarin.cmdi.vlo.VloWebApplication;
import org.apache.wicket.RequestCycle;

public final class DaoLocator {

    public static SearchResultsDao getSearchResultsDao()
    {
        VloWebApplication app = (VloWebApplication)RequestCycle.get().getApplication();
        return app.getSearchResultsDao();
    }
    
}
