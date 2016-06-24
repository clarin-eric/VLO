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
package eu.clarin.cmdi.vlo.service.solr.impl;

import static eu.clarin.cmdi.vlo.FacetConstants.FIELD_COLLECTION;
import static eu.clarin.cmdi.vlo.FacetConstants.FIELD_COUNTRY;
import static eu.clarin.cmdi.vlo.FacetConstants.FIELD_DESCRIPTION;
import static eu.clarin.cmdi.vlo.FacetConstants.FIELD_ID;
import static eu.clarin.cmdi.vlo.FacetConstants.FIELD_NAME;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.util.AbstractSolrTestCase;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CMDIData;

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
        // set up an embedded solr server
        super.setUp();
        initCore(getResourcePath(getConfigString()), getResourcePath(getSchemaString()));
        server = new EmbeddedSolrServer(h.getCoreContainer(), h.getCore().getName());
        instance = new SearchResultsDaoImpl(server, new VloConfig() {

            @Override
            public List<String> getFacetsInSearch() {
                return ImmutableList.of(FIELD_COLLECTION, FIELD_COUNTRY);
            }

        });

        // add some documents
        int id = 1;
        CMDIData cmdiData = new CMDIData();
        cmdiData.addDocField(FIELD_COLLECTION, "Collection1", false);
        cmdiData.addDocField(FIELD_COUNTRY, "Country1", false);
        SolrInputDocument document = cmdiData.getSolrDocument();
        document.addField("id", Integer.toString(id++));
        server.add(document);

        cmdiData = new CMDIData();
        cmdiData.addDocField(FIELD_COLLECTION, "Collection1", false);
        cmdiData.addDocField(FIELD_COUNTRY, "Country2", false);
        document = cmdiData.getSolrDocument();
        document.addField("id", Integer.toString(id++));
        server.add(document);

        server.commit();
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
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testGetFacets() throws Exception {
        final SolrQuery query = new SolrQuery();
        query.setRows(10);
        query.setStart(0);
        query.setFields(FIELD_NAME, FIELD_ID, FIELD_DESCRIPTION);
        query.setQuery(null);

        query.setFacet(true);
        query.setFacetMinCount(1);
        query.addFacetField(FIELD_COLLECTION, FIELD_COUNTRY);

        List<FacetField> facetFields = instance.getFacets(query);
        assertNotNull(facetFields);
        assertEquals(2, facetFields.size());

        // 1 collection
        assertThat(facetFields, Matchers.<FacetField>hasItem(Matchers.allOf(
                Matchers.<FacetField>hasProperty("name", equalTo(FIELD_COLLECTION)),
                Matchers.<FacetField>hasProperty("valueCount", equalTo(1)))));

        // 2 countries
        assertThat(facetFields, Matchers.<FacetField>hasItem(Matchers.allOf(
                Matchers.<FacetField>hasProperty("name", equalTo(FIELD_COUNTRY)),
                Matchers.<FacetField>hasProperty("valueCount", equalTo(2)))));
    }

    @Test
    public void testGetDocuments() {
        // get all documents
        SolrQuery query = new SolrQuery();
        query.setRows(10);
        query.setStart(0);
        query.setFields(FIELD_NAME, FIELD_ID, FIELD_DESCRIPTION);
        {
            // all documents should match this
            SolrDocumentList documents = instance.getDocuments(query);
            assertEquals(2, documents.getNumFound());
        }

        // get document with specific field value
        query.setQuery(FIELD_COUNTRY + ":Country1");
        {
            // only document with id "1" should match this
            SolrDocumentList documents = instance.getDocuments(query);
            assertEquals(1, documents.getNumFound());
            assertEquals("1", documents.get(0).getFieldValue(FIELD_ID));
        }

        query.setFilterQueries(FIELD_COLLECTION + ":Collection2");
        {
            // no matches
            SolrDocumentList documents = instance.getDocuments(query);
            assertEquals(0, documents.getNumFound());
        }
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
