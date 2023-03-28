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

import eu.clarin.linkchecker.persistence.service.StatusService;
import java.io.Writer;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * This factory bridges the Spring (boot) based link checker persistence
 * interface with the importer.
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class LinkcheckerAvailabilityStatusCheckerFactory {

    private final static Logger logger = LoggerFactory.getLogger(LinkcheckerAvailabilityStatusCheckerFactory.class);
    private AnnotationConfigApplicationContext ctx;

    @PostConstruct
    public final void init() {
        // Programmatically initiate a Spring context
        logger.info("Initializing Spring context for LinkcheckerAvailabilityStatusChecker");

        final ConfigurableEnvironment environment = new StandardEnvironment();
        configureEnvironment(environment.getPropertySources());

        ctx = new AnnotationConfigApplicationContext();
        ctx.setEnvironment(environment);
        ctx.register(ApplicationConfig.class);

        ctx.refresh();
    }

    public final LinkcheckerAvailabilityStatusChecker getInstance() {
        if (ctx == null) {
            logger.error("Expecting a Spring context but none found.");
            throw new RuntimeException("Application context not available. Initialize factory first!");
        } else {
            logger.info("Requesting Spring bean for LinkcheckerAvailabilityStatusChecker");
            final LinkcheckerAvailabilityStatusChecker instance = ctx.getBean(LinkcheckerAvailabilityStatusChecker.class);
            logger.info("LinkcheckerAvailabilityStatusChecker instance retrieved from context: {}", instance);
            return instance;
        }
    }

    public void configureEnvironment(MutablePropertySources propertySources) {
        // can be overridden for configuration
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = {"eu.clarin.linkchecker.persistence", "eu.clarin.cmdi.vlo.importer.linkcheck"})
    @EnableJpaRepositories(basePackages = "eu.clarin.linkchecker.persistence.repository")
    @EntityScan(basePackages = "eu.clarin.linkchecker.persistence.model")
    public static class ApplicationConfig {

        @Autowired
        private StatusService statusService;

        @Autowired
        private DataSource dataSource;

        @Autowired
        private ConfigurableApplicationContext ctx;

        @Bean
        public LinkcheckerAvailabilityStatusChecker checker() {
            return new LinkcheckerAvailabilityStatusChecker(statusService, statusWriter(), closeHandler());
        }

        @Bean
        public Consumer<Writer> statusWriter() {
            return new LinkcheckerAvailabilityStatusCheckerStatusWriter(dataSource);
        }

        @Bean
        public Callable closeHandler() {
            return () -> {
                ctx.close();
                return null;
            };
        }
    }

}
