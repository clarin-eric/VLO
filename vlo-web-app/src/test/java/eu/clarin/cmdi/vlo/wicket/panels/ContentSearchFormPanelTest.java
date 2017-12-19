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
package eu.clarin.cmdi.vlo.wicket.panels;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.VloApplicationTestConfig;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.VloServicesSpringConfig;
import eu.clarin.cmdi.vlo.config.VloSolrSpringConfig;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;
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

/**
 * Mock injection based on blog post by Petri Kainulainen found at
 * {@link http://www.petrikainulainen.net/programming/tips-and-tricks/mocking-spring-beans-with-apache-wicket-and-mockito/}
 *
 * @author twagoo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // gives us a fresh context for each test
public class ContentSearchFormPanelTest {

    @Inject
    private VloWicketApplication application;
    @Inject
    private FieldNameService fieldNameService;
    
    private WicketTester tester;

    @Before
    public void setUp() {
        tester = new WicketTester(application);
    }

    @Test
    public void testSearchFormPanel() {
        final SolrDocument document = new SolrDocument();
        document.setField(fieldNameService.getFieldName(FieldKey.SELF_LINK), "hdl:1234/selflink");

        final ContentSearchFormPanel panel = new ContentSearchFormPanel("panel", Model.of(document), Model.of("http://cqlEndPoint/"));
        tester.startComponentInPage(panel);

        // form action should be aggregator search page
        tester.assertContains("action=\"http://fcs.org/aggregator\"");
        //tester.assertContains("action=\"http://weblicht.sfs.uni-tuebingen.de/Aggregator/\"");
        // json hidden input should have the CQL endpoint and document handle, and should be encoded into entities
        tester.assertContains(Pattern.quote("name=\"x-aggregation-context\" "
                + "value=\"{\n"
                + "  &quot;http://cqlEndPoint/&quot;: [\n"
                + "    &quot;hdl:1234/selflink&quot;\n"
                + "  ]\n"
                + "}\""));
    }

    /**
     * Custom configuration injected into web app for testing
     */
    @Configuration
    @PropertySource(value = "classpath:/config.default.properties", ignoreResourceNotFound = false)
    @Import({
        VloApplicationTestConfig.class,
        VloSolrSpringConfig.class,
        VloServicesSpringConfig.class})
    static class ContextConfiguration {

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

    }

    /**
     * Overrides returned application configuration
     */
    @Configuration
    static class VloApplicationFcsTestConfig extends VloApplicationTestConfig {

        @Override
        public VloConfig vloConfig() {
            // override default config
            final VloConfig config = super.vloConfig();
            // this globally configured URL should be the action target of the form
            config.setFederatedContentSearchUrl("http://fcs.org/aggregator");
            return config;
        }
    }

}
