package eu.clarin.cmdi.vlo.service.impl;

import eu.clarin.cmdi.vlo.service.SearchResultsDao;
import eu.clarin.cmdi.vlo.FacetConstants;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

public class SearchResultsDaoImpl extends SolrDaoImpl implements SearchResultsDao {

    public SearchResultsDaoImpl(SolrServer solrServer) {
        super(solrServer);
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
        QueryResponse response = fireQuery(query);
        return response.getFacetFields();
    }

    @Override
    public SolrDocumentList getDocIdList(SolrQuery query) {
        query = query.getCopy();
        setDefaultSortField(query);
        query.setFields(FacetConstants.FIELD_ID);
        query.setFacet(false);
        query.setStart(0);
        query.setRows(Integer.MAX_VALUE);
        QueryResponse queryResponse = fireQuery(query);
        return queryResponse.getResults();
    }

    private void setDefaultSortField(SolrQuery query) {
        if (query.getSortField() == null) {
            query.setSort(SolrQuery.SortClause.asc(FacetConstants.FIELD_NAME));
        }
    }

}
