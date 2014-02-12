/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.vlo.service.impl;

import java.io.File;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.util.AbstractSolrTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Example taken from
 * {@link http://blog.synyx.de/2011/01/integration-tests-for-your-solr-config/}
 *
 * @author twagoo
 */
public class SearchResultsDaoImplTest extends AbstractSolrTestCase {

    private EmbeddedSolrServer server;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        initCore(getResourcePath(getConfigString()), getResourcePath(getSchemaString()));
        server = new EmbeddedSolrServer(h.getCoreContainer(), h.getCore().getName());
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Test of getFacets method, of class SearchResultsDaoImpl.
     */
    @Test
    public void testGetFacets() throws Exception {
        SolrInputDocument document = new SolrInputDocument();
        document.addField("id", "1");
        document.addField("text", "test");

        server.add(document);
        server.commit();
        
        SolrParams params = new SolrQuery("text");
        QueryResponse response = server.query(params);
        assertEquals(1L, response.getResults().getNumFound());
        assertEquals("1", response.getResults().get(0).get("id"));
    }

    public static String getSchemaString() {
        return "/solr/collection1/conf/schema.xml";
    }

    public static String getConfigString() {
        return "/solr/collection1/conf/solrconfig.xml";
    }

    public static String getResourcePath(String resource) throws Exception {
        return new File(SearchResultsDaoImplTest.class.getResource(resource).toURI()).getAbsolutePath();
    }
}
