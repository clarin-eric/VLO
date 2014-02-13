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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import java.io.File;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.util.AbstractSolrTestCase;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;

/**
 * Example taken from
 * {@link http://blog.synyx.de/2011/01/integration-tests-for-your-solr-config/}
 *
 * @author twagoo
 */
public class SearchResultsDaoImplTest extends AbstractSolrTestCase {

    private EmbeddedSolrServer server;
    private SearchResultsDaoImpl instance;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        initCore(getResourcePath(getConfigString()), getResourcePath(getSchemaString()));
        server = new EmbeddedSolrServer(h.getCoreContainer(), h.getCore().getName());
        instance = new SearchResultsDaoImpl(server, new VloConfig());
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
        int id = 1;
        CMDIData cmdiData = new CMDIData();
        cmdiData.addDocField(FacetConstants.FIELD_COLLECTION, "Collection1", false);
        cmdiData.addDocField(FacetConstants.FIELD_COUNTRY, "Country1", false);
        SolrInputDocument document = cmdiData.getSolrDocument();
        document.addField("id", Integer.toString(id++));
        server.add(document);
        server.commit();

        cmdiData = new CMDIData();
        cmdiData.addDocField(FacetConstants.FIELD_COLLECTION, "Collection1", false);
        cmdiData.addDocField(FacetConstants.FIELD_COUNTRY, "Country2", false);
        document = cmdiData.getSolrDocument();
        document.addField("id", Integer.toString(id++));
        server.add(document);
        server.commit();

        SolrQuery query = new SolrQuery();
        query.setRows(10);
        query.setStart(0);
        query.setFields(FacetConstants.FIELD_NAME, FacetConstants.FIELD_ID, FacetConstants.FIELD_DESCRIPTION);
        query.setQuery("*:*");

        query.setFacet(true);
        query.setFacetMinCount(1);
        query.addFacetField(FacetConstants.FIELD_COLLECTION, FacetConstants.FIELD_COUNTRY);

        List<FacetField> facetFields = instance.getFacets(query);
        assertNotNull(facetFields);
        assertEquals(2, facetFields.size());

        // 1 collection
        assertThat(facetFields, Matchers.<FacetField>hasItem(Matchers.allOf(
                Matchers.<FacetField>hasProperty("name", equalTo(FacetConstants.FIELD_COLLECTION)),
                Matchers.<FacetField>hasProperty("valueCount", equalTo(1)))));

        // 2 countries
        assertThat(facetFields, Matchers.<FacetField>hasItem(Matchers.allOf(
                Matchers.<FacetField>hasProperty("name", equalTo(FacetConstants.FIELD_COUNTRY)),
                Matchers.<FacetField>hasProperty("valueCount", equalTo(2)))));
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
