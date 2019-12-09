package eu.clarin.cmdi.vlo.exposure.postgresql;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.exposure.models.Record;
import eu.clarin.cmdi.vlo.exposure.models.SearchResult;

import java.util.HashMap;
import java.util.List;

public interface SearchResultHandler {

    boolean addSearchResult(VloConfig vloConfig, long queryId, SearchResult sr);
    List<Record> getSearchResultsStat(VloConfig vloConfig, QueryParameters qp, boolean withHomepageResults);
    HashMap<String, Double> getSearchResultsStatPerRecordId(VloConfig vloConfig, QueryParameters qp);
}
