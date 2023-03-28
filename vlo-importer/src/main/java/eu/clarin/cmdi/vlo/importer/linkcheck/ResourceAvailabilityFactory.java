/*
 * Copyright (C) 2022 CLARIN
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
package eu.clarin.cmdi.vlo.importer.linkcheck;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class ResourceAvailabilityFactory {

    /**
     * Logging
     */
    private final static Logger logger = LoggerFactory.getLogger(ResourceAvailabilityFactory.class);

    public static ResourceAvailabilityStatusChecker createDefaultResourceAvailabilityStatusChecker(final VloConfig vloConfig) {
        try {
            final LinkcheckerAvailabilityStatusChecker checker = newJpaAvailabilityStatusChecker(vloConfig);
            if (testChecker(checker)) {
                return checker;
            }
        } catch (Exception ex) {
            logger.error("Error while initialising resource availability checker", ex);
        }
        logger.warn("Resource availability checker initialisation and/or test FAILED. Installing a NOOP availability checker. Availability status will NOT be checked!");
        return new NoopResourceAvailabilityStatusChecker();
    }

    private static LinkcheckerAvailabilityStatusChecker newJpaAvailabilityStatusChecker(final VloConfig vloConfig) {

        final LinkcheckerAvailabilityStatusCheckerFactory factory = new LinkcheckerAvailabilityStatusCheckerFactory() {
            @Override
            public void configureEnvironment(MutablePropertySources propertySources) {
                super.configureEnvironment(propertySources);

                // Configure environment with YAML properties loaded from a resource file
                try {
                    final List<PropertySource<?>> sources = loadYamlProperties("/spring/application.yml");
                    if (sources != null) {
                        sources.forEach(propertySources::addLast);
                    }
                } catch (IOException ex) {
                    logger.error("Error while reading properties from yaml", ex);
                }

                // Set database properties from VLO config
                logger.info("Setting database connection properties from VLO configuration");
                final ImmutableMap<String, Object> dbProps = ImmutableMap.<String, Object>builder()
                        .put("spring.datasource.url", vloConfig.getLinkCheckerDbConnectionString())
                        .put("spring.datasource.username", vloConfig.getLinkCheckerDbUser())
                        .put("spring.datasource.password", vloConfig.getLinkCheckerDbPassword())
                        .build();
                logger.debug("Database properties: {}", Iterables.toString(dbProps.entrySet()));
                propertySources.addLast(new MapPropertySource("VloConfig", dbProps));
            }

        };
        logger.info("Initializing LinkcheckerAvailabilityStatusCheckerFactory");
        factory.init();

        logger.info("Getting LinkcheckerAvailabilityStatusChecker from factory");
        return factory.getInstance();
    }

    private static List<PropertySource<?>> loadYamlProperties(String resourcePath) throws IOException {
        logger.info("Loading YAML properties from classpath resource: {}", resourcePath);
        final YamlPropertySourceLoader yamlLoader = new YamlPropertySourceLoader();
        final Resource resource = new ClassPathResource(resourcePath, ResourceAvailabilityFactory.class);
        return yamlLoader.load("springConfig", resource);
    }

    private static boolean testChecker(ResourceAvailabilityStatusChecker checker) {
        logger.debug("Carrying out functional check of resource availability status checker...");
        final String testUrl = "https://www.clarin.eu";
        try {
            checker.getLinkStatusForRefs(Stream.of(testUrl));
            return true;
        } catch (IOException ex) {
            logger.error("Failed to carry out test check for {}", testUrl, ex);
            return false;
        }
    }
}
