package eu.clarin.cmdi.vlo.service.solr.impl;

import eu.clarin.cmdi.vlo.service.solr.SearchResultsDao;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchResultsDaoImpl extends SolrDaoImpl implements SearchResultsDao {

    private final static Logger logger = LoggerFactory.getLogger(SearchResultsDaoImpl.class);

    public SearchResultsDaoImpl(SolrClient solrClient, VloConfig config, FieldNameService fieldNameService, MeterRegistry meterRegistry) {
        super(solrClient, config, fieldNameService, meterRegistry);
    }

    @Override
    public List<FacetField> getFacets(SolrQuery query) {
        final QueryResponse response = fireQuery(query, "facets");
        final List<FacetField> facetFields = response.getFacetFields();
        if (logger.isDebugEnabled()) {
            if (facetFields.size() == 1) {
                final FacetField field = facetFields.get(0);
                logger.debug("Found facet field '{}' with {} values", field.getName(), field.getValueCount());
            } else {
                logger.debug("Found {} facet fields", facetFields.size());
            }
        }
        return facetFields;
    }

    @Override
    public SolrDocumentList getDocuments(SolrQuery query) {
        QueryResponse queryResponse = fireQuery(query, "documents");
        final SolrDocumentList documents = queryResponse.getResults();
        if (documents != null) {
            logger.debug("Found {} documents", documents.getNumFound());
            return documents;
        } else {
            logger.warn("Null result for query {}", query);
            return new SolrDocumentList();
        }
    }

    @Override
    public QueryResponse getQueryResponse(SolrQuery query) {
        return fireQuery(query, "expansion");
    }
}
