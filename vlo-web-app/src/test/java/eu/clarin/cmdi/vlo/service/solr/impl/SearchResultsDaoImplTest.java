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

import eu.clarin.cmdi.vlo.FieldKey;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import org.apache.solr.SolrTestCaseJ4;

import org.junit.BeforeClass;

/**
 * Example taken from
 * {@link http://blog.synyx.de/2011/01/integration-tests-for-your-solr-config/}
 *
 * @author twagoo
 */
public class SearchResultsDaoImplTest extends SolrTestCaseJ4 {

    private EmbeddedSolrServer server;
    private SearchResultsDaoImpl instance;
    FieldNameService fieldNameService;

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
        this.fieldNameService = new FieldNameServiceImpl(DefaultVloConfigFactory.configureDefaultMappingLocations(new DefaultVloConfigFactory().newConfig()));
        VloConfig vloConfig = new VloConfig() {

            @Override
            public List<String> getFacetsInSearch() {
                return ImmutableList.of(fieldNameService.getFieldName(FieldKey.COLLECTION), fieldNameService.getFieldName(FieldKey.COUNTRY));
            }

        };
        

        // set up an embedded solr server
        super.setUp();
        server = new EmbeddedSolrServer(h.getCoreContainer(), h.getCore().getName());
        instance = new SearchResultsDaoImpl(server, vloConfig, fieldNameService);

        // add some documents
        int id = 1;
        CMDIData cmdiData = new CMDIData(fieldNameService);
        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.COLLECTION), "Collection1", false);
        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.COUNTRY), "Country1", false);
        SolrInputDocument document = cmdiData.getSolrDocument();
        document.addField("id", Integer.toString(id++));
        server.add(document);

        cmdiData = new CMDIData(fieldNameService);
        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.COLLECTION), "Collection2", false);
        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.COUNTRY), "Country2", false);
        document = cmdiData.getSolrDocument();
        document.addField("id", Integer.toString(id++));
        server.add(document);

        server.commit();

        super.postSetUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.preTearDown();
        super.clearIndex();
        super.tearDown();
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
        query.setFields(fieldNameService.getFieldName(FieldKey.NAME), fieldNameService.getFieldName(FieldKey.ID), fieldNameService.getFieldName(FieldKey.DESCRIPTION));
        query.setQuery(null);

        query.setFacet(true);
        query.setFacetMinCount(1);
        query.addFacetField(fieldNameService.getFieldName(FieldKey.COLLECTION), fieldNameService.getFieldName(FieldKey.COUNTRY));

        List<FacetField> facetFields = instance.getFacets(query);
        assertNotNull(facetFields);
        assertEquals(2, facetFields.size());

        // 2 collections
        assertThat(facetFields, Matchers.<FacetField>hasItem(Matchers.allOf(
                Matchers.<FacetField>hasProperty("name", equalTo(fieldNameService.getFieldName(FieldKey.COLLECTION))),
                Matchers.<FacetField>hasProperty("valueCount", equalTo(2)))));

        // 2 countries
        assertThat(facetFields, Matchers.<FacetField>hasItem(Matchers.allOf(
                Matchers.<FacetField>hasProperty("name", equalTo(fieldNameService.getFieldName(FieldKey.COUNTRY))),
                Matchers.<FacetField>hasProperty("valueCount", equalTo(2)))));
    }

    @Test
    public void testGetDocuments() {
        // get all documents
        SolrQuery query = new SolrQuery();
        query.setRows(10);
        query.setStart(0);
        query.setFields(fieldNameService.getFieldName(FieldKey.NAME), fieldNameService.getFieldName(FieldKey.ID), fieldNameService.getFieldName(FieldKey.DESCRIPTION));
        {
            // all documents should match this
            SolrDocumentList documents = instance.getDocuments(query);
            assertEquals(2, documents.getNumFound());
        }

        // get document with specific field value
        query.setQuery(fieldNameService.getFieldName(FieldKey.COUNTRY) + ":Country1");
        {
            // only document with id "1" should match this
            SolrDocumentList documents = instance.getDocuments(query);
            assertEquals(1, documents.getNumFound());
            assertEquals("1", documents.get(0).getFieldValue(fieldNameService.getFieldName(FieldKey.ID)));
        }

        query.setFilterQueries(fieldNameService.getFieldName(FieldKey.COLLECTION) + ":Collection3");
        {
            // no matches
            SolrDocumentList documents = instance.getDocuments(query);
            assertEquals(0, documents.getNumFound());
        }
    }

    public static String getResourcePath(String resource) throws Exception {
        return new File(SearchResultsDaoImplTest.class.getResource(resource).toURI()).getAbsolutePath();
    }
}
