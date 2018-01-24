package eu.clarin.cmdi.vlo.solr;

import com.carrotsearch.ant.tasks.junit4.dependencies.com.google.common.collect.ImmutableList;
import com.carrotsearch.ant.tasks.junit4.dependencies.com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * Copyright (C) 2018 CLARIN
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
/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class SolrQueryTest extends SolrTestCaseJ4 {

    private SolrClient client;

    @BeforeClass
    public static void setUpClass() throws Exception {
        SolrTestCaseJ4.initCore(
                //config
                getResourcePath("/solr/vlo-index/solrconfig.xml"),
                //schema
                getResourcePath("/solr/vlo-index/conf/managed-schema"),
                //solr home
                getResourcePath("/solr"),
                //core name
                "vlo-index");
    }

    @Before
    @Override
    public void setUp() throws Exception {
        // set up an embedded solr server
        super.setUp();
        client = new EmbeddedSolrServer(h.getCoreContainer(), h.getCore().getName());
        client.add(getInputDocuments());
        client.commit();

        super.postSetUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.preTearDown();
        super.clearIndex();
        super.tearDown();
    }

    @Test
    public void testGetAllValues() throws Exception {
        final SolrDocumentList results = getResults(client, ImmutableMap.builder()
                .put("q", "*:*")
                .build()
        );
        assertEquals(2, results.size());
    }

    @Test
    public void testGetValues2() throws Exception {
        final SolrDocumentList results = getResults(client, ImmutableMap.builder()
                .put("q", "*:*")
                .build()
        );
        assertEquals(2, results.size());
    }

    private static SolrDocumentList getResults(SolrClient client, Map<String, String> params) throws SolrServerException, IOException {
        return client
                .query(new MapSolrParams(params))
                .getResults();
    }

    public static String getResourcePath(String resource) throws Exception {
        return new File(SolrQueryTest.class.getResource(resource).toURI()).getAbsolutePath();
    }

    private static ImmutableList getInputDocuments() {
        return ImmutableList.builder()
                .add(new SolrInputDocument("id", "1", "name", "test 1"))
                .add(new SolrInputDocument("id", "2", "name", "test 2"))
                .build();
    }

}
