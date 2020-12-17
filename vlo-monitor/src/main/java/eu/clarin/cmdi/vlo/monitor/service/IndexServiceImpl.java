package eu.clarin.cmdi.vlo.monitor.service;

import util.PreemptiveAuthInterceptor;
import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Service;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Service
@Slf4j
public class IndexServiceImpl implements IndexService {
    
    @Inject
    private VloConfig config;
    
    private SolrClient solrClient;
    
    @Override
    public Map<String, Long> getValueCounts(String facet) {
        final SolrQuery addFacetField = new SolrQuery().addFacetField(facet);
        try {
            final QueryResponse response = solrClient.query(addFacetField);
            final ImmutableMap.Builder<String, Long> mapBuilder = ImmutableMap.builder();
            response.getFacetFields().stream()
                    // find counts for target field
                    .filter(f -> facet.equals(f.getName()))
                    // get values (=list of counts)
                    .map(f -> f.getValues())
                    // there should be only 1 per field
                    .limit(1)
                    // put counts in our map
                    .forEach((values) -> {
                        values.forEach(count -> {
                            mapBuilder.put(count.getName(), count.getCount());
                        });
                    });
            return mapBuilder.build();
        } catch (SolrServerException | IOException ex) {
            log.error("Error while querying for facet {}", facet, ex);
            throw new RuntimeException(ex);
        }
    }
    
    @PostConstruct
    protected void initSolrClient() {
        final String solrUrl = config.getSolrUrl();
        final int nThreads = config.getSolrThreads();
        log.info("Initializing concurrent Solr Server on {} with {} threads", solrUrl, nThreads);

        // setting credentials for connection and enabling preemptive authentication
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(config.getSolrUserReadWrite(), config.getSolrUserReadWritePass()));
        CloseableHttpClient httpClient = HttpClientBuilder.create().addInterceptorFirst(new PreemptiveAuthInterceptor()).setDefaultCredentialsProvider(credentialsProvider).build();

        /* Specify the number of documents in the queue that will trigger the
         * threads, two of them, emptying it.
         */
        solrClient = new ConcurrentUpdateSolrClient(new ConcurrentUpdateSolrClient.Builder(solrUrl)
                .withQueueSize(config.getMinDocsInSolrQueue()).withThreadCount(nThreads).withHttpClient(httpClient)) {
        };
    }
    
    @PreDestroy
    protected void closeSolrClient() {
        try {
            solrClient.close();
        } catch (IOException ex) {
            log.error("Error while closing Solr client", ex);
        }
    }
    
}
