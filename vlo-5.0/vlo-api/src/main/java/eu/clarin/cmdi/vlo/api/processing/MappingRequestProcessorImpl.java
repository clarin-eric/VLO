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

import eu.clarin.cmdi.vlo.data.model.VloRecordMappingProcessingTicket;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Component
public class MappingRequestProcessorImpl implements MappingRequestProcessor {

    private final MappingResultStore resultStore;

    public MappingRequestProcessorImpl(MappingResultStore resultStore) {
        this.resultStore = resultStore;
    }
    
    @Override
    public Mono<VloRecordMappingProcessingTicket> processMappingRequest(VloRecordMappingRequest request) {
        //TODO: store request data
        //TODO: create ticket & do queue processing

        return Mono.just(VloRecordMappingProcessingTicket.builder()
                            .file(request.getFile())
                            .processId("1234")
                            .build());
    }

}
