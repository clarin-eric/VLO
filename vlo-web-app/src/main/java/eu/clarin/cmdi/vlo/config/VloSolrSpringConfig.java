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
import eu.clarin.cmdi.vlo.service.solr.AutoCompleteService;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.SearchResultsDao;
import eu.clarin.cmdi.vlo.service.solr.SimilarDocumentsService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import eu.clarin.cmdi.vlo.service.solr.SolrFacetQueryFactory;
import eu.clarin.cmdi.vlo.service.solr.impl.AutoCompleteServiceImpl;
import eu.clarin.cmdi.vlo.service.solr.impl.SearchResultsDaoImpl;
import eu.clarin.cmdi.vlo.service.solr.impl.SimilarDocumentsServiceImpl;
import eu.clarin.cmdi.vlo.service.solr.impl.SolrDocumentQueryFactoryImpl;
import eu.clarin.cmdi.vlo.service.solr.impl.SolrDocumentServiceImpl;
import eu.clarin.cmdi.vlo.service.solr.impl.SolrFacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.impl.SolrFacetQueryFactoryImpl;
import java.util.List;
import javax.inject.Inject;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
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
   @Inject
   FieldNameService fieldNameService;

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
   public SimilarDocumentsService similarDocumentsService() {
      return new SimilarDocumentsServiceImpl(searchResultsDao(), documentQueryFactory());
   }

   @Bean
   public SearchResultsDao searchResultsDao() {
      return new SearchResultsDaoImpl(solrClient(), vloConfig, fieldNameService);
   }

   @Bean
   public SolrDocumentQueryFactoryImpl documentQueryFactory() {
      return new SolrDocumentQueryFactoryImpl(getDocumentFields(), fieldNameService);
   }

   @Bean
   public AutoCompleteService autoCompleteService() {
      return new AutoCompleteServiceImpl(solrClient(), vloConfig, fieldNameService);
   }

   @Bean(destroyMethod = "close")
   public SolrClient solrClient() {
      return new HttpSolrClient.Builder(vloConfig.getSolrUrl()).build();
   }

   @Bean(name = "documentFieldOrder")
   public List<String> documentFieldOrder() {
      return getDocumentFields();
   }

   /**
    * Fields to request for documents. 
    *
    */
   // public List<String> DOCUMENT_FIELDS = ImmutableList.of(
   public List<String> getDocumentFields() {
      return  ImmutableList.copyOf(vloConfig.getFields().values());
   }
}
