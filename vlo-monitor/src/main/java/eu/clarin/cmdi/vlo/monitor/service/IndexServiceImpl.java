package eu.clarin.cmdi.vlo.monitor.service;

import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.IOException;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
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

    @PostConstruct
    protected void postConstruct() {
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
    protected void preDestroy() {
        try {
            solrClient.close();
        } catch (IOException ex) {
            log.error("Error while closing Solr client", ex);
        }
    }

    @Override
    public Map<String, Integer> getValueCounts(String facet) {
        final SolrQuery addFacetField = new SolrQuery().addFacetField(facet);
        try {
            final QueryResponse response = solrClient.query(addFacetField);
            ImmutableMap.Builder<String, Integer> mapBuilder = ImmutableMap.builder();
            response.getFacetFields()
                    .forEach(f -> {
                        mapBuilder.put(f.getName(), f.getValueCount());
                    });
            return mapBuilder.build();
        } catch (SolrServerException | IOException ex) {
            log.error("Error while querying for facet {}", facet, ex);
            throw new RuntimeException(ex);
        }
    }

    class PreemptiveAuthInterceptor implements HttpRequestInterceptor {

        @Override
        public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
            AuthState authState = (AuthState) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);

            // If no auth scheme available yet, try to initialize it
            // preemptively
            if (authState.getAuthScheme() == null) {
                CredentialsProvider credsProvider = (CredentialsProvider) context
                        .getAttribute(HttpClientContext.CREDS_PROVIDER);
                Credentials creds = credsProvider.getCredentials(AuthScope.ANY);
                if (creds == null) {
                    throw new HttpException("No credentials for preemptive authentication");
                }
                authState.update(new BasicScheme(), creds);
            }
        }
    }

}
