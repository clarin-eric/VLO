package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.VloConfigFactory;
import eu.clarin.cmdi.vlo.config.VloSpringConfig;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD) // gives us a fresh context for each test
public class TestRecordPage {

    @Configuration
    static class ContextConfiguration extends VloSpringConfig {

        @Override
        public VloConfigFactory vloConfigFactory() {
            //TODO: Separate test config? -> override vloConfig() instead
            return new DefaultVloConfigFactory();
        }

    }

    private WicketTester tester;
    @Autowired(required = true)
    private VloWicketApplication application;

    private QueryFacetsSelection selection;
    private SolrDocument document;

    @Before
    public void setUp() {
        tester = new WicketTester(application);
        document = new SolrDocument();
        selection = new QueryFacetsSelection();
    }

    @Test
    public void testRendersSuccessfully() {
        tester.startPage(new RecordPage(new Model(document), new Model(selection)));
        //assert rendered page class
        tester.assertRenderedPage(RecordPage.class);
    }

    @Test
    public void testLandingPageLinkInvisible() {
        tester.startPage(new RecordPage(new Model(document), new Model(selection)));
        // no landing page for document, assert landing page link is invisible
        tester.assertInvisible("landingPageLink");
    }

    @Test
    public void testLandingPageLinkVisible() {
        document.addField(FacetConstants.FIELD_LANDINGPAGE, "http://www.landingpage.com");
        tester.startPage(new RecordPage(new Model(document), new Model(selection)));
        // document has a landing page, assert link is invisible
        tester.assertVisible("landingPageLink");
    }
}
