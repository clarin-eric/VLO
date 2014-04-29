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
import com.google.common.collect.Sets;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.service.handle.HandleClient;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.service.ResourceTypeCountingService;
import eu.clarin.cmdi.vlo.service.UriResolver;
import eu.clarin.cmdi.vlo.service.XmlTransformationService;
import eu.clarin.cmdi.vlo.service.impl.ExclusiveFieldFilter;
import eu.clarin.cmdi.vlo.service.handle.impl.HandleRestApiClient;
import eu.clarin.cmdi.vlo.service.impl.DocumentParametersConverter;
import eu.clarin.cmdi.vlo.service.impl.UriResolverImpl;
import eu.clarin.cmdi.vlo.service.impl.InclusiveFieldFilter;
import eu.clarin.cmdi.vlo.service.impl.ResourceStringConverterImpl;
import eu.clarin.cmdi.vlo.service.impl.ResourceTypeCountingServiceImpl;
import eu.clarin.cmdi.vlo.service.impl.XmlTransformationServiceImpl;
import eu.clarin.cmdi.vlo.service.solr.AutoCompleteService;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.SearchResultsDao;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import eu.clarin.cmdi.vlo.service.solr.SolrFacetQueryFactory;
import eu.clarin.cmdi.vlo.service.solr.impl.AutoCompleteServiceImpl;
import eu.clarin.cmdi.vlo.service.impl.QueryFacetsSelectionParametersConverter;
import eu.clarin.cmdi.vlo.service.impl.SearchContextParametersConverter;
import eu.clarin.cmdi.vlo.service.solr.impl.SearchResultsDaoImpl;
import eu.clarin.cmdi.vlo.service.solr.impl.SolrDocumentQueryFactoryImpl;
import eu.clarin.cmdi.vlo.service.solr.impl.SolrDocumentServiceImpl;
import eu.clarin.cmdi.vlo.service.solr.impl.SolrFacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.impl.SolrFacetQueryFactoryImpl;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
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
        return new ServletVloConfigFactory();
    }

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
        return new SearchResultsDaoImpl(solrServer(), vloConfig());
    }

    @Bean
    public SolrDocumentQueryFactoryImpl documentQueryFactory() {
        return new SolrDocumentQueryFactoryImpl(DOCUMENT_FIELDS);
    }

    @Bean
    public AutoCompleteService autoCompleteService() {
        return new AutoCompleteServiceImpl(solrServer(), vloConfig());
    }

    @Bean
    public ResourceTypeCountingService resourceTypeCountingService() {
        return new ResourceTypeCountingServiceImpl(resourceStringConverter());
    }

    @Bean(name = "resourceStringConverter")
    public ResourceStringConverter resourceStringConverter() {
        return new ResourceStringConverterImpl();
    }

    @Bean(name = "resolvingResourceStringConverter")
    public ResourceStringConverter resolvingResourceStringConverter() {
        return new ResourceStringConverterImpl(uriResolver());
    }

    @Bean
    public UriResolver uriResolver() {
        return new UriResolverImpl(handleClient());
    }

    public HandleClient handleClient() {
        return new HandleRestApiClient();
    }

    @Bean(name = "queryParametersConverter")
    public PageParametersConverter<QueryFacetsSelection> queryParametersConverter() {
        return new QueryFacetsSelectionParametersConverter();
    }

    @Bean(name = "documentParamsConverter")
    public PageParametersConverter<SolrDocument> documentParamsConverter() {
        return new DocumentParametersConverter();
    }

    @Bean(name = "searchContextParamsConverter")
    public PageParametersConverter<SearchContext> searchContextParamsConverter() {
        return new SearchContextParametersConverter(queryParametersConverter());
    }

    @Bean
    public SolrServer solrServer() {
        return new HttpSolrServer(vloConfig().getSolrUrl());
    }

    @Bean
    public XmlTransformationService cmdiTransformationService() throws TransformerConfigurationException {
        final Source xsltSource = new StreamSource(getClass().getResourceAsStream("/cmdi2xhtml.xsl"));
        //TODO: Read properties from file??
        final Properties transformationProperties = new Properties();
        transformationProperties.setProperty(OutputKeys.METHOD, "html");
        transformationProperties.setProperty(OutputKeys.INDENT, "yes");
        transformationProperties.setProperty(OutputKeys.ENCODING, "UTF-8");
        return new XmlTransformationServiceImpl(xsltSource, transformationProperties);
    }

    @Bean(name = "basicPropertiesFilter")
    public FieldFilter basicPropertiesFieldFilter() {
        return new ExclusiveFieldFilter(Sets.union(
                vloConfig().getIgnoredFields(),
                vloConfig().getTechnicalFields()));
    }

    @Bean(name = "searchResultPropertiesFilter")
    public FieldFilter searchResultPropertiesFilter() {
        return new InclusiveFieldFilter(vloConfig().getSearchResultFields());
    }

    @Bean(name = "technicalPropertiesFilter")
    public FieldFilter technicalPropertiesFieldFilter() {
        return new InclusiveFieldFilter(
                vloConfig().getTechnicalFields());
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
            FacetConstants.FIELD_LANGUAGES,
            FacetConstants.FIELD_MODALITY,
            FacetConstants.FIELD_CONTINENT,
            FacetConstants.FIELD_COUNTRY,
            FacetConstants.FIELD_GENRE,
            FacetConstants.FIELD_SUBJECT,
            FacetConstants.FIELD_ORGANISATION,
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
            FacetConstants.FIELD_COMPLETE_METADATA
    );
}
