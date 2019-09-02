package eu.clarin.cmdi.vlo.exposure.postgresql;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.exposure.models.SearchQuery;

public interface SearchQueryHandler {

    public boolean addSearchQuery(VloConfig vloConfig, SearchQuery sq);
}
