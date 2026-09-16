package eu.clarin.cmdi.vlo.service.solr.impl;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrDaoImpl {

    private final static Logger logger = LoggerFactory.getLogger(SolrDaoImpl.class);

    private final SolrClient solrClient;
    private final VloConfig vloConfig;

    private final String ID;
    private final MeterRegistry meterRegistry;

    public SolrDaoImpl(SolrClient solrClient, VloConfig vloConfig, FieldNameService fieldNameService, MeterRegistry meterRegistry) {
        this.solrClient = solrClient;
        this.vloConfig = vloConfig;
        this.ID = fieldNameService.getFieldName(FieldKey.ID) + ":";
        this.meterRegistry = meterRegistry;
    }

    protected SolrClient getSolrClient() {
        return solrClient;
    }

    protected NamedList<Object> fireRawQuery(QueryRequest req) {
        try {
            logger.debug("Executing raw query: {}", req);
            req.setBasicAuthCredentials(vloConfig.getSolrUserReadOnly(), vloConfig.getSolrUserReadOnlyPass());
            return solrClient.request(req);
        } catch (SolrServerException | IOException e) {
            logger.error("Error getting data:", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param query query to execute
     * @param type type of query, used to break the timings down;
     *             the set of query types should be bounded
     * @return the response
     */
    protected QueryResponse fireQuery(SolrQuery query, String type) {
        final long start = System.nanoTime();
        try {
            logger.debug("Executing query: {}", query);
            QueryRequest req = new QueryRequest(query);
            req.setBasicAuthCredentials(vloConfig.getSolrUserReadOnly(), vloConfig.getSolrUserReadOnlyPass());
            final QueryResponse response = req.process(solrClient);
            logger.trace("Response: {}", response);
            recordTimings(type, start, response.getQTime());
            return response;
        } catch(SolrException | SolrServerException e) {
            logger.error("Error getting data:", e);
            throw new RuntimeException(e);
        } catch(IOException e) {
            logger.error("IO error:", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Records how long a query took, we record solr's given query time and we also
     * measure the end to end query local time of handling the query (serialization,
     * network)
     */
    private void recordTimings(String type, long startNanos, int qTimeMillis) {
        final long elapsed = System.nanoTime() - startNanos;
        Timer.builder("solr.query.elapsed")
                .tag("type", type)
                .publishPercentiles(0.95, 0.99)
                .register(meterRegistry)
                .record(elapsed, TimeUnit.NANOSECONDS);
        Timer.builder("solr.query.qtime")
                .tag("type", type)
                .publishPercentiles(0.95, 0.99)
                .register(meterRegistry)
                .record(qTimeMillis, TimeUnit.MILLISECONDS);
    }
}
