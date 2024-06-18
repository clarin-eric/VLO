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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import eu.clarin.cmdi.vlo.JavaScriptResources;
import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.MappingDefinitionResolver;
import eu.clarin.cmdi.vlo.facets.FacetsConfigurationsMarshaller;
import eu.clarin.cmdi.vlo.facets.configuration.FacetsConfiguration;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.ExposureTracker;
import eu.clarin.cmdi.vlo.service.FacetConditionEvaluationService;
import eu.clarin.cmdi.vlo.service.FacetParameterMapper;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.service.FieldValueOrderingsFactory;
import eu.clarin.cmdi.vlo.service.PIDResolver;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.service.PermalinkService;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.service.ResourceTypeCountingService;
import eu.clarin.cmdi.vlo.service.UriResolver;
import eu.clarin.cmdi.vlo.service.XmlTransformationService;
import eu.clarin.cmdi.vlo.service.centreregistry.CachingEndPointProvidersService;
import eu.clarin.cmdi.vlo.service.centreregistry.CentreRegistryProvidersService;
import eu.clarin.cmdi.vlo.service.centreregistry.EndpointProvidersService;
import eu.clarin.cmdi.vlo.service.impl.DOIResolver;
import eu.clarin.cmdi.vlo.service.impl.DocumentParametersConverter;
import eu.clarin.cmdi.vlo.service.impl.ExclusiveFieldFilter;
import eu.clarin.cmdi.vlo.service.impl.ExposureTrackerImpl;
import eu.clarin.cmdi.vlo.service.impl.FacetConditionEvaluationServiceImpl;
import eu.clarin.cmdi.vlo.service.impl.FacetParameterMapperImpl;
import eu.clarin.cmdi.vlo.service.impl.FieldValueOrderingsFactoryImpl;
import eu.clarin.cmdi.vlo.service.impl.HandleResolverWrapper;
import eu.clarin.cmdi.vlo.service.impl.InclusiveFieldFilter;
import eu.clarin.cmdi.vlo.service.impl.PermalinkServiceImpl;
import eu.clarin.cmdi.vlo.service.impl.QueryFacetsSelectionParametersConverter;
import eu.clarin.cmdi.vlo.service.impl.ResourceStringConverterImpl;
import eu.clarin.cmdi.vlo.service.impl.ResourceTypeCountingServiceImpl;
import eu.clarin.cmdi.vlo.service.impl.SearchContextParametersConverter;
import eu.clarin.cmdi.vlo.service.impl.UriResolverImpl;
import eu.clarin.cmdi.vlo.service.impl.XmlTransformationServiceImpl;
import eu.clarin.cmdi.vlo.wicket.provider.FieldValueConverterProvider;
import eu.clarin.cmdi.vlo.wicket.provider.FieldValueConverterProviderImpl;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import javax.inject.Inject;
import jakarta.xml.bind.JAXBException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import nl.mpi.archiving.corpusstructure.core.handle.CachingHandleResolver;
import nl.mpi.archiving.corpusstructure.core.handle.HandleRestApiResolver;
import org.apache.solr.common.SolrDocument;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xml.sax.InputSource;

/**
 * Beans for services used by the VLO web application (converters, resolvers,
 * filters)
 *
 * @author twagoo
 */
@Configuration
public class VloServicesSpringConfig {

    /**
     * Handle resolution cache expiry in seconds
     */
    private static final Duration HANDLE_CACHE_EXPIRY = Duration.ofHours(1);
    private static final Duration ENDPOINTS_CACHE_EXPIRY = Duration.ofHours(2);

    @Inject
    VloConfig vloConfig;

