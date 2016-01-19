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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.VloApplicationSpringConfig;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.VloServicesSpringConfig;
import eu.clarin.cmdi.vlo.config.VloSolrSpringConfig;
import java.io.IOException;
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
public class ContentSearchFormPanelTest {

    @Inject
    private VloWicketApplication application;
    private WicketTester tester;

    @Before
    public void setUp() {
        tester = new WicketTester(application);
    }

    @Test
    public void testSearchFormPanel() {
        final SolrDocument document = new SolrDocument();
        document.setField(FacetConstants.FIELD_SELF_LINK, "hdl:1234/selflink");

        final ContentSearchFormPanel panel = new ContentSearchFormPanel("panel", Model.of(document), Model.of("http://cqlEndPoint/"));
        tester.startComponentInPage(panel);

        // form action should be aggregator search page
        tester.assertContains("action=\"http://fcs.org/aggregator\"");

        // json hidden input should have the CQL endpoint and document handle, and should be encoded into entities
        tester.assertContains(Pattern.quote("name=\"x-aggregation-context\" value=\"{&quot;http://cqlEndPoint/&quot;: [&quot;hdl:1234/selflink&quot;]}\""));
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
    static class VloApplicationTestConfig extends VloApplicationSpringConfig {

        @Override
        public VloConfig vloConfig() {
            try {
                // override default config
                final VloConfig config = new DefaultVloConfigFactory().newConfig();
                // this globally configured URL should be the action target of the form
                config.setFederatedContentSearchUrl("http://fcs.org/aggregator");
                return config;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
