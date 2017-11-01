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
import java.util.Collection;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

/**
 *
 * @author twagoo
 */
public interface SolrBridge {

    SolrClient getClient();

    void init() throws MalformedURLException;

    void shutdown() throws SolrServerException, IOException;

    void addDocument(SolrInputDocument doc) throws SolrServerException, IOException;

    void addDocuments(Collection<SolrInputDocument> docs) throws SolrServerException, IOException;

    void commit() throws SolrServerException, IOException;

    Throwable popError();
}
