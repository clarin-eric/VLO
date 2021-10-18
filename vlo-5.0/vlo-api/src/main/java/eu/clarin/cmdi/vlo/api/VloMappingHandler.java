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

import eu.clarin.cmdi.vlo.api.processing.MappingRequestProcessor;
import eu.clarin.cmdi.vlo.api.processing.MappingResultStore;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingProcessingTicket;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@Slf4j
public class VloMappingHandler {

    private final MappingRequestProcessor mappingRequestProcessor;

    private final MappingResultStore<UUID> resultStore;

    public Mono<ServerResponse> requestMapping(ServerRequest request) {
        log.debug("Incoming mapping request. Extracting body...");
        final Mono<VloRecordMappingRequest> mappingRequestMono
                = request.bodyToMono(VloRecordMappingRequest.class);

        //processing
        final Mono<VloRecordMappingProcessingTicket> resultMono
                = mappingRequestProcessor.processMappingRequest(mappingRequestMono);

        //response
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(resultMono, VloRecordMappingProcessingTicket.class);

//        return resultMono.flatMap(result
//                -> ServerResponse
//                        .ok()
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .body(result, VloRecordMappingProcessingTicket.class));
    }

    public Mono<ServerResponse> getMappingResult(ServerRequest request) {
        final String id = request.pathVariable("id");
        log.info("Retrieval of mapping result {}", id);

        final Optional<VloRecord> record = resultStore.getMappingResult(UUID.fromString(id));

        //respond
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.justOrEmpty(record), VloRecord.class)
                .switchIfEmpty(ServerResponse.notFound().build());
    }

}
