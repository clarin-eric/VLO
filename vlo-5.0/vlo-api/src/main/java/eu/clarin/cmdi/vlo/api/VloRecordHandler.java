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
import eu.clarin.cmdi.vlo.elasticsearch.VloRecordRepository;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
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
public class VloRecordHandler {

    private final VloRecordRepository recordRepository;

    public VloRecordHandler(VloRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public Mono<ServerResponse> getRecordFromRepository(ServerRequest request) {
        final String id = request.pathVariable("id");
        return recordRepository.findById(id)
                .flatMap(record -> ServerResponse.ok().bodyValue(record))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> saveRecord(ServerRequest request) {
        log.debug("Incoming saving request");
        return request.bodyToMono(VloRecord.class)
                .doOnNext(record -> {
                    log.info("Saving record {} repository", record.getId());
                })
                .flatMap(recordRepository::save)
                .flatMap(response -> {
                    final URI uri = request.uriBuilder().pathSegment(response.getId()).build();
                    return ServerResponse.created(uri).bodyValue(response.toString());
                })
                .switchIfEmpty(ServerResponse.badRequest().build());
    }
}
