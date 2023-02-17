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
package eu.clarin.cmdi.vlo.api.configuration;

import eu.clarin.cmdi.vlo.api.service.solr.SolrDocumentQueryFactoryImpl;
import eu.clarin.cmdi.vlo.api.service.solr.SolrRecordService;
import java.util.Arrays;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 *
 * @author twagoo
 */
@Configuration
@Profile("solr")
public class VloSolrConfiguration {

    protected final static String[] DEFAULT_FIELDS = {
        "name", "creator", "description", "collection", "languageCode", "_languageCount", "multilingual", "modality", "continent", "country", "genre", "subject", "organisation", "license", "licenseType", "availability", "accessInfo", "keywords", "nationalProject", "resourceClass", "_resourceRef", "_selfLink", "id"
    };

    @Value("${solr.auth.username}")
    private String solrUsermame;

    @Value("${solr.auth.password}")
    private String solrPassword;

    @Value("${solr.url}")
    private String solrUrl;

    @Bean
    public SolrDocumentQueryFactoryImpl queryFactory() {
        return new SolrDocumentQueryFactoryImpl(Arrays.asList(DEFAULT_FIELDS));
    }

    @Bean(destroyMethod = "close")
    public SolrClient solrClient() {
        return new HttpSolrClient.Builder(solrUrl).build();
    }

    @Bean
    public SolrRecordService solrRecordService() {
        return new SolrRecordService(queryFactory(), solrClient(), solrUsermame, solrPassword);
    }

}
