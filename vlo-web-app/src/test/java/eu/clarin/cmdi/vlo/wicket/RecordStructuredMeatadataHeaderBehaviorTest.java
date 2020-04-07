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

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloSolrSpringConfig;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import javax.inject.Inject;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Page;
import org.apache.wicket.mock.MockHomePage;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;
import org.jmock.Mockery;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

    private final static String LANDING_PAGE_URL = "http://www.clarin.eu/landingpage";
    private final static String LANDING_PAGE = "{\"url\":\"" + LANDING_PAGE_URL + "\",\"type\":\"text/html\",\"status\":200,\"lastChecked\":0}";
    private final static String RECORD_ID = "recordId";

    @Inject
    private FieldNameService fieldNameService;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        tester = getTester();
        solrDoc = new SolrDocument();

        instance = new RecordStructuredMeatadataHeaderBehavior(new Model<>(solrDoc));
    }

    @Test
    public void testOutput() throws Exception {
        final Page page = new MockHomePage();
        page.add(instance);

        setDocField(FieldKey.ID, RECORD_ID);
        setDocField(FieldKey.LANDINGPAGE, LANDING_PAGE);

        tester.startPage(page);
        final String document = tester.getLastResponse().getDocument();
        final JSONObject json = getJsonFromDoc(document);
        
        logger.debug("JSON from response: {}", json);

        assertEquals("https://schema.org", json.get("@context"));
        assertEquals("DataSet", json.get("@type"));

        assertTrue(json.get("url").toString().contains(RECORD_ID));
        assertEquals(LANDING_PAGE_URL, json.get("mainEntityOfPage"));

        final Object identifier = json.get("identifier");
        assertTrue(identifier instanceof JSONArray);
        assertEquals(1, ((JSONArray) identifier).size());
        assertEquals(LANDING_PAGE_URL, ((JSONArray) identifier).get(0));

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
