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

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.service.solr.AutoCompleteService;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.SearchResultsDao;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import eu.clarin.cmdi.vlo.service.solr.SolrFacetQueryFactory;
import eu.clarin.cmdi.vlo.service.solr.impl.AutoCompleteServiceImpl;
import eu.clarin.cmdi.vlo.service.solr.impl.SearchResultsDaoImpl;
import eu.clarin.cmdi.vlo.service.solr.impl.SolrDocumentQueryFactoryImpl;
import eu.clarin.cmdi.vlo.service.solr.impl.SolrDocumentServiceImpl;
import eu.clarin.cmdi.vlo.service.solr.impl.SolrFacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.impl.SolrFacetQueryFactoryImpl;
import java.util.List;
import javax.inject.Inject;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans for SOLR related services (DAO's, query factories)
 *
 * @author twagoo
 */
@Configuration
public class VloSolrSpringConfig {

    @Inject
    private VloConfig vloConfig;

    @Bean
    public FacetFieldsService facetFieldsService() {
        return new SolrFacetFieldsService(searchResultsDao(), facetQueryFactory());
    }

    @Bean
    public SolrFacetQueryFactory facetQueryFactory() {
        return new SolrFacetQueryFactoryImpl();
    }

    @Bean
    public SolrDocumentService documentService() {
        return new SolrDocumentServiceImpl(searchResultsDao(), documentQueryFactory());
    }

    @Bean
    public SearchResultsDao searchResultsDao() {
        return new SearchResultsDaoImpl(solrServer(), vloConfig);
    }

    @Bean
    public SolrDocumentQueryFactoryImpl documentQueryFactory() {
        return new SolrDocumentQueryFactoryImpl(DOCUMENT_FIELDS);
    }

    @Bean
    public AutoCompleteService autoCompleteService() {
        return new AutoCompleteServiceImpl(solrServer(), vloConfig);
    }

    @Bean
    public SolrServer solrServer() {
        return new HttpSolrServer(vloConfig.getSolrUrl());
    }

    @Bean(name = "documentFieldOrder")
    public List<String> documentFieldOrder() {
        return DOCUMENT_FIELDS;
    }

    /**
     * Fields to request for documents. TODO: Make configurable?
     *
     */
    public static final List<String> DOCUMENT_FIELDS = ImmutableList.of(
            FacetConstants.FIELD_NAME,
            FacetConstants.FIELD_DESCRIPTION,
            FacetConstants.FIELD_COLLECTION,
            FacetConstants.FIELD_LANGUAGE_CODE,
            FacetConstants.FIELD_MODALITY,
            FacetConstants.FIELD_CONTINENT,
            FacetConstants.FIELD_COUNTRY,
            FacetConstants.FIELD_GENRE,
            FacetConstants.FIELD_SUBJECT,
            FacetConstants.FIELD_ORGANISATION,
            FacetConstants.FIELD_LICENSE,
            FacetConstants.FIELD_AVAILABILITY,
            FacetConstants.FIELD_ACCESS_INFO,
            FacetConstants.FIELD_KEYWORDS,
            FacetConstants.FIELD_NATIONAL_PROJECT,
            FacetConstants.FIELD_RESOURCE_CLASS,
            FacetConstants.FIELD_RESOURCE,
            FacetConstants.FIELD_SELF_LINK,
            FacetConstants.FIELD_ID,
            FacetConstants.FIELD_DATA_PROVIDER,
            FacetConstants.FIELD_FILENAME,
            FacetConstants.FIELD_FORMAT,
            FacetConstants.FIELD_LANDINGPAGE,
            FacetConstants.FIELD_SEARCHPAGE,
            FacetConstants.FIELD_SEARCH_SERVICE,
            FacetConstants.FIELD_LAST_SEEN,
            FacetConstants.FIELD_CLARIN_PROFILE,
            FacetConstants.FIELD_COMPLETE_METADATA,
            FacetConstants.FIELD_HIERARCHY_WEIGHT,
            FacetConstants.FIELD_HAS_PART,
            FacetConstants.FIELD_HAS_PART_COUNT,
            FacetConstants.FIELD_RESOURCE_COUNT,
            FacetConstants.FIELD_IS_PART_OF,
            FacetConstants.FIELD_SOLR_SCORE
    );
}
