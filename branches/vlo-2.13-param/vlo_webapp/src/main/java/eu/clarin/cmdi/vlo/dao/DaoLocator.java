package eu.clarin.cmdi.vlo.dao;

import org.apache.wicket.RequestCycle;

import eu.clarin.cmdi.vlo.VloApplication;


public final class DaoLocator {

    public static SearchResultsDao getSearchResultsDao()
    {
        VloApplication app = (VloApplication)RequestCycle.get().getApplication();
        return app.getSearchResultsDao();
    }
    
}
