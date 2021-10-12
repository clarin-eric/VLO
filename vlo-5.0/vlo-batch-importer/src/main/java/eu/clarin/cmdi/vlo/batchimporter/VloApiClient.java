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
package eu.clarin.cmdi.vlo.batchimporter;

import eu.clarin.cmdi.vlo.data.model.VloImportProcessingTicket;
import eu.clarin.cmdi.vlo.data.model.VloImportRequest;
import java.io.IOException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class VloApiClient {

    private final WebClient webClient;

    public VloApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<VloImportProcessingTicket> sendImportRequest(VloImportRequest importRequest) throws IOException {
        return webClient
                .method(HttpMethod.POST)
                .uri("/import-request")
                .body(Mono.just(importRequest), VloImportRequest.class)
                .exchangeToMono(this::handleResponseForImportRequest);
    }

    private Mono<VloImportProcessingTicket> handleResponseForImportRequest(ClientResponse response) {
        if (response.statusCode().equals(HttpStatus.OK)) {
            return response.bodyToMono(VloImportProcessingTicket.class);
        } else {
            return response.createException().flatMap(Mono::error);
        }
    }

}
