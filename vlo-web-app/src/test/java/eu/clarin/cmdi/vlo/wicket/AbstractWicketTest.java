/*
 * Copyright (C) 2018 CLARIN
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

import eu.clarin.cmdi.vlo.VloApplicationTestConfig;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.VloServicesSpringConfig;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.inject.Inject;
import org.apache.wicket.util.tester.WicketTester;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * Mock injection based on blog post by Petri Kainulainen found at
 * {@link http://www.petrikainulainen.net/programming/tips-and-tricks/mocking-spring-beans-with-apache-wicket-and-mockito/}
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // gives us a fresh context for each test
public abstract class AbstractWicketTest {

    @Inject
    private VloWicketApplication application;

    private WicketTester tester;

    @Before
    public void setUp() throws IOException {
        final Properties testProperties = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/vlo-test.properties")) {
            testProperties.load(is);
        }
        tester = new WicketTester(application, testProperties.getProperty("wicket.test.contextpath"));
    }

    /**
     * @return the prepared wicket page/panel tester
     */
    public WicketTester getTester() {
        return tester;
    }

    /**
     * Custom configuration injected into web app for testing
     */
    @Configuration
    @PropertySource(value = "classpath:/config.default.properties", ignoreResourceNotFound = false)
    @Import({
        VloApplicationTestConfig.class,
        VloServicesSpringConfig.class})
    static protected class WicketBaseContextConfiguration {

        @Bean
        public Mockery mockery() {
            return new JUnit4Mockery();
        }

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }
}
