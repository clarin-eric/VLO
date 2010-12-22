package eu.clarin.cmdi.vlo.dao;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import eu.clarin.cmdi.vlo.FacetConstants;

public class SearchResultsDao extends SolrDao {

    //private final static Logger LOG = LoggerFactory.getLogger(SearchResultsDao.class);

    private QueryResponse response;

    public SearchResultsDao() {
        super();
    }

    public SolrDocumentList getResults(SolrQuery query) {
        response = fireQuery(query);
        SolrDocumentList results = response.getResults();
        return results;
    }

    public List<FacetField> getFacets(SolrQuery query) {
        response = fireQuery(query);
        return response.getFacetFields();
    }

    public SolrDocumentList getDocIdList(SolrQuery query) {
        query.setFields(FacetConstants.FIELD_ID);
        query.setFacet(false);
        query.setStart(0);
        query.setRows(Integer.MAX_VALUE);
        QueryResponse queryResponse = fireQuery(query);
        return queryResponse.getResults();
    }

}
