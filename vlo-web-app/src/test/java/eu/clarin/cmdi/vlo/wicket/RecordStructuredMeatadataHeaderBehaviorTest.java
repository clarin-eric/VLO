/*
 * Copyright (C) 2020 CLARIN
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
package eu.clarin.cmdi.vlo.wicket;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloSolrSpringConfig;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Page;
import org.apache.wicket.mock.MockHomePage;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class RecordStructuredMeatadataHeaderBehaviorTest extends AbstractWicketTest {

    private WicketTester tester;
    private RecordStructuredMeatadataHeaderBehavior instance;
    private SolrDocument solrDoc;
    
    @Inject
    private FieldNameService fieldNameService;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        tester = getTester();
        solrDoc = new SolrDocument();
        solrDoc.setField(fieldNameService.getFieldName(FieldKey.ID), "recordId");
        
        instance = new RecordStructuredMeatadataHeaderBehavior(new Model<>(solrDoc));
    }
    
    
    @Test
    public void testOutput() {
        final Page page = new MockHomePage();
        page.add(instance);
        
        tester.startPage(page);
        tester.assertContains(Pattern.quote("<script type=\"application/ld+json\">"));
        tester.assertContains(Pattern.quote("\"@context\": \"https://schema.org\""));
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
