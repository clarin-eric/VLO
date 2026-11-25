/*
 * Copyright (C) 2026 CLARIN
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
package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.config.VloSpringConfig;
import jakarta.servlet.DispatcherType;
import org.apache.catalina.filters.ExpiresFilter;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.spring.SpringWebApplicationFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.error.ErrorPage;
import org.springframework.boot.web.error.ErrorPageRegistrar;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

/**
 * Starts the VLO web application with Tomcat
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@Import(VloSpringConfig.class)
public class VloWebApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(VloWebApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(VloWebApplication.class);
    }

    @Bean
    public FilterRegistrationBean<WicketFilter> wicketFilter() {
        final FilterRegistrationBean<WicketFilter> registration = new FilterRegistrationBean<>(new WicketFilter());
        registration.setName("wicket.vlo-web-app");
        registration.addUrlPatterns("/*");
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);
        registration.addInitParameter(WicketFilter.APP_FACT_PARAM, SpringWebApplicationFactory.class.getName());
        registration.addInitParameter(WicketFilter.FILTER_MAPPING_PARAM, "/*");
        return registration;
    }

    /**
     * Sets client-side (or proxy-side) caching headers on static content
     */
    @Bean
    public FilterRegistrationBean<ExpiresFilter> expiresFilter() {
        final FilterRegistrationBean<ExpiresFilter> registration = new FilterRegistrationBean<>(new ExpiresFilter());
        registration.addUrlPatterns("/images/*", "/assets/*");
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.addInitParameter("ExpiresDefault", "access plus 3 hours");
        return registration;
    }

    @Bean
    public ErrorPageRegistrar errorPageRegistrar() {
        return registry -> registry.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/error/page-not-found"));
    }

}
