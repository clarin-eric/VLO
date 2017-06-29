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
package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class SolrBridgeImpl implements SolrBridge {

    private final static Logger LOG = LoggerFactory.getLogger(SolrBridgeImpl.class);
    
    private final VloConfig config;
    private final ThreadLocal<Throwable> serverError = new ThreadLocal<>();

    private SolrServer solrServer;

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

        /* Specify the number of documents in the queue that will trigger the
         * threads, two of them, emptying it.
         */
        solrServer = new ConcurrentUpdateSolrServer(solrUrl,
                config.getMinDocsInSolrQueue(), nThreads) {
            /*
                     * Let the super class method handle exceptions. Make the
                     * exception available to the importer in the form of the
                     * serverError variable.
             */
            @Override
            public void handleError(Throwable exception) {
                super.handleError(exception);
                serverError.set(exception);
            }
        };
    }

    @Override
    public void addDocument(SolrInputDocument doc) throws SolrServerException, IOException {
        solrServer.add(doc);
    }

    @Override
    public void addDocuments(Collection<SolrInputDocument> docs) throws SolrServerException, IOException {
        solrServer.add(docs);
    }

    @Override
    public void commit() throws SolrServerException, IOException {
        solrServer.commit();
    }

    @Override
    public Throwable popError() {
        final Throwable error = serverError.get();
        serverError.remove();
        return error;
    }
    
    @Override
    public void shutdownServer() {
        solrServer.shutdown();
    }

    @Override
    public SolrServer getServer() {
        return solrServer;
    }

}
