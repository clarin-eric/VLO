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

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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

    private static final double BUFFER_SIZE_FACTOR = 1.2;

    private final FieldNameService fieldNameService;

    private final Collection<SolrInputDocument> buffer;
    private final int flushSize;
    private final AtomicInteger submitCount = new AtomicInteger();

    public BufferingSolrBridgeImpl(VloConfig config) {
        super(config);

        this.fieldNameService = new FieldNameServiceImpl(config);

        //flush size determines how often the buffered is submitted to Solr
        this.flushSize = config.getMaxDocsInList();

        //initialize buffer with capacity based on flush size
        this.buffer = new ArrayList<>((int) (BUFFER_SIZE_FACTOR * flushSize));

        LOG.info("Buffered will be submitted to the Solr server if it contains {} or more documents", flushSize);
    }

    @Override
    public void init() throws MalformedURLException {
        super.init();
        submitCount.set(0);
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
    public synchronized void commit() throws SolrServerException, IOException {
        submitAllInBuffer();
        super.commit();
    }

    @Override
    public void shutdown() throws SolrServerException, IOException {
        LOG.info("Shutdown requested");
        this.commit();
        LOG.info("{} committed submits in lifespan", submitCount.get());
        super.shutdown();
    }

    private synchronized void submitIfBufferFull() throws SolrServerException, IOException {
        if (buffer.size() >= flushSize) {
            LOG.debug("Buffer saturated. Sending {} documents to Solr", buffer.size());
            submitBuffer();
        }
    }

    protected void submitAllInBuffer() throws IOException, SolrServerException {
        if (!buffer.isEmpty()) {
            LOG.debug("Sending {} remaining documents to Solr", buffer.size());
            submitBuffer();
        }
    }

    protected synchronized void submitBuffer() throws IOException, SolrServerException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Submitting buffer. Document ids: {}", getBufferDocIds());
        }
        getClient().add(buffer);
        LOG.trace("Submit count total: {}", submitCount.addAndGet(buffer.size()));
        buffer.clear();
    }

    private String getBufferDocIds() {
        final String idField = fieldNameService.getFieldName(FieldKey.ID);
        return buffer.stream()
                //extract IDs
                .map((doc) -> {
                    return doc.getFieldValue(idField).toString();
                })
                //join ID strings
                .collect(Collectors.joining(", "));
    }
}
