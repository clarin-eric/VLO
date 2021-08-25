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

import eu.clarin.cmdi.vlo.api.data.VloRecordRepository;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import java.util.Optional;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Component
public class VloRecordHandler {

    private final ElasticsearchRestTemplate elasticsearchRestTemplate;
    private final VloRecordRepository recordRepository;

    public VloRecordHandler(ElasticsearchRestTemplate elasticsearchRestTemplate, VloRecordRepository recordRepository) {
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
        this.recordRepository = recordRepository;
    }

    public Mono<ServerResponse> getRecord(ServerRequest request) {
        final String id = request.pathVariable("id");
        return getRecordFromRepository(id);
    }
    
    public Mono<ServerResponse> getRecordFromRepository(String id) {
        final Optional<VloRecord> result = recordRepository.findById(id);
        return result.map(record -> ServerResponse.ok().bodyValue(record))
                .orElse(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getRecordFromTemplate(String id) {
        final Optional<VloRecord> result = Optional.ofNullable(elasticsearchRestTemplate.get(id, VloRecord.class));
        return result.map(record -> ServerResponse.ok().bodyValue(record))
                .orElse(ServerResponse.notFound().build());
    }
}