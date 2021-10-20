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
package eu.clarin.cmdi.vlo.api.processing;

import eu.clarin.cmdi.vlo.api.parsing.MetadataFileParser;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingProcessingTicket;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingRequest;
import eu.clarin.cmdi.vlo.exception.InputProcessingException;
import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Component
@AllArgsConstructor
@Slf4j
public class MappingRequestProcessorImpl implements MappingRequestProcessor {

    private final MappingResultStore<UUID> resultStore;
    private final MetadataFileParser parser;
    @Qualifier("mappingRequestProcessorScheduler")
    private final Scheduler scheduler;

    @Override
    public Mono<VloRecordMappingProcessingTicket> processMappingRequest(Mono<VloRecordMappingRequest> requestMono) {
        final UUID ticketId = UUID.randomUUID();

        return requestMono
                .publishOn(scheduler)
                .flatMap(request -> Mono.fromCallable(
                () -> mapAndStoreResults(ticketId, request)));
    }

    private VloRecordMappingProcessingTicket mapAndStoreResults(final UUID ticketId, VloRecordMappingRequest request) throws InputProcessingException {
        log.debug("Start processing request with ticket ID {}", ticketId);

        final VloRecord record = parser.parseFile(request);

        log.debug("Storing mapping result for ticket {}", ticketId);
        resultStore.storeResult(ticketId, record);

        final VloRecordMappingProcessingTicket ticket = VloRecordMappingProcessingTicket.builder()
                .file(request.getFile())
                .processId(ticketId.toString())
                .build();

        log.debug("Done, ticket {} created", ticketId);

        return ticket;
    }

}
