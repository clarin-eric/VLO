package eu.clarin.cmdi.vlo.exposure.postgresql;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.exposure.models.SearchResult;
import eu.clarin.cmdi.vlo.exposure.postgresql.VloExposureException;

public interface SearchResultHandler {
	public boolean addSearchResult(VloConfig vloConfig,long queryId, SearchResult sr) throws VloExposureException;
}
