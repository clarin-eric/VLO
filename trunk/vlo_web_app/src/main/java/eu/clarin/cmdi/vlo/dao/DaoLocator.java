package eu.clarin.cmdi.vlo.dao;

import org.apache.wicket.RequestCycle;

import eu.clarin.cmdi.vlo.VloWebApplication;


public final class DaoLocator {

    public static SearchResultsDao getSearchResultsDao()
    {
        VloWebApplication app = (VloWebApplication)RequestCycle.get().getApplication();
        return app.getSearchResultsDao();
    }
    
}
