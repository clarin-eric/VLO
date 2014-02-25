package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.wicket.pages.FacetedSearchPage;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.VloSpringConfig;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.SolrDocumentService;
import java.util.Arrays;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.util.tester.WicketTester;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.hamcrest.core.AnyOf.*;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Abstract base class for tests that require dependency injection of (mock)
 * objects and services. Based on blog post by Petri Kainulainen found at
 * {@link http://www.petrikainulainen.net/programming/tips-and-tricks/mocking-spring-beans-with-apache-wicket-and-mockito/}
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class TestFacetedSearchPage {

    @Configuration
    @ComponentScan
    static class ContextConfiguration extends VloSpringConfig {

        @Bean
        public Mockery mockery() {
            return new JUnit4Mockery();
        }

        @Override
        @Bean(name = VloSpringConfig.FACETS_PANEL_SERVICE)
        public FacetFieldsService facetFieldsService() {
            return mockery().mock(FacetFieldsService.class, "facetFieldsService");
        }

        @Override
        @Bean(name = COLLECTION_FACET_SERVICE)
        public FacetFieldsService collectionFacetFieldsService() {
            return mockery().mock(FacetFieldsService.class, "collectionFacetFieldsService");
        }

        @Override
        public SolrDocumentService documentService() {
            return mockery().mock(SolrDocumentService.class);
        }
    }

    private WicketTester tester;
    @Autowired(required = true)
    private VloWicketApplication application;
    @Autowired(required = true)
    private Mockery mockery;

    @Autowired(required = true)
    @Qualifier(VloSpringConfig.FACETS_PANEL_SERVICE)
    private FacetFieldsService facetFieldsService;

    @Autowired(required = true)
    @Qualifier(VloSpringConfig.COLLECTION_FACET_SERVICE)
    private FacetFieldsService collectionsFacetFieldsService; //TODO: Make sure this gets injected separately (like qualifier should ensure)

    @Autowired(required = true)
    private SolrDocumentService documentService;

    @Before
    public void setUp() {
        tester = new WicketTester(application);
    }

    @Test
    public void homepageRendersSuccessfully() {
        // mock behaviour of facet fields service
        mockery.checking(new Expectations() {
            {
                // mock facets
                atLeast(1).of(facetFieldsService).getFacetFieldCount();
                will(returnValue(2L));
                atLeast(1).of(facetFieldsService).getFacetFields(with(any(QueryFacetsSelection.class)));
                will(returnValue(Arrays.asList(new FacetField("language"), new FacetField("resource class"))));
//
//                // mock collection facet
//                atLeast(1).of(collectionsFacetFieldsService).getFacetFieldCount();
//                will(returnValue(1L));
//                oneOf(collectionsFacetFieldsService).getFacetFields(with(any(QueryFacetsSelection.class)));
//                will(returnValue(Arrays.asList(new FacetField("collection"))));

                // mock search results
                atLeast(1).of(documentService).getDocumentCount(with(any(QueryFacetsSelection.class)));
                will(returnValue(1000L));
                oneOf(documentService).getDocuments(with(any(QueryFacetsSelection.class)), with(equal(0)), with(equal(10)));
                will(returnValue(Arrays.asList(new SolrDocument(), new SolrDocument())));
            }
        });

        //start and render the test page
        tester.startPage(FacetedSearchPage.class);

        //assert rendered page class
        tester.assertRenderedPage(FacetedSearchPage.class);
    }
}
