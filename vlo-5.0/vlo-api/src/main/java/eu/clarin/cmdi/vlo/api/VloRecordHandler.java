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

import eu.clarin.cmdi.vlo.api.configuration.VloApiRouteConfiguration;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.elasticsearch.VloRecordRepository;
import eu.clarin.cmdi.vlo.util.Pagination;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static eu.clarin.cmdi.vlo.util.VloApiConstants.QUERY_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.ROWS_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.START_PARAMETER;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import reactor.core.publisher.Flux;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Component
@Slf4j
public class VloRecordHandler {

    private final VloRecordRepository recordRepository;

    private final ReactiveElasticsearchOperations operations;

    public VloRecordHandler(VloRecordRepository recordRepository, ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.recordRepository = recordRepository;
        this.operations = reactiveElasticsearchTemplate;
    }

    public Mono<ServerResponse> getRecordCount(ServerRequest request) {
        final String query = request.queryParam(QUERY_PARAMETER).orElse("*");

        return getQuery(query, Optional.empty())
                //query to search result
                .flatMap(q -> operations.count(q, VloRecord.class))
                .doOnNext(count -> log.debug("Search result count: {}", count))
                //map to response
                .flatMap(count -> ServerResponse.ok().bodyValue(count))
                .switchIfEmpty(ServerResponse.badRequest().bodyValue("No query in request"));
    }

    public Mono<ServerResponse> getRecords(ServerRequest request) {
        final Optional<String> query = request.queryParam(QUERY_PARAMETER);
        int offset = request.queryParam(START_PARAMETER).map(Integer::valueOf).orElse(1);
        int size = request.queryParam(ROWS_PARAMETER).map(Integer::valueOf).orElse(5);

        return getRecords(query, offset, size)
                //turn into list mono
                .collectList()
                .doOnNext(results -> log.debug("Results: {}", results))
                //map to response
                .flatMap(resultList -> ServerResponse.ok().bodyValue(resultList))
                .switchIfEmpty(ServerResponse.badRequest().bodyValue("No query in request"));
    }

    private Flux<VloRecord> getRecords(final Optional<String> queryParam, int offset, int size) {
        final Pageable pageable = Pagination.pageRequestFor(offset, size);
        return queryParam.map(query
                -> getQuery(query, Optional.of(pageable))
                        //query to search result
                        .flatMapMany(q -> operations.search(q, VloRecord.class))
                        .doOnNext(hit -> log.trace("Search hit: {}", hit.getId()))
                        //get content (record) for each hit, turn into list
                        .flatMap(hit -> Mono.just(hit.getContent()))
        ).orElse(recordRepository.findByIdNotNull(pageable));
    }

    private Mono<? extends Query> getQuery(String queryParam, Optional<Pageable> pageable) {
        final Mono<String> qMono = Mono.justOrEmpty(queryParam);

        return qMono
                .doOnNext(qParam -> log.debug("Query in request: '{}'", qParam))
                .map(qParam -> {
                    final NativeQueryBuilder builder = NativeQuery.builder();
                    builder.withQuery(q -> q.queryString(s -> s.query(qParam)));
                    pageable.ifPresent(builder::withPageable);
                    return builder.build();
                })
                .doOnNext(q -> log.debug("Query prepared: {}", q));
    }

    public Mono<ServerResponse> getRecordFromRepository(ServerRequest request) {
        final String id = request.pathVariable(VloApiRouteConfiguration.ID_PATH_VARIABLE);
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
