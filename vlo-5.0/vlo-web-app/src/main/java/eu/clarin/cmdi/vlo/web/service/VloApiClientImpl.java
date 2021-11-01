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
package eu.clarin.cmdi.vlo.web.service;

import eu.clarin.cmdi.vlo.data.model.VloRecord;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class VloApiClientImpl implements VloApiClient {
    
    private final WebClient webClient;
    
    public VloApiClientImpl(WebClient webClient) {
        this.webClient = webClient;
    }
    
    @Override
    public Flux<VloRecord> getRecords(String q, Optional<Integer> rows, Optional<Integer> start) {
        log.debug("Getting records");
        return webClient
                .method(HttpMethod.GET)
                .uri(uriBuilder
                        -> uriBuilder
                        .path("/records")
                        .queryParam("q", q)
                        .queryParamIfPresent("rows", rows)
                        .queryParamIfPresent("start", start)
                        .build())
                .exchangeToFlux(this::handleResponseForGetRecords);
    }
    
    private Flux<VloRecord> handleResponseForGetRecords(ClientResponse response) {
        if (response.statusCode().equals(HttpStatus.OK)) {
            return response.bodyToFlux(VloRecord.class);
        } else {
            log.error("API response: {}", response.statusCode());
            return response.createException().flatMapMany(Mono::error);
        }
    }
}
