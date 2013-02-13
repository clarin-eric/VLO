package eu.clarin.cmdi.vlo.dao;

import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.Configuration;
import eu.clarin.cmdi.vlo.config.WebAppConfig;

public class SolrDao {

    private final static Logger LOG = LoggerFactory.getLogger(SolrDao.class);
    private final CommonsHttpSolrServer solrServer;

    public SolrDao() {
        String solrUrl = WebAppConfig.open().getSolrUrl();
        try {
            solrServer = new CommonsHttpSolrServer(solrUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL: " + solrUrl, e);
        }
    }

    protected CommonsHttpSolrServer getSolrserver() {
        return solrServer;
    }

    protected QueryResponse fireQuery(SolrQuery query) {
        try {
            return solrServer.query(query);
        } catch (SolrServerException e) {
            LOG.error("Error getting data:", e);
            throw new RuntimeException(e);
        }
    }

    public SolrDocument getSolrDocument(String docId) {
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
