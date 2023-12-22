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
package eu.clarin.cmdi.vlo.web.configuration;

import eu.clarin.cmdi.vlo.web.service.VloApiClient;
import eu.clarin.cmdi.vlo.web.service.VloApiClientImpl;
import javax.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Configuration
@Validated
public class VloWebAppConfiguration {
    
    @NotEmpty
    @Value("${vlo.web.api.base-url}")
    private String apiBaseUrl;
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
        @Bean(name = "vloApiWebClient")
    WebClient apiWebClient() {
        return webClientBuilder
                .baseUrl(apiBaseUrl)
                .build();
    }

    @Bean
    public VloApiClient apiClient() {
        final WebClient webClient = apiWebClient();
        return new VloApiClientImpl(webClient);
    }
    
}
