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

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import eu.clarin.cmdi.vlo.api.configuration.VloApiRouteConfiguration;
import eu.clarin.cmdi.vlo.api.service.ReactiveVloRecordService;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.QUERY_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.ROWS_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.FROM_PARAMETER;
import java.net.URI;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Component
//@RequiredArgsConstructor
@Slf4j
public class VloRecordHandler extends VloHandlerProvider {

    private final Long recordCacheTTL;
    private final Long recordCacheMaxSize;
    private final boolean cacheStats;

    private AsyncCache<String, VloRecord> recordCache;

    private final ReactiveVloRecordService recordService;

    public VloRecordHandler(ReactiveVloRecordService recordService, @Value("${vlo.api.cache.record.ttl.seconds:60}") Long recordCacheTTL, @Value("${vlo.api.cache.record.maxSize:1000}") Long recordCacheMaxSize, @Value("${vlo.api.cache.stats:false}") boolean cacheStats) {
        this.recordService = recordService;
        this.recordCacheTTL = recordCacheTTL;
        this.cacheStats = cacheStats;
        this.recordCacheMaxSize = recordCacheMaxSize;
        createCaches();
    }

    private void createCaches() {
        recordCache = cacheBuilder(recordCacheTTL, recordCacheMaxSize).buildAsync();
    }

    private <KEY, VALUE> Caffeine<KEY, VALUE> cacheBuilder(Long ttlSeconds, Long maxSize) {
        return cacheBuilder(ttlSeconds, maxSize, cacheStats);
    }

    @CrossOrigin
    public Mono<ServerResponse> getRecordCount(ServerRequest request) {
        final String query = request.queryParam(QUERY_PARAMETER).orElse("*");
        final Map<String, ? extends Iterable<String>> filters = getFiltersFromRequest(request);

        return recordService.getRecordCount(query, filters)
                .doOnNext(count -> log.debug("Search result count: {}", count))
                //map to response
                .flatMap(count -> ServerResponse.ok().bodyValue(count))
                .switchIfEmpty(ServerResponse.badRequest().bodyValue("No query in request"));
    }

    @CrossOrigin
    public Mono<ServerResponse> getRecords(ServerRequest request) {
        final String query = request.queryParam(QUERY_PARAMETER).orElse("*");
        final Map<String, ? extends Iterable<String>> filters = getFiltersFromRequest(request);
        int from = request.queryParam(FROM_PARAMETER).map(Integer::valueOf).orElse(0);
        int size = request.queryParam(ROWS_PARAMETER).map(Integer::valueOf).orElse(10);

        return recordService.getRecords(query, filters, from, size)
                .doOnNext(results -> log.debug("Results: {}", results))
                //map to response
                .flatMap(resultList -> ServerResponse.ok().bodyValue(resultList))
                .switchIfEmpty(ServerResponse.badRequest().bodyValue("No query in request"));
    }

    public Mono<ServerResponse> saveRecord(ServerRequest request) {
        log.debug("Incoming saving request");

        return request.bodyToMono(VloRecord.class)
                .doOnNext(record -> {
                    log.info("Saving record {} repository", record.getId());
                })
                .flatMap(recordService::saveRecord)
                .flatMap(response -> {
                    final URI uri = request.uriBuilder().pathSegment(response.getId()).build();
                    return ServerResponse.created(uri).bodyValue(response.toString());
                })
                .switchIfEmpty(ServerResponse.badRequest().build());
    }

    @CrossOrigin
    public Mono<ServerResponse> getRecordFromRepository(ServerRequest request) {
        final String id = request.pathVariable(VloApiRouteConfiguration.ID_PATH_VARIABLE);

        final Mono<VloRecord> cachedRecordMono = Mono.fromFuture(recordCache.get(id, (i, executor) -> {
            return recordService.getRecordById(i).toFuture();
        }));

        return cachedRecordMono
                .doOnNext(r -> logCacheStats("Records", recordCache))
                .flatMap(record -> ServerResponse.ok().bodyValue(record))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    @Override
    protected void logCacheStats(String name, AsyncCache<?, ?> cache) {
        if (cacheStats) {
            super.logCacheStats(name, cache);
        }
    }

}
