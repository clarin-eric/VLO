package eu.clarin.cmdi.vlo.service.impl;

import eu.clarin.cmdi.vlo.config.VloConfig;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrDaoImpl {

    private final static Logger LOG = LoggerFactory.getLogger(SolrDaoImpl.class);
    private final HttpSolrServer solrServer;

    public SolrDaoImpl(String solrUrl) {
        solrServer = new HttpSolrServer(solrUrl);
    }

    protected HttpSolrServer getSolrserver() {
        return solrServer;
    }

    /**
     * Basic sanitising of Solr queries.
     *
     * Query is based on the URL to the VLO web application. Also, explain about
     * the URL and ?fq=language:dutch Assume filters have the form a:b like for
     * example language:dutch
     *
     * @param query
     * @return
     */
    private SolrQuery sanitise(SolrQuery query) {

        // String [] facetsFromConfig; 
        // try and get the filters facets from the query
        String[] filtersInQuery;
        filtersInQuery = query.getFilterQueries();

        if (filtersInQuery == null) {
            // the query does not contain filters
        } else {
            // get the facets from the configuration file
            // facetsFromConfig = VloConfig.getFacetFields();

            // present the facets from the config file as a list to a new set
            Set<String> facetsDefined;
            facetsDefined = new HashSet<String>(Arrays.asList(VloConfig.getFacetFields()));

            // check the filters in the query by name
            for (String filter : filtersInQuery) {
                // split up a filter, look at the string preceeding the semicolon 
                String facetInFilter = filter.split(":")[0];

                if (facetsDefined.contains(facetInFilter)) {
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
        SolrQuery sanitisedQuery;
        sanitisedQuery = sanitise(query);
        try {
            return solrServer.query(sanitisedQuery);
        } catch (SolrServerException e) {
            LOG.error("Error getting data:", e);
            throw new RuntimeException(e);
        }
    }

    public SolrDocument getSolrDocument(String docId) {
        if (docId == null) {
            throw new NullPointerException("Cannot get SOLR document for null docId");
        }
        SolrDocument result = null;
        SolrQuery query = new SolrQuery();
        query.setQuery("id:" + ClientUtils.escapeQueryChars(docId));
        query.setFields("*");
        SolrDocumentList docs = fireQuery(query).getResults();
        if (docs.getNumFound() > 1) {
            LOG.error("Error: found multiple documents for id (will return first one): " + docId + " \nDocuments found: " + docs);
            result = docs.get(0);
        } else if (docs.getNumFound() == 1) {
            result = docs.get(0);
        }
        return result;
    }
}
