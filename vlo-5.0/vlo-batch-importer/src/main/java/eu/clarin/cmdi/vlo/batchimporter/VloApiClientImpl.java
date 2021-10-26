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

import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingProcessingTicket;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
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
    public Mono<VloRecordMappingProcessingTicket> sendRecordMappingRequest(VloRecordMappingRequest importRequest) {
        log.debug("Sending mapping request {}", importRequest);
        return webClient
                .method(HttpMethod.POST)
                .uri("/recordMapping/request")
                .body(Mono.just(importRequest), VloRecordMappingRequest.class)
                .exchangeToMono(this::handleResponseForImportRequest);
    }

    private Mono<VloRecordMappingProcessingTicket> handleResponseForImportRequest(ClientResponse response) {
        log.debug("Handling response");
        if (response.statusCode().equals(HttpStatus.OK)) {
            return response.bodyToMono(VloRecordMappingProcessingTicket.class);
        } else {
            log.error("API response: {}", response.statusCode());
            return response.createException().flatMap(Mono::error);
        }
    }

    @Override
    public Mono<VloRecord> retrieveRecord(VloRecordMappingProcessingTicket ticket) {
        log.debug("Retrieving mapped record mono for processing ticket {}", ticket);
        return webClient
                .method(HttpMethod.GET)
                .uri("/recordMapping/result/{id}", ticket.getProcessId())
                .exchangeToMono(this::handleResponseForRecordRetrieval);
    }

    private Mono<VloRecord> handleResponseForRecordRetrieval(ClientResponse response) {
        log.debug("Handling response");
        if (response.statusCode().equals(HttpStatus.OK)) {
            return response.bodyToMono(VloRecord.class);
        } else if (response.statusCode().is4xxClientError()) {
            log.warn("API response: {}", response.statusCode());
            return Mono.empty();
        } else {
            log.error("API response: {}", response.statusCode());
            return response.createException().flatMap(Mono::error);
        }
    }
    
    @Override
    public Mono<ResponseEntity<Void>> saveRecord(Mono<VloRecord> record) {
        log.debug("Sending record to endpoint for indexation {}", record);
        return webClient
                .method(HttpMethod.PUT)
                .uri("/record")
                .body(record, VloRecord.class)
                .retrieve()
                .toBodilessEntity();
    }

}
