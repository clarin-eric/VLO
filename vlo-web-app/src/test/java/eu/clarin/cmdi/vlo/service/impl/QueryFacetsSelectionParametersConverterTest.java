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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import eu.clarin.cmdi.vlo.VloApplicationTestConfig;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloServicesSpringConfig;
import eu.clarin.cmdi.vlo.config.VloSolrSpringConfig;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.SimilarDocumentsService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
//import eu.clarin.cmdi.vlo.wicket.pages.TestRecordPage.VloSolrSpringTestConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
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
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD) // gives us a fresh context for each test
public class QueryFacetsSelectionParametersConverterTest {

    private QueryFacetsSelectionParametersConverter instance;
    
    @Inject
    FieldNameService fieldNameService;

    @Before
    public void setUp() {
        instance = new QueryFacetsSelectionParametersConverter(ImmutableSet.of("facet1", "facet2", "facet3", "facet4"));
    }

    /**
     * Test of fromParameters method, of class
     * QueryFacetsSelectionParametersConverter.
     */
    @Test
    public void testFromParameters() {
        final PageParameters params = new PageParameters();
        params.add("q", "query");
        params.add("q", "ignored"); // only one query param is selected
        params.add("fq", "facet1:valueA");
        params.add("fq", "facet1:valueB");
        params.add("fq", "facet2:value:C"); // has a colon in value
        params.add("fq", "illegal-no-colon"); //should get ignored
        params.add("fq", "facet5:valueD"); //not in list, should get ignored
        params.add("fq", ""); // not a valid facet selection
        params.add("fq", "invalid"); // not a valid facet selection
        params.add("fqType", "facet1:and");
        params.add("fqType", "facet3:not_empty");
        params.add("fqType", "facet4:illegaltype"); //should get ignored
        params.add("fqType", "illegal-no-colon"); //should get ignored

        final QueryFacetsSelection result = instance.fromParameters(params);
        assertEquals("query", result.getQuery());
        assertEquals(3, result.getFacets().size());
        assertThat(result.getFacets(), hasItem("facet1"));
        assertThat(result.getFacets(), hasItem("facet2"));
        assertThat(result.getSelectionValues("facet1").getValues(), hasItem("valueA"));
        assertThat(result.getSelectionValues("facet1").getValues(), hasItem("valueB"));
        assertThat(result.getSelectionValues("facet2").getValues(), hasItem("value:C"));
        // AND explicitly set
        assertEquals(FacetSelectionType.AND, result.getSelectionValues("facet1").getSelectionType());
        // OR is default
        assertEquals(FacetSelectionType.OR, result.getSelectionValues("facet2").getSelectionType());
        // NOT_EMPTY explicitly set
        assertEquals(FacetSelectionType.NOT_EMPTY, result.getSelectionValues("facet3").getSelectionType());
    }

    /**
     * Test of fromParameters method, of class
     * QueryFacetsSelectionParametersConverter.
     */
    @Test
    public void testFromParametersSerializable() {
        final PageParameters params = new PageParameters();
        params.add("q", "query");
        params.add("q", "ignored"); // only one query param is selected
        params.add("fq", "facet1:valueA");
        params.add("fq", "facet1:valueB");
        params.add("fq", "facet2:valueC");
        params.add("fq", ""); // not a valid facet selection
        params.add("fq", "invalid"); // not a valid facet selection

        SerializationUtils.roundtrip(instance.fromParameters(params));
    }

    /**
     * Test of toParameters method, of class
     * QueryFacetsSelectionParametersConverter.
     */
    @Test
    public void testToParameters() {
        final String query = "query";
        final Map<String, FacetSelection> map = Maps.newHashMapWithExpectedSize(3);
        map.put("facet1", new FacetSelection(FacetSelectionType.OR, Arrays.asList("valueA", "valueB")));
        map.put("facet2", new FacetSelection(FacetSelectionType.AND, Collections.singleton("value:C")));
        map.put("facet3", new FacetSelection(FacetSelectionType.NOT_EMPTY));

        QueryFacetsSelection selection = new QueryFacetsSelection(query, map);
        PageParameters result = instance.toParameters(selection);

        assertThat(result.get("q"), equalTo(StringValue.valueOf("query")));

        final List<StringValue> fq = result.getValues("fq");
        assertNotNull(fq);
        assertThat(fq, hasItem(StringValue.valueOf("facet1:valueA")));
        assertThat(fq, hasItem(StringValue.valueOf("facet1:valueB")));
        assertThat(fq, hasItem(StringValue.valueOf("facet2:value:C")));

        final List<StringValue> fqType = result.getValues("fqType");
        assertNotNull(fqType);
        assertThat(fqType, hasItem(StringValue.valueOf("facet1:or")));
        //facet 2 is AND, which is default and should not be encoded
        assertThat(fqType, hasItem(StringValue.valueOf("facet3:not_empty")));
    }
    //TODO: Add test for display of resources
    /**
     * Custom configuration injected into web app for testing
     */
    @Configuration
    @PropertySource(value = "classpath:/config.default.properties", ignoreResourceNotFound = false)
    @Import({
        VloSolrSpringTestConfig.class,
        VloApplicationTestConfig.class,
        VloServicesSpringConfig.class})
    static class ContextConfiguration {

        @Bean
        public Mockery mockery() {
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
    static class VloSolrSpringTestConfig extends VloSolrSpringConfig {

        @Inject
        private Mockery mockery;

        @Override
        public SolrDocumentService documentService() {
            return mockery.mock(SolrDocumentService.class);
        }

        @Override
        public SimilarDocumentsService similarDocumentsService() {
            return mockery.mock(SimilarDocumentsService.class);
        }

    }

}
