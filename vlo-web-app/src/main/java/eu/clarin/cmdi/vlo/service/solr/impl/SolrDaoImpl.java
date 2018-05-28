package eu.clarin.cmdi.vlo.service.solr.impl;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.IOException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrDaoImpl {

    private final static Logger logger = LoggerFactory.getLogger(SolrDaoImpl.class);
   
    private final SolrClient solrClient;
    private final VloConfig vloConfig;
    
    private final String ID;

    public SolrDaoImpl(SolrClient solrClient, VloConfig vloConfig, FieldNameService fieldNameService) {
        this.solrClient = solrClient;
        this.vloConfig = vloConfig;
        this.ID = fieldNameService.getFieldName(FieldKey.ID) + ":";
    }

    protected SolrClient getSolrClient() {
        return solrClient;
    }

    /**
     * Basic sanitising of Solr queries.
     *
     * TODO: Move this to QueryFacetSelection level??
     *
     * Query is based on the URL to the VLO web application. Also, explain about
     * the URL and ?fq=language:dutch Assume filters have the form a:b like for
     * example language:dutch
     *
     * @param query
     * @return
     */
    protected SolrQuery sanitise(SolrQuery query) {

        // String [] facetsFromConfig; 
        // try and get the filters facets from the query
        String[] filtersInQuery;
        filtersInQuery = query.getFilterQueries();
        
        if (filtersInQuery == null) {
            // the query does not contain filters
        } else {
            // get the facets from the configuration file
            // facetsFromConfig = VloConfig.getFacetFields();

            // check the filters in the query by name
            for (String filter : filtersInQuery) {
                // split up a filter, look at the string preceeding the semicolon 
                String facetInFilter = filter.split(":")[0]
                        .replaceFirst("^\\{!tag=.*\\}", ""); //strip out tag syntax (may be used for OR queries, see query factory implementation)
                
                if (vloConfig.getFields().values().contains(facetInFilter)) {
                    // facet in the filter is in the set that is defined by the config file
                } else {
                    if (facetInFilter.startsWith("_")) {
                        // this facet is hidden, do not consider it
                    } else {
                        // the filter name does not match a facet in the facet
                        query.removeFilterQuery(filter);
                    }
                }
            }
        }

        // finally, return the sanitised query
        return query;
    }

    protected QueryResponse fireQuery(SolrQuery query) {
        try {
            logger.debug("Executing query: {}", query);
            QueryRequest req = new QueryRequest(query);
            req.setBasicAuthCredentials(vloConfig.getSolrUserReadOnly(), vloConfig.getSolrUserReadOnlyPass());
            final QueryResponse response = req.process(solrClient);
            logger.trace("Response: {}", response);
            return response;
        } catch(SolrException | SolrServerException e) {
            logger.error("Error getting data:", e);
            throw new RuntimeException(e);
        } catch(IOException e) {
            logger.error("IO error:", e);
            throw new RuntimeException(e);
        }
    }

    public SolrDocument getSolrDocument(String docId) {
        if (docId == null) {
            throw new NullPointerException("Cannot get SOLR document for null docId");
        }
        SolrDocument result = null;
        SolrQuery query = new SolrQuery();
        query.setQuery(ID + ClientUtils.escapeQueryChars(docId));
        query.setFields("*");
        SolrDocumentList docs = fireQuery(query).getResults();
        if (docs.getNumFound() > 1) {
            logger.error("Error: found multiple documents for id (will return first one): " + docId + " \nDocuments found: " + docs);
            result = docs.get(0);
        } else if (docs.getNumFound() == 1) {
            result = docs.get(0);
        }
        return result;
    }
}
