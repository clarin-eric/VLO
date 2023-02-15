/*
 * Copyright (C) 2021 CLARIN ERIC <clarin@clarin.eu>
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

import eu.clarin.cmdi.vlo.elasticsearch.VloRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Configuration
@Profile({"default", "elastic"})
@EnableReactiveElasticsearchRepositories(basePackageClasses = VloRecordRepository.class)
public class VloElasticsearchConfiguration extends ReactiveElasticsearchConfiguration {

    @Value("${spring.data.elasticsearch.client.reactive.endpoints}")
    private String elasticSearchEndpoint;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticSearchEndpoint)
                .build();
    }

}
