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
package eu.clarin.cmdi.vlo.wicket.model;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.VloApplicationTestConfig;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloServicesSpringConfig;
import eu.clarin.cmdi.vlo.config.VloSolrSpringConfig;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;


import org.apache.solr.common.SolrDocument;
import org.jmock.Expectations;

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

import javax.inject.Inject;

/**
 *
 * @author twagoo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // gives us a fresh context for each test
public class SolrDocumentModelTest {

    private final Mockery context = new JUnit4Mockery();

    private SolrDocumentService service;
    private SolrDocument expected;
    @Inject
    private FieldNameService fieldNameService;

    @Before
    public void setUp() {
        expected = new SolrDocument();
        expected.setField(fieldNameService.getFieldName(FieldKey.ID), "id");
        service = context.mock(SolrDocumentService.class);
    }

    /**
     * Test of load method, of class SolrDocumentModel.
     */
    @Test
    public void testGetObject() {
        // construct with a document id (lazy loading)
        final SolrDocumentModel instance = new SolrDocumentModel("id") {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            protected SolrDocumentService getDocumentService() {
                return service;
            }

        };

        context.checking(new Expectations() {
            {
                exactly(2).of(service).getDocument("id");
                will(returnValue(expected));
            }
        });

        // get object, should call service to load object
        assertEquals(expected, instance.getObject());
        // call once more, should not call service (already loaded)
        assertEquals(expected, instance.getObject());
        // detach, will invalidate loaded object
        instance.detach();
        // getting object will require a service call to reload
        assertEquals(expected, instance.getObject());
    }

    /**
     * Test of load method, of class SolrDocumentModel.
     */
    @Test
    public void testGetObjectInitialised() {
        // construct with an existing solr document
        final SolrDocumentModel instance = new SolrDocumentModel(expected, fieldNameService) {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            protected SolrDocumentService getDocumentService() {
                return service;
            }

        };

        context.checking(new Expectations() {
            {
                exactly(1).of(service).getDocument("id");
                will(returnValue(expected));
            }
        });

        // call once more, should not call service (already loaded) at construction
        assertEquals(expected, instance.getObject());
        // detach, will invalidate loaded object
        instance.detach();
        // getting object will require a service call to reload
        assertEquals(expected, instance.getObject());
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
