/*
 * Copyright (C) 2020 CLARIN
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
package eu.clarin.cmdi.vlo.wicket;

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.VloSolrSpringConfig;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.apache.curator.shaded.com.google.common.base.Suppliers;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Page;
import org.apache.wicket.mock.MockHomePage;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;
import org.jmock.Mockery;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class RecordStructuredMeatadataHeaderBehaviorTest extends JsonLdHeaderBehaviorTest {

    private final static Logger logger = LoggerFactory.getLogger(RecordStructuredMeatadataHeaderBehaviorTest.class);

    private WicketTester tester;
    private RecordStructuredMeatadataHeaderBehavior instance;
    private SolrDocument solrDoc;

    private final static String HOME_URL = "https://test.vlo.clarin.eu";
    private final static String LANDING_PAGE_URL = "http://www.clarin.eu/landingpage";
    private final static String LANDING_PAGE = "{\"url\":\"" + LANDING_PAGE_URL + "\",\"type\":\"text/html\",\"status\":200,\"lastChecked\":0}";
    private final static String RESOURCE_URL = "http://www.clarin.eu/resource";
    private final static String RESOURCE_TYPE = "application/pdf";
    private final static String RESOURCE_REF = "{\"url\":\"" + RESOURCE_URL + "\",\"type\":\"" + RESOURCE_TYPE + "\",\"status\":null,\"lastChecked\":null}";
    private final static String PART_URL = "child_record_id";
    private final static String RECORD_ID = "recordId";
    private final static String CREATOR_NAME = "creator1";
    private final static String COUNTRY = "country1";
    private final static String LICENSE_URL = "http://www.clarin.eu/license";
    private final static String LICENSE_TEXT = "CLARIN test license";

    @Inject
    private FieldNameService fieldNameService;
    @Inject
    private VloConfig vloConfig;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        tester = getTester();
        solrDoc = new SolrDocument();

        instance = new RecordStructuredMeatadataHeaderBehavior(new Model<>(solrDoc));
    }

    @Test
    public void testCorrectOutput() throws Exception {
        final Page page = preparePage();

        setDocField(FieldKey.LANDINGPAGE, LANDING_PAGE);
        setDocField(FieldKey.CREATOR, ImmutableList.of(CREATOR_NAME, CREATOR_NAME));
        setDocField(FieldKey.COUNTRY, ImmutableList.of(COUNTRY, COUNTRY));
        setDocField(FieldKey.HAS_PART, ImmutableList.of(PART_URL, PART_URL + "-other"));
        setDocField(FieldKey.RESOURCE, ImmutableList.of(RESOURCE_REF, RESOURCE_REF));
        setDocField(FieldKey.LICENSE, LICENSE_URL);

        final JSONObject json = startPage(page);

        //DataSet object type and schema
        assertEquals("https://schema.org", json.get("@context"));
        assertEquals("DataSet", json.get("@type"));

        // DataSet URL
        assertTrue(json.get("url").toString().contains(RECORD_ID));

        // Landing page URL
        assertEquals(LANDING_PAGE_URL, json.get("mainEntityOfPage"));
        // Same as
        assertEquals(LANDING_PAGE_URL, json.get("sameAs"));
        // License
        assertEquals(LICENSE_URL, json.get("license"));

        // DataCatalog property
        {
            final Object dataCatalog = json.get("includedInDataCatalog");
            assertTrue(dataCatalog instanceof JSONObject);
            assertEquals("DataCatalog", ((JSONObject) dataCatalog).get("@type"));
            assertEquals(HOME_URL, ((JSONObject) dataCatalog).get("url"));
        }

        // Identifiers array
        {
            final Object identifier = json.get("identifier");
            assertTrue(identifier instanceof JSONArray);
            assertEquals(1, ((JSONArray) identifier).size());
            assertEquals(LANDING_PAGE_URL, ((JSONArray) identifier).get(0));
        }

        // Creators array
        {
            final Object creators = json.get("creator");
            assertTrue(creators instanceof JSONArray);
            assertEquals(2, ((JSONArray) creators).size());
            final Object creator1 = ((JSONArray) creators).get(0);
            assertTrue(creator1 instanceof JSONObject);
            assertEquals("Person", ((JSONObject) creator1).get("@type"));
            assertEquals(CREATOR_NAME, ((JSONObject) creator1).get("name"));
        }

        // Country array
        {
            final Object spatial = json.get("spatial");
            assertTrue(spatial instanceof JSONArray);
            assertEquals(2, ((JSONArray) spatial).size());
            final Object spatial1 = ((JSONArray) spatial).get(0);
            assertTrue(spatial1 instanceof JSONObject);
            assertEquals("Place", ((JSONObject) spatial1).get("@type"));
            assertEquals(COUNTRY, ((JSONObject) spatial1).get("name"));
        }

        // Has part array
        {
            final Object parts = json.get("hasPart");
            assertTrue(parts instanceof JSONArray);
            assertEquals(2, ((JSONArray) parts).size());
            final Object part1 = ((JSONArray) parts).get(0);
            assertTrue(part1 instanceof JSONObject);
            assertEquals("CreativeWork", ((JSONObject) part1).get("@type"));
            assertTrue(((JSONObject) part1).get("url") instanceof String);
            assertTrue(((JSONObject) part1).get("url").toString().endsWith(PART_URL));
        }

        // Resources (distribution) array
        {
            final Object resources = json.get("distribution");
            assertTrue(resources instanceof JSONArray);
            assertEquals(2, ((JSONArray) resources).size());
            final Object resource1 = ((JSONArray) resources).get(0);
            assertTrue(resource1 instanceof JSONObject);
            assertEquals("DataDownload", ((JSONObject) resource1).get("@type"));
            assertEquals(RESOURCE_URL, ((JSONObject) resource1).get("contentUrl"));
            assertEquals(RESOURCE_TYPE, ((JSONObject) resource1).get("encodingFormat"));
        }
    }

    @Test
    public void testNoLicenseOutput() throws Exception {
        {
            final Page page = preparePage();
            //license not set at all
            final JSONObject json = startPage(page);
            assertNull(json.get("license"));
        }

        {
            final Page page = preparePage();
            //license set to something that is not a URI
            setDocField(FieldKey.LICENSE, LICENSE_TEXT);

            final JSONObject json = startPage(page);
            assertNull(json.get("license"));
        }
    }

    @Test
    public void testNoLandingPage() throws Exception {
        Page page = preparePage();

        final JSONObject json = startPage(page);
        assertNull(json.get("mainEntityOfPage"));
        assertNull(json.get("sameAs"));

        // Identifiers array
        {
            final Object identifier = json.get("identifier");
            assertTrue(identifier instanceof JSONArray);
            assertEquals(1, ((JSONArray) identifier).size());
            assertEquals(RECORD_ID, ((JSONArray) identifier).get(0));
        }
    }

    @Test
    public void testArrayLimit() throws Exception {
        Page page = preparePage();

        final int limit = RecordStructuredMeatadataHeaderBehavior.ARRAY_SIZE_LIMIT;
        final int belowLimit = limit - 1;

        //more creators than limit allows
        setDocField(FieldKey.CREATOR, Stream.generate(() -> CREATOR_NAME).limit(limit + 10).collect(Collectors.toList()));
        //more 'spatial' entries than limit allows
        setDocField(FieldKey.COUNTRY, Stream.generate(() -> COUNTRY).limit(limit + 10).collect(Collectors.toList()));
        //hasPart entries below limit
        setDocField(FieldKey.HAS_PART, Stream.generate(() -> PART_URL).limit(belowLimit).collect(Collectors.toList()));
        //resource ref entries below limit
        setDocField(FieldKey.RESOURCE, Stream.generate(() -> RESOURCE_REF).limit(belowLimit).collect(Collectors.toList()));

        final JSONObject json = startPage(page);

        assertEquals("limit exceeded - should be capped", limit, ((JSONArray) json.get("creator")).size());
        assertEquals("limit exceeded - should be capped", limit, ((JSONArray) json.get("spatial")).size());
        assertEquals("below limit - should NOT be capped", belowLimit, ((JSONArray) json.get("hasPart")).size());
        assertEquals("below limit - should NOT be capped", belowLimit, ((JSONArray) json.get("distribution")).size());
    }

    private Page preparePage() {
        //create page with behaviour
        final Page page = new MockHomePage();
        page.add(instance);

        //set basic properties
        vloConfig.setHomeUrl(HOME_URL);
        setDocField(FieldKey.ID, RECORD_ID);

        return page;
    }

    private JSONObject startPage(final Page page) throws ParseException {
        tester.startPage(page);

        final String document = tester.getLastResponse().getDocument();

        final JSONObject json = getJsonFromDoc(document);
        logger.debug("JSON from response: {}", json);
        return json;
    }

    private void setDocField(FieldKey key, Object value) {
        solrDoc.setField(fieldNameService.getFieldName(key), value);
    }

    /**
     * Provides some mock Solr services
     */
    @Configuration
    @Import({WicketBaseContextConfiguration.class})
    static class ContextConfiguration extends VloSolrSpringConfig {

        @Inject
        private Mockery mockery;

        @Override
        public SolrDocumentService documentService() {
            return mockery.mock(SolrDocumentService.class);
        }

        @Override
        public FacetFieldsService facetFieldsService() {
            return mockery.mock(FacetFieldsService.class, "facetFieldsService");
        }
    }
}
