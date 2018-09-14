package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.config.VloSolrSpringConfig;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionList;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionPair;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import eu.clarin.cmdi.vlo.wicket.AbstractWicketTest;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.SolrDocument;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class TestFacetedSearchPage extends AbstractWicketTest {

    @Inject
    private Mockery mockery;
    @Inject
    private FacetFieldsService facetFieldsService;
    @Inject
    private SolrDocumentService documentService;

    @Test
    public void homepageRendersSuccessfully() {
        SolrDocumentExpansionList resultList = mockery.mock(SolrDocumentExpansionList.class);
        SolrDocumentExpansionPair resultItem = mockery.mock(SolrDocumentExpansionPair.class);
        
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
                        new FacetField("licenseType"),
                        new FacetField("nationalProject"),
                        new FacetField("keywords"),
                        new FacetField("dataProvider")
                )));

                // mock search results
                atLeast(1).of(documentService).getDocumentCount(with(any(QueryFacetsSelection.class)));
                will(returnValue(1000L));
                oneOf(documentService).getDocumentsWithExpansion(with(any(QueryFacetsSelection.class)), with(equal(0)), with(equal(10)), with(equal("_signature")));
                will(returnValue(resultList));
                atLeast(1).of(resultList).iterator();
                will(returnIterator(resultItem));
                allowing(any(SolrDocumentExpansionPair.class)).method(anything());
            }
        });

        //start and render the test page
        getTester().startPage(FacetedSearchPage.class);

        //assert rendered page class
        getTester().assertRenderedPage(FacetedSearchPage.class);
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
