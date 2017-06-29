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
import java.util.ArrayList;
import java.util.Collection;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of the default SolrBridge implementation that 'buffers' documents
 * and only sends them to the server once the buffer is full.
 *
 * @see VloConfig#getMaxDocsInList()
 * @author twagoo
 */
public class BufferingSolrBridgeImpl extends SolrBridgeImpl {

    private final static Logger LOG = LoggerFactory.getLogger(BufferingSolrBridgeImpl.class);

    private final VloConfig config;

    private final Collection<SolrInputDocument> buffer = new ArrayList<>();

    public BufferingSolrBridgeImpl(VloConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    public synchronized void addDocument(SolrInputDocument doc) throws SolrServerException, IOException {
        buffer.add(doc);
        submitIfBufferFull();
    }

    @Override
    public synchronized void addDocuments(Collection<SolrInputDocument> docs) throws SolrServerException, IOException {
        buffer.addAll(docs);
        submitIfBufferFull();
    }

    @Override
    public void shutdownServer() throws SolrServerException, IOException {
        if (buffer.size() > 0) {
            LOG.info("Shutdown requested. Sending {} remaining documents to Solr", buffer.size());
            submitBuffer();
            commit();
        }
        super.shutdownServer();
    }

    private synchronized void submitIfBufferFull() throws SolrServerException, IOException {
        if (buffer.size() >= config.getMaxDocsInList()) {
            LOG.info("Buffer saturated. Sending {} documents to Solr", buffer.size());
            submitBuffer();
        }
    }

    protected synchronized void submitBuffer() throws IOException, SolrServerException {
        getServer().add(buffer);
        buffer.clear();
    }

}
