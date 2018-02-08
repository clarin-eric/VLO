package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.VloWebAppParameters;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloSolrSpringConfig;
import eu.clarin.cmdi.vlo.service.solr.SimilarDocumentsService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import eu.clarin.cmdi.vlo.wicket.AbstractWicketTest;
import java.io.IOException;
import java.util.Collections;
import javax.inject.Inject;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class TestRecordPage extends AbstractWicketTest {

    @Inject
    private Mockery mockery;
    @Inject
    private SolrDocumentService documentService;
    @Inject
    private SimilarDocumentsService similarDocumentsService;
    @Inject
    private FieldNameService fieldNameService;

    private SolrDocument document;
    private PageParameters params;

    @Before
    @Override
    public void setUp() throws IOException {
        super.setUp();
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
        getTester().startPage(RecordPage.class, params);
        //assert rendered page class
        getTester().assertRenderedPage(RecordPage.class);
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
        getTester().startPage(RecordPage.class, params);
        // no landing page for document, assert landing page link is invisible
        getTester().assertInvisible("landingPageLink");
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
        getTester().startPage(RecordPage.class, params);
        // document has a landing page, assert link is invisible
        getTester().assertVisible("landingPageLink");
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
        public SimilarDocumentsService similarDocumentsService() {
            return mockery.mock(SimilarDocumentsService.class);
        }

    }

}
