package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.VloApplicationTestConfig;
import eu.clarin.cmdi.vlo.VloWebAppParameters;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloServicesSpringConfig;
import eu.clarin.cmdi.vlo.config.VloSolrSpringConfig;
import eu.clarin.cmdi.vlo.service.solr.SimilarDocumentsService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import java.util.Collections;
import javax.inject.Inject;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.request.mapper.parameter.PageParameters;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * Mock injection based on blog post by Petri Kainulainen found at
 * {@link http://www.petrikainulainen.net/programming/tips-and-tricks/mocking-spring-beans-with-apache-wicket-and-mockito/}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD) // gives us a fresh context for each test
public class TestRecordPage {

    @Inject
    private VloWicketApplication application;
    @Inject
    private Mockery mockery;
    @Inject
    private SolrDocumentService documentService;
    @Inject
    private SimilarDocumentsService similarDocumentsService;
    @Inject
    private FieldNameService fieldNameService;

    private WicketTester tester;
    private SolrDocument document;
    private PageParameters params;

    @Before
    public void setUp() {
        tester = new WicketTester(application);

        document = new SolrDocument();
        document.setField(fieldNameService.getFieldName(FieldKey.ID), "documentId");

        params = new PageParameters();
        params.set(VloWebAppParameters.DOCUMENT_ID, "documentId");
    }

    @Test
    public void testRendersSuccessfully() {
        mockery.checking(new Expectations() {
            {
                oneOf(documentService).getDocument("documentId");
                will(returnValue(document));
                oneOf(similarDocumentsService).getDocuments("documentId");
                will(returnValue(Collections.<SolrDocument>emptyList()));
            }
        });
        tester.startPage(RecordPage.class, params);
        //assert rendered page class
        tester.assertRenderedPage(RecordPage.class);
    }

    @Test
    public void testLandingPageLinkInvisible() {
        mockery.checking(new Expectations() {
            {
                oneOf(documentService).getDocument("documentId");
                will(returnValue(document));
                oneOf(similarDocumentsService).getDocuments("documentId");
                will(returnValue(Collections.<SolrDocument>emptyList()));
            }
        });
        tester.startPage(RecordPage.class, params);
        // no landing page for document, assert landing page link is invisible
        tester.assertInvisible("landingPageLink");
    }

    @Test
    public void testLandingPageLinkVisible() {
        document.addField(fieldNameService.getFieldName(FieldKey.LANDINGPAGE), "http://www.landingpage.com");

        mockery.checking(new Expectations() {
            {
                oneOf(documentService).getDocument("documentId");
                will(returnValue(document));
                oneOf(similarDocumentsService).getDocuments("documentId");
                will(returnValue(Collections.<SolrDocument>emptyList()));
            }
        });
        tester.startPage(RecordPage.class, params);
        // document has a landing page, assert link is invisible
        tester.assertVisible("landingPageLink");
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
