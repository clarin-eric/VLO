package eu.clarin.cmdi.vlo.exposure.postgresql;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.exposure.models.SearchQuery;

import java.util.HashMap;

public interface SearchQueryHandler {

    boolean addSearchQuery(VloConfig vloConfig, SearchQuery sq);
    HashMap<String,Integer> getKeywordsStat(VloConfig vloConfig, QueryParameters qp);
    HashMap<String,Integer> getSearchQueriesPerDay(VloConfig vloConfig, QueryParameters qp);
}
