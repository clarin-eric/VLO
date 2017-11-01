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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class DummySolrBridgeImpl implements SolrBridge {

    protected final static org.slf4j.Logger LOG = LoggerFactory.getLogger(DummySolrBridgeImpl.class);

    public DummySolrBridgeImpl() {
    }
    private final List<SolrInputDocument> result = new ArrayList<>();

    public List<SolrInputDocument> getDocuments() {
        return result;
    }

    @Override
    public SolrClient getClient() {
        return new SolrClient() {
            @Override
            public NamedList<Object> request(SolrRequest request, String collection) throws SolrServerException, IOException {
                //do nothing
                LOG.debug("SolrRequest to dummy server on collection '{}': {}", collection, request);
                return new NamedList<>();
            }

            @Override
            public void close() {
                LOG.debug("Dummy solr client shutdown");
            }
        };
    }

    @Override
    public void init() throws MalformedURLException {
        LOG.debug("Dummy solr bridge init");
    }

    @Override
    public void shutdown() {
        LOG.debug("Dummy solr bridge shutdown");
    }

    @Override
    public void addDocument(SolrInputDocument doc) throws SolrServerException, IOException {
        result.add(doc);
    }

    @Override
    public void addDocuments(Collection<SolrInputDocument> docs) throws SolrServerException, IOException {
        result.addAll(docs);
    }

    @Override
    public void commit() throws SolrServerException, IOException {
        LOG.debug("Dummy solr bridge commit");
    }

    @Override
    public Throwable popError() {
        return null;
    }

}
