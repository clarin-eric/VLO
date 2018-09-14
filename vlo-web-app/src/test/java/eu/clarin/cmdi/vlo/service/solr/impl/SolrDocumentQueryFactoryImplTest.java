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

import com.google.common.collect.ImmutableList;

import eu.clarin.cmdi.vlo.VloApplicationTestConfig;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloServicesSpringConfig;
import eu.clarin.cmdi.vlo.config.VloSolrSpringConfig;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;


import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.apache.solr.client.solrj.SolrQuery;
import org.hamcrest.Matchers;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // gives us a fresh context for each test
public class SolrDocumentQueryFactoryImplTest {

    private final Collection<String> docFields = ImmutableList.of("field1", "field2", "field3");
    private SolrDocumentQueryFactoryImpl instance;
    
    @Inject
    FieldNameService fieldNameService;

    @Before
    public void setUp() {
        instance = new SolrDocumentQueryFactoryImpl(docFields, fieldNameService);
    }

    /**
     * Test of createDocumentQuery method, of class
     * SolrDocumentQueryFactoryImpl.
     */
    @Test
    public void testCreateDocumentQueryForSelection() {
        final Map<String, FacetSelection> selectionMap = Collections.<String, FacetSelection>singletonMap("field1", new FacetSelection(FacetSelectionType.AND, Collections.singleton("value 1")));
        final QueryFacetsSelection selection = new QueryFacetsSelection("query", selectionMap);
        final int first = 100;
        final int count = 15;

        final SolrQuery query = instance.createDocumentQuery(selection, first, count);

        assertEquals(Integer.valueOf(100), query.getStart());
        assertEquals(Integer.valueOf(15), query.getRows());

        final String[] filterQueries = query.getFilterQueries();
        assertEquals(1, filterQueries.length);
        assertEquals("field1:\"value\\ 1\"", filterQueries[0]);

        final String fields = query.getFields();
        assertTrue(fields.contains("field1"));
        assertTrue(fields.contains("field2"));
        assertTrue(fields.contains("field3"));
        assertEquals("query", query.getQuery());
    }

    /**
     * Test of createDocumentQuery method, of class
     * SolrDocumentQueryFactoryImpl.
     */
    @Test
    public void testCreateDocumentQueryForDocId() {
        final SolrQuery query = instance.createDocumentQuery("document\"Id");

        final String[] filterQueries = query.getFilterQueries();
        assertEquals(2, filterQueries.length);
        // expecting query that looks in both id and selflink fields with properly escaped values
        assertThat(filterQueries, Matchers.hasItemInArray("id:\"document\\\"Id\" OR _selfLink:\"document\\\"Id\""));

        final String fields = query.getFields();
        assertTrue(fields.contains("field1"));
        assertTrue(fields.contains("field2"));
        assertTrue(fields.contains("field3"));
    }
    /**
     * Custom configuration injected into web app for testing
     */
    @Configuration
    @PropertySource(value = "classpath:/config.default.properties", ignoreResourceNotFound = false)
    @Import({
        VloSolrTestConfig.class,
        VloApplicationTestConfig.class,
        VloServicesSpringConfig.class})
    static class ContextConfiguration {

        @Bean
        public Mockery mockery() {
            // shared mockery context
            return new JUnit4Mockery();
        }

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }

    /**
     * Provides some mock Solr services
     */
    @Configuration
    static class VloSolrTestConfig extends VloSolrSpringConfig {

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
