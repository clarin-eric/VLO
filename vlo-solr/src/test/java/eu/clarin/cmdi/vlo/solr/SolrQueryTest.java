package eu.clarin.cmdi.vlo.solr;

import com.carrotsearch.ant.tasks.junit4.dependencies.com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
 * Tests query response behaviour given a fixed set of resources
 *
 * TODO: Add test for facets
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class SolrQueryTest extends SolrTestCaseJ4 {

    private static final String INPUT_DOCUMENTS_RESOURCE = "/documents.json";
    private static List<SolrInputDocument> INPUT_DOCUMENTS;

    private SolrClient client;

    @BeforeClass
    public static void setUpClass() throws Exception {
        INPUT_DOCUMENTS = getInputDocuments();

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
        client.add(INPUT_DOCUMENTS);
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
    public void testGetAllDocumentsCount() throws Exception {
        assertResultCount(58, "*:*");
    }

    @Test
    public void testQuery_AND() throws Exception {
        assertResultCount(20, "German");
        assertResultCount(1, "German Treebank");
        assertResultCount(1, "German AND Treebank");
        assertResultCount(1, "(German AND Treebank)");
        assertResultCount(1, "+German +Treebank");
        assertResultCount(1, "+(German Treebank)");

        assertResultCount(9, "language:German AND country:Germany");
    }

    @Test
    public void testQuery_OR() throws Exception {
        assertResultCount(9, "Corpus");
        assertResultCount(1, "Nederlands");
        assertResultCount(9, "Corpus OR Nederlands");
        assertResultCount(10, "Corpus OR Nederlands OR Treebank");
        assertResultCount(10, "(Corpus OR Nederlands OR Treebank)");
        assertResultCount(10, "Corpus OR (Nederlands OR Treebank)");
        assertResultCount(10, "(Corpus OR Nederlands) OR Treebank");

        assertResultCount(1, "country:Bulgaria");
        assertResultCount(5, "country:Netherlands");
        assertResultCount(6, "country:Netherlands OR country:Bulgaria");
        assertResultCount(6, "country:(Netherlands OR Bulgaria)");
    }

    @Test
    public void testQuery_NOT() throws Exception {
        assertResultCount(20, "German");
        assertResultCount(38, "-German");
        assertResultCount(38, "NOT German");

        assertResultCount(4, "French");
        assertResultCount(3, "French -*Tools*");

        assertResultCount(55, "-country:France");
        assertResultCount(55, "country:(NOT France)");
        assertResultCount(46, "-country:Germany -country:France");
        assertResultCount(46, "-country:(Germany OR France)");
    }

    @Test
    public void testQueryCounts() throws Exception {
        assertResultCount(14, "_resourceRefCount:0");
        assertResultCount(44, "_resourceRefCount:[1 TO *]");
    }

    @Test
    public void testFieldAlias() throws Exception {
        assertNotEquals(0, getResultCount("_resourceRefCount:0"));
        assertEquals(getResultCount("_resourceRefCount:0"), getResultCount("resources:0")); 
        
        assertNotEquals(0, getResultCount("_languageName:German"));
        assertEquals(getResultCount("_languageName:German"), getResultCount("language:German"));       
    }

    @Test
    public void testRankingForTermFrequency() throws Exception {
        SolrDocumentList resultList = getResults("important", 2);
        // term frequency of query terms boosts ranking of the containing document
        assertEquals("europeana_58_aggregation_47_europeana_47_9200377_47_BibliographicResource_3000115528936", resultList.get(0).getFieldValue("id"));
        assertEquals("hdl_58_11221_47_90D1-4400-4082-6-7_64_md_61_cmdi", resultList.get(1).getFieldValue("id"));
    }

    @Test
    public void testRankingForBoostedResources() throws Exception {
        SolrDocumentList resultList = getResults("Isold", 2);
        // having more CMDI resources (i.e. higher _resourceRefCount) boosts a document among otherwise identical documents
        assertTrue(Integer.parseInt(resultList.get(0).getFieldValue("_resourceRefCount").toString()) > Integer.parseInt(resultList.get(1).getFieldValue("_resourceRefCount").toString()));
    }

    @Test
    public void testRankingForBoostedAvailability() throws Exception {
        SolrDocumentList resultList = getResults("Staats=Geschichte", 2);
        // having an open license boosts a document among otherwise identical documents
        assertEquals("http_58__47__47_hdl.handle.net_47_10932_47_00-01B8-AE42-9CC4-E401-C", resultList.get(0).getFieldValue("id"));
    }

    @Test
    public void testRankingForBoostedDescription() throws Exception {
        SolrDocumentList resultList = getResults("name:ukr_newscrawl_2011_1M", 2);
        // having a description boosts a document among otherwise identical documents
        assertEquals("11022_47_0000-0000-7F86-B", resultList.get(0).getFieldValue("id"));
    }

    private void assertResultCount(long expectedCount, String query) throws SolrServerException, IOException {
        final long numFound = getResultCount(query);
        assertEquals(String.format("Expected %d results, actual result count is %d", expectedCount, numFound), expectedCount, numFound);
    }
    
    private long getResultCount(String query) throws SolrServerException, IOException {
        return getResults(query, 0).getNumFound();
    }

    private SolrDocumentList getResults(String query, int rows) throws SolrServerException, IOException {
        return getResults(client, ImmutableMap.builder()
                .put("q", query)
                .put("rows", rows)
                .put("fq", "{!collapse field=id}") //effictively 'de-collapse' to get reliable result counts
                .build()
        );
    }

    private static SolrDocumentList getResults(SolrClient client, Map<String, String> params) throws SolrServerException, IOException {
        return client
                .query(new MapSolrParams(params))
                .getResults();
    }

    public static String getResourcePath(String resource) throws Exception {
        return new File(SolrQueryTest.class.getResource(resource).toURI()).getAbsolutePath();
    }

    /**
     * Reads Solr input document definition from a json file
     *
     * @return
     */
    private static List<SolrInputDocument> getInputDocuments() throws IOException {
        try (InputStream inputStream = SolrQueryTest.class.getResourceAsStream(INPUT_DOCUMENTS_RESOURCE)) {
            return SolrInputDataCreator.getDocumentsFromJson(inputStream);
        }
    }

}
