package eu.clarin.cmdi.vlo.dao;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

public class SearchResultsDao extends SolrDao {

    //private final static Logger LOG = LoggerFactory.getLogger(SearchResultsDao.class);

    private QueryResponse response;

    public SearchResultsDao() { //TODO Patrick doing query twice not good.
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

}
