/*
 * Copyright (C) 2023 twagoo
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
package eu.clarin.cmdi.vlo.api.solr;

import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.vlo.api.service.FieldValueLabelService;
import eu.clarin.cmdi.vlo.api.service.impl.FieldValueLabelServiceImpl;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Data;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author twagoo
 */
@Configuration
@Profile("solr")
public class VloSolrConfiguration {

    @Autowired
    private SolrConfigurationProperties properties;

    @Bean
    public SolrDocumentQueryFactoryImpl queryFactory() {
        return new SolrDocumentQueryFactoryImpl(
                properties.getRecordProperties().getMinimalFields(),
                properties.getRecordProperties().getExtraFields(),
                properties.getFacetsProperties().getDefaultFields());
    }

    @Bean(destroyMethod = "close")
    public SolrClient solrClient() {
        return new HttpSolrClient.Builder(properties.getUrl()).build();
    }

    @Bean
    public Map<String, Function<String, String>> fieldValueLabelFunctionsMap() {
        return ImmutableMap.<String, Function<String, String>>builder()
                .put("languageCode",
                        new FieldValueLabelServiceImpl.PatternMatchingLabelFunction(
                                "((code|name):)?(.*)", 3))
                .build();
    }

    @Bean
    public FieldValueLabelService fieldValueLabelService() {
        // identity
        return new FieldValueLabelServiceImpl(fieldValueLabelFunctionsMap());
    }

    @Bean
    public JsonParser jsonParser() {
        return JsonParserFactory.getJsonParser();
    }

    @Bean
    public Converter<SolrDocument, VloRecord> recordConverter() {
        return new SolrVloRecordConverter(jsonParser());
    }

    @Bean
    public SolrService solrService(Converter<SolrDocument, VloRecord> recordConverter) {
        return new SolrService(queryFactory(), solrClient(),
                properties.getAuthProperties().getUsername(),
                properties.getAuthProperties().getPassword(),
                fieldValueLabelService(), recordConverter());
    }

    @Configuration
    @ConfigurationProperties(prefix = "solr")
    @Data
    public static class SolrConfigurationProperties {

        private String url;

        @Autowired
        @Name("record")
        private RecordProperties recordProperties;

        @Autowired
        @Name("facets")
        private FacetsProperties facetsProperties;

        @Autowired
        @Name("auth")
        private AuthProperties authProperties;

        @Configuration
        @Data
        public static class RecordProperties {

            private List<String> minimalFields;
            private List<String> extraFields;
        }

        @Configuration
        @Data
        public static class FacetsProperties {

            private Integer defaultValueLimit;
            private List<String> defaultFields = Collections.emptyList();
        }

        @Configuration
        @Data
        public static class AuthProperties {

            private String username;
            private String password;
        }

    }

}
