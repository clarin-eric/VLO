/*
 * Copyright (C) 2023 twagoo
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
package eu.clarin.cmdi.vlo.api.service;

import eu.clarin.cmdi.vlo.api.model.VloRecordsRequest;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordSearchResult;
import eu.clarin.cmdi.vlo.elasticsearch.VloRecordRepository;
import eu.clarin.cmdi.vlo.util.Pagination;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author twagoo
 */
@Slf4j
@Component
@Profile({"elastic"})
public class VloRecordRepositoryBridge implements ReactiveVloRecordService {

    private final VloRecordRepository recordRepository;

    private final ReactiveElasticsearchOperations operations;

    public VloRecordRepositoryBridge(VloRecordRepository respository, ReactiveElasticsearchOperations operations) {
        this.recordRepository = respository;
        this.operations = operations;
    }

    @Override
    public Mono<VloRecordSearchResult> getRecords(VloRecordsRequest request) {
        final Pageable pageable = Pagination.pageRequestFor(request.getFrom(), request.getSize());
        final String query = request.getQuery();

        if (query == null) {
            final Mono<Long> countMono = recordRepository.countByIdNotNull();
            final Flux<VloRecord> recordsFlux = recordRepository.findByIdNotNull(pageable);
            return createSearchResultMono(countMono, recordsFlux, pageable);
        } else {
            final Flux<VloRecord> recordsFlux = queryToRecordsFlux(query, request.getFilters(), pageable);
            final Mono<Long> countMono = getRecordCount(query, request.getFilters());
            return createSearchResultMono(countMono, recordsFlux, pageable);
        }
    }

    private Flux<VloRecord> queryToRecordsFlux(String query, Map<String, ? extends Iterable<String>> filters, final Pageable pageable) {
        return createQuery(query, filters, Optional.of(pageable))
                //query to search result
                .flatMapMany(q -> operations.search(q, VloRecord.class))
                .doOnNext(hit -> log.trace("Search hit: {}", hit.getId()))
                //get content (record) for each hit, turn into list
                .flatMap(hit -> Mono.just(hit.getContent()));
    }

    private Mono<VloRecordSearchResult> createSearchResultMono(final Mono<Long> countMono, final Flux<VloRecord> recordsFux, Pageable pageable) {
        // combine count and records list into search results object
        return countMono.flatMap(count
                -> recordsFux
                        .collectList()
                        .map(records -> new VloRecordSearchResult(records, count, pageable.getOffset())));
    }

    @Override
    public Mono<VloRecord> getRecordById(final String id) {
        return recordRepository.findById(id);
    }

    @Override
    public Mono<Long> getRecordCount(final String query, Map<String, ? extends Iterable<String>> filters) {
        return createQuery(query, filters, Optional.empty())
                //query to search result
                .flatMap(q -> operations.count(q, VloRecord.class));
    }

    @Override
    public Mono<VloRecord> saveRecord(VloRecord record) {
        return recordRepository.save(record);
    }

    private Mono<? extends Query> createQuery(String queryParam, Map<String, ? extends Iterable<String>> filters, Optional<Pageable> pageable) {
        final Mono<String> qMono = Mono.justOrEmpty(queryParam);

        //TODO: apply filters
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
}
