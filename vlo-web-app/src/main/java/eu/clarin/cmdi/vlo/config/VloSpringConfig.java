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
package eu.clarin.cmdi.vlo.config;

import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.service.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.SearchResultsDao;
import eu.clarin.cmdi.vlo.service.SolrDocumentService;
import eu.clarin.cmdi.vlo.service.SolrFacetQueryFactory;
import eu.clarin.cmdi.vlo.service.impl.SearchResultsDaoImpl;
import eu.clarin.cmdi.vlo.service.impl.SolrDocumentQueryFactoryImpl;
import eu.clarin.cmdi.vlo.service.impl.SolrDocumentServiceImpl;
import eu.clarin.cmdi.vlo.service.impl.SolrFacetFieldsService;
import eu.clarin.cmdi.vlo.service.impl.SolrFacetQueryFactoryImpl;
import java.io.IOException;
import java.util.Collections;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Annotation based Spring configuration for the VLO web application.
 *
 * Note: this works because
 * {@link org.apache.wicket.spring.SpringWebApplicationFactory} is used in place
 * of the standard Wicket application factory and annotation driven
 * configuration is enabled in WEB-INF/applicationContext.xml
 *
 * @author twagoo
 */
@Configuration
public class VloSpringConfig {

    public final static String FACETS_PANEL_SERVICE = "factsPanelService";
    public final static String COLLECTION_FACET_SERVICE = "factsPanelService";
    
    /**
     *
     * @return the web application object that represents the Wicket application
     */
    @Bean
    public VloWicketApplication webApplication() {
        return new VloWicketApplication();
    }

    @Bean
    public VloConfig vloConfig() {
        try {
            return vloConfigFactory().newConfig();
        } catch (IOException ex) {
            throw new RuntimeException("Could not read VLO configuration", ex);
        }
    }

    @Bean
    public VloConfigFactory vloConfigFactory() {
        return new DefaultVloConfigFactory();
    }

    @Bean(name = FACETS_PANEL_SERVICE)
    public FacetFieldsService facetFieldsService() {
        return new SolrFacetFieldsService(searchResultsDao(), facetQueryFactory());
    }

    @Bean
    public SolrFacetQueryFactory facetQueryFactory() {
        return new SolrFacetQueryFactoryImpl(vloConfig().getFacetFields());
    }

    @Bean(name = COLLECTION_FACET_SERVICE)
    public FacetFieldsService collectionFacetFieldsService() {
        return new SolrFacetFieldsService(searchResultsDao(), collectionFacetQueryFactory());
    }
    
    @Bean
    public SolrFacetQueryFactory collectionFacetQueryFactory() {
        return new SolrFacetQueryFactoryImpl(Collections.singletonList("collection"));
    }
    
    @Bean
    public SolrDocumentService documentService() {
        return new SolrDocumentServiceImpl(searchResultsDao(), documentQueryFactory());
    }

    @Bean
    public SearchResultsDao searchResultsDao() {
        return new SearchResultsDaoImpl(solrServer(), vloConfig());
    }
    
    @Bean
    public SolrDocumentQueryFactoryImpl documentQueryFactory() {
        return new SolrDocumentQueryFactoryImpl();
    }

    @Bean
    public SolrServer solrServer() {
        return new HttpSolrServer(vloConfig().getSolrUrl());
    }
}
