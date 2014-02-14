package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.wicket.pages.FacetedSearchPage;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.VloSpringConfig;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.FacetFieldsService;
import java.util.Arrays;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
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
        public FacetFieldsService facetFieldsService() {
            return mockery().mock(FacetFieldsService.class);
        }

    }

    private WicketTester tester;
    @Autowired
    private VloWicketApplication application;
    @Autowired
    private Mockery mockery;
    @Autowired
    private FacetFieldsService facetFieldsService;

    @Before
    public void setUp() {
        tester = new WicketTester(application);
    }

    @Test
    public void homepageRendersSuccessfully() {
        // mock behaviour of facet fields service
        mockery.checking(new Expectations() {
            {
                atLeast(1).of(facetFieldsService).getFacetFieldCount();
                will(returnValue(2L));
                oneOf(facetFieldsService).getFacetFields(with(any(QueryFacetsSelection.class)));
                will(returnValue(Arrays.asList(new FacetField("language"), new FacetField("resource class"))));
            }
        });

        //start and render the test page
        tester.startPage(FacetedSearchPage.class);

        //assert rendered page class
        tester.assertRenderedPage(FacetedSearchPage.class);
    }
}
