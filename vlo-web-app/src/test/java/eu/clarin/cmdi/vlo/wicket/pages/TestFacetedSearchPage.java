package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.VloApplicationTestConfig;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.VloServicesSpringConfig;
import eu.clarin.cmdi.vlo.config.VloSolrSpringConfig;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.util.tester.WicketTester;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * Mock injection based on blog post by Petri Kainulainen found at
 * {@link http://www.petrikainulainen.net/programming/tips-and-tricks/mocking-spring-beans-with-apache-wicket-and-mockito/}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class TestFacetedSearchPage {

    @Inject
    private VloWicketApplication application;
    @Inject
    private Mockery mockery;
    @Inject
    private FacetFieldsService facetFieldsService;
    @Inject
    private SolrDocumentService documentService;

    private WicketTester tester;

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
                atLeast(1).of(facetFieldsService).getFacetFieldCount(with(any(List.class)));
                will(returnValue(2L));
                atLeast(1).of(facetFieldsService).getFacetFields(with(any(QueryFacetsSelection.class)), with(any(List.class)), with(any(Integer.class)));
                will(returnValue(Arrays.asList(
                        new FacetField("languageCode"),
                        new FacetField("collection"),
                        new FacetField("resourceClass"),
                        new FacetField("country"),
                        new FacetField("modality"),
                        new FacetField("genre"),
                        new FacetField("subject"),
                        new FacetField("format"),
                        new FacetField("organisation"),
                        new FacetField("availability"),
                        new FacetField("nationalProject"),
                        new FacetField("keywords"),
                        new FacetField("dataProvider")
                )));

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
