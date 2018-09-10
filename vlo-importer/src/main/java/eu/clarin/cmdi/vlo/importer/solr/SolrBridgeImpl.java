/*
 * Copyright (C) 2017 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.importer.solr;

import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
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
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Solr bridge that adds documents directly to the SolrClient instance
 *
 * @author twagoo
 */
public class SolrBridgeImpl implements SolrBridge {

    private final static Logger LOG = LoggerFactory.getLogger(SolrBridgeImpl.class);

    private final VloConfig config;
    private final ThreadLocal<Throwable> serverError = new ThreadLocal<>();

    private SolrClient solrClient;

    private boolean commit = true;

    public SolrBridgeImpl(VloConfig config) {
        this.config = config;
    }

    /**
     * Create an interface to the SOLR server.
     *
     * After the interface has been created the importer can send documents to
     * the server. Sending documents involves a queue. The importer adds
     * documents to a queue, and dedicated threads will empty it, and
     * effectively store store the documents.
     *
     * @throws MalformedURLException
     */
    @Override
    public void init() throws MalformedURLException {
        final String solrUrl = config.getSolrUrl();
        final int nThreads = config.getSolrThreads();
        LOG.info("Initializing concurrent Solr Server on {} with {} threads", solrUrl, nThreads);

        // setting credentials for connection and enabling preemptive authentication
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(config.getSolrUserReadWrite(), config.getSolrUserReadWritePass()));
        CloseableHttpClient httpClient = HttpClientBuilder.create().addInterceptorFirst(new PreemptiveAuthInterceptor()).setDefaultCredentialsProvider(credentialsProvider).build();

        /* Specify the number of documents in the queue that will trigger the
         * threads, two of them, emptying it.
         */
        solrClient = new ConcurrentUpdateSolrClient(new ConcurrentUpdateSolrClient.Builder(solrUrl)
                .withQueueSize(config.getMinDocsInSolrQueue()).withThreadCount(nThreads).withHttpClient(httpClient)) {
            /*
                     * Let the super class method handle exceptions. Make the
                     * exception available to the importer in the form of the
                     * serverError variable.
             */
            @Override
            public void handleError(Throwable exception) {
                super.handleError(exception);
                serverError.set(exception);
                onSolrClientError(exception);
            }
        };
    }

    @Override
    public void addDocument(SolrInputDocument doc) throws IOException, DocumentStoreException {
        try {
            solrClient.add(doc);
        } catch (SolrServerException ex) {
            throw new DocumentStoreException(ex);
        }
    }

    @Override
    public void addDocuments(Collection<SolrInputDocument> docs) throws IOException, DocumentStoreException {
        try {
            solrClient.add(docs);
        } catch (SolrServerException ex) {
            throw new DocumentStoreException(ex);
        }
    }

    @Override
    public void commit() throws SolrServerException, IOException {
        if (commit) {
            LOG.info("Manual commit");
            solrClient.commit();
        } else {
            LOG.debug("Commit requested but skipping because commit == false");
        }
    }

    @Override
    public Throwable popError() {
        final Throwable error = serverError.get();
        serverError.remove();
        return error;
    }

    @Override
    public void shutdown() throws SolrServerException, IOException {
        //commit before shutdown
        solrClient.commit();
        solrClient.close();
    }

    @Override
    public SolrClient getClient() {
        return solrClient;
    }

    public void setCommit(boolean commit) {
        this.commit = commit;
    }

    protected void onSolrClientError(Throwable exception) {
        //do nothing
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