    @Inject
    FieldNameService fieldNameService;

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
        return new UriResolverImpl(handleResolver(), doiResolver());
    }

    public PIDResolver handleResolver() {
        return new HandleResolverWrapper(new CachingHandleResolver(new HandleRestApiResolver(), HANDLE_CACHE_EXPIRY), Duration.ofMillis(vloConfig.getHandleResolverTimeout()));
    }

    public PIDResolver doiResolver() {
        return new DOIResolver();
    }

    @Bean
    public FacetParameterMapper facetParameterMapper() {
        return new FacetParameterMapperImpl(languageCodeUtils());
    }

    @Bean(name = "queryParametersConverter")
    public PageParametersConverter<QueryFacetsSelection> queryParametersConverter() {
        return new QueryFacetsSelectionParametersConverter(facetParameterMapper(), vloConfig.getFields().values());
    }

    @Bean(name = "documentParamsConverter")
    public PageParametersConverter<SolrDocument> documentParamsConverter() {
        return new DocumentParametersConverter();
    }

    @Bean(name = "searchContextParamsConverter")
    public PageParametersConverter<SearchContext> searchContextParamsConverter() {
        return new SearchContextParametersConverter(queryParametersConverter());
    }

    @Bean(name = "fieldValueSorters")
    public Map<String, Ordering<String>> fieldValueSorters() {
        return fieldValueOrderingsFactory().createFieldValueOrderingMap();
    }

    @Bean
    public FieldValueOrderingsFactory fieldValueOrderingsFactory() {
        return new FieldValueOrderingsFactoryImpl();
    }

    @Bean
    public XmlTransformationService cmdiTransformationService() throws TransformerConfigurationException {
        final Source xsltSource = new StreamSource(getClass().getResourceAsStream("/cmdi2xhtml.xsl"));
        //TODO: Read properties from file??
        final Properties transformationProperties = new Properties();
        transformationProperties.setProperty(OutputKeys.METHOD, "html");
        transformationProperties.setProperty(OutputKeys.INDENT, "no");
        transformationProperties.setProperty(OutputKeys.ENCODING, "UTF-8");
        return new XmlTransformationServiceImpl(xsltSource, transformationProperties);
    }

    @Bean(name = "basicPropertiesFilter")
    public FieldFilter basicPropertiesFieldFilter() throws JAXBException, IOException {
        return new ExclusiveFieldFilter(Sets.union(
                ImmutableSet.copyOf(facetConfigurationService().getIgnoredFieldNames()),
                ImmutableSet.copyOf(facetConfigurationService().getTechnicalFieldNames())));
    }

    @Bean(name = "searchResultPropertiesFilter")
    public FieldFilter searchResultPropertiesFilter() throws JAXBException, IOException {
        return new InclusiveFieldFilter(facetConfigurationService().getSearchResultFieldNames());
    }

    @Bean(name = "technicalPropertiesFilter")
    public FieldFilter technicalPropertiesFieldFilter() throws JAXBException, IOException {
        return new InclusiveFieldFilter(
                facetConfigurationService().getTechnicalFieldNames());
    }

    @Bean
    public LanguageCodeUtils languageCodeUtils() {
        return new LanguageCodeUtils(vloConfig);
    }

    @Bean
    public FieldValueConverterProvider fieldValueConverters() {
        return new FieldValueConverterProviderImpl(languageCodeUtils(), vloConfig);
    }

    @Bean(initMethod = "init")
    public FacetConfigurationService facetConfigurationService() throws JAXBException, IOException {
        return new FacetConfigurationServiceImpl(facetsConfiguration());
    }

    @Bean
    public FacetConditionEvaluationService facetConditionEvaluationService() throws JAXBException, IOException {
        return new FacetConditionEvaluationServiceImpl(facetsConfiguration());
    }

    @Bean
    public FacetsConfiguration facetsConfiguration() throws JAXBException, IOException {
        final Source streamSource = getFacetsConfigSource();
        return facetsConfigMarshaller().unmarshal(streamSource);
    }

    private Source getFacetsConfigSource() throws IOException {
        final String facetConceptsFile = vloConfig.getFacetsConfigFile();

        if (Strings.isNullOrEmpty(facetConceptsFile)) {
            //logger.info("No facet concepts file configured. Reading default definitions from packaged file.");
            return new StreamSource(getClass().getResourceAsStream(VloConfig.DEFAULT_FACETS_CONFIG_RESOURCE_FILE));
        } else {
            final InputSource stream = mappingDefinitionResolver().tryResolveUrlFileOrResourceStream(facetConceptsFile);
            if (stream != null) {
                //  logger.info("Reading facet definitions from {}", facetConceptsFile);
                return new StreamSource(stream.getByteStream(), stream.getSystemId());
            } else {
                return null;
            }
        }
    }

    @Bean
    public MappingDefinitionResolver mappingDefinitionResolver() {
        return new MappingDefinitionResolver(FacetConfigurationServiceImpl.class);
    }

    @Bean
    public FacetsConfigurationsMarshaller facetsConfigMarshaller() throws JAXBException {
        return new FacetsConfigurationsMarshaller();
    }

    @Bean
    public PermalinkService permalinkService() {
        return new PermalinkServiceImpl(queryParametersConverter());
    }

    @Bean
    public PiwikConfig piwikConfig() {
        return new PiwikConfig();
    }

    @Bean
    public SnippetConfig snippetConfig() {
        return new SnippetConfig();
    }

    @Bean
    public EndpointProvidersService endpointProvidersService() {
        return new CachingEndPointProvidersService(new CentreRegistryProvidersService(vloConfig), ENDPOINTS_CACHE_EXPIRY);
    }

    @Bean
    public ExposureTracker exposureTracker() {
        return new ExposureTrackerImpl(vloConfig);
    }

    @Bean
    public JavaScriptResources javaScriptResources() {
        return new JavaScriptResources(vloConfig);
    }

}
