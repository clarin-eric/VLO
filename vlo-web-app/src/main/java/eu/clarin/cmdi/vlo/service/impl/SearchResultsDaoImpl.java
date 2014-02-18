package eu.clarin.cmdi.vlo.service.impl;

import eu.clarin.cmdi.vlo.service.SearchResultsDao;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchResultsDaoImpl extends SolrDaoImpl implements SearchResultsDao {

    private final static Logger logger = LoggerFactory.getLogger(SearchResultsDaoImpl.class);

    public SearchResultsDaoImpl(SolrServer solrServer, VloConfig config) {
        super(solrServer, config);
    }

    @Override
    public SolrDocumentList getResults(SolrQuery query) {
        setDefaultSortField(query);
        QueryResponse response = fireQuery(query);
        SolrDocumentList results = response.getResults();
        return results;
    }

    @Override
    public List<FacetField> getFacets(SolrQuery query) {
        final QueryResponse response = fireQuery(query);
        final List<FacetField> facetFields = response.getFacetFields();
        logger.debug("Found {} facet fields", facetFields.size());
        return facetFields;
    }

    @Override
    public SolrDocumentList getDocuments(SolrQuery query) {
        QueryResponse queryResponse = fireQuery(query);
        final SolrDocumentList documents = queryResponse.getResults();
        logger.debug("Found {} documents", documents.getNumFound());
        return documents;
    }

    private void setDefaultSortField(SolrQuery query) {
        if (query.getSortField() == null) {
            query.setSort(SolrQuery.SortClause.asc(FacetConstants.FIELD_NAME));
        }
    }

}
