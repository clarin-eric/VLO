/*
 * Copyright (C) 2019 CLARIN
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
package eu.clarin.vlo.sitemap.services;

import eu.clarin.vlo.sitemap.gen.Config;
import java.io.IOException;
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
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
class SolrHelpers {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrHelpers.class);

    static QueryResponse getSolrResult(SolrClient solrClient, SolrQuery recordsQuery) throws RuntimeException {
        try {
            QueryResponse response = solrClient.query(recordsQuery);
            return response;
        } catch (SolrServerException | IOException ex) {
            LOGGER.error("Fatal exception while querying index", ex);
            throw new RuntimeException(ex);
        }
    }

    static SolrClient createSolrClient(final String solrUrl) {
        LOGGER.info("Initializing Solr client on {}", solrUrl);
        // setting credentials for connection and enabling preemptive authentication
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(Config.SOLR_USER, Config.SOLR_PASS));
        CloseableHttpClient httpClient = HttpClientBuilder.create().addInterceptorFirst(new PreemptiveAuthInterceptor()).setDefaultCredentialsProvider(credentialsProvider).build();
        return new HttpSolrClient(new HttpSolrClient.Builder(solrUrl).withHttpClient(httpClient)) {
        };
    }

    private static class PreemptiveAuthInterceptor implements HttpRequestInterceptor {

        @Override
        public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
            AuthState authState = (AuthState) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);
            // If no auth scheme available yet, try to initialize it
            // preemptively
            if (authState.getAuthScheme() == null) {
                CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(HttpClientContext.CREDS_PROVIDER);
                Credentials creds = credsProvider.getCredentials(AuthScope.ANY);
                if (creds == null) {
                    throw new HttpException("No credentials for preemptive authentication");
                }
                authState.update(new BasicScheme(), creds);
            }
        }
    }

}
