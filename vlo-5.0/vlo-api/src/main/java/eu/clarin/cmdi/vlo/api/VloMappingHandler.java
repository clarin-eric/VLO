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
package eu.clarin.cmdi.vlo.api;

import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingProcessingTicket;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Component
@Slf4j
public class VloMappingHandler {

    public Mono<ServerResponse> requestMapping(ServerRequest request) {
        log.info("Incoming mapping request. Extracting body...");
        Mono<VloRecordMappingRequest> mappingRequestMono = request.bodyToMono(VloRecordMappingRequest.class);
        log.info("Incoming mapping request {}", mappingRequestMono);

        Mono<VloRecordMappingProcessingTicket> resultMono = mappingRequestMono
                .flatMap(mappingRequest -> {
                    //TODO: store request data
                    //TODO: create ticket & do queue processing
                    return Mono.just(VloRecordMappingProcessingTicket.builder()
                            .file(mappingRequest.getFile())
                            .processId("id")
                            .build());
                });

        //respond
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(resultMono, VloRecordMappingProcessingTicket.class);
    }

    public Mono<ServerResponse> getMappingResult(ServerRequest request) {
        final String id = request.pathVariable("id");
        log.info("Retrieval of mapping result {}", id);

        //TODO: get from processing queue
        final VloRecord record = VloRecord.builder()
                .id("recordId")
                .name("recordName")
                .build();

        //respond
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(record), VloRecord.class);
    }

}
