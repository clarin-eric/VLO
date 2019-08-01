package eu.clarin.cmdi.vlo.exposure.postgresql;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.exposure.models.SearchResult;

public interface SearchResultHandler {

	public boolean addSearchResult(VloConfig vloConfig, long queryId, SearchResult sr);
}
