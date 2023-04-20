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
package eu.clarin.cmdi.vlo.api;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.api.service.ReactiveVloFacetsService;
import eu.clarin.cmdi.vlo.data.model.Facet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static eu.clarin.cmdi.vlo.util.VloApiConstants.QUERY_PARAMETER;

/**
 *
 * @author twagoo
 */
@Component
@Slf4j
public class VloFacetHandler extends VloHandlerProvider {

    private final AsyncCache<FacetsRequest, List<Facet>> facetCache;
    private final ReactiveVloFacetsService facetsService;

    public VloFacetHandler(ReactiveVloFacetsService facetsService,
            @Value("${vlo.api.cache.facet.ttl.seconds:60}") Long facetCacheTTL,
            @Value("${vlo.api.cache.facet.ttl.maxSize:1000}") Long facetCacheMaxSize,
            @Value("${vlo.api.cache.stats:false}") boolean cacheStats) {
        this.facetsService = facetsService;
        this.facetCache = cacheBuilder(facetCacheTTL, facetCacheTTL, cacheStats).buildAsync();
    }

    @CrossOrigin
    public Mono<ServerResponse> getFacets(ServerRequest request) {
        final String query = request.queryParam(QUERY_PARAMETER).orElse("*");
        final Map<String, ? extends Iterable<String>> filters = getFiltersFromRequest(request);
        //TODO: select subset of facets
        //TODO: override default value count

        final FacetsRequest fr = new FacetsRequest(query, filters, Optional.empty(), Optional.empty());

        final Mono<List<Facet>> cachedFacetsMono = Mono.fromFuture(facetCache.get(fr,
                (r, executor) -> facetsService.getAllFacets(r.getQuery(), r.getFilters(), r.getFacets(), r.getValueCount())
                        .collectList()
                        .toFuture())
        );

        return cachedFacetsMono
                .doOnNext(r -> logCacheStats("Facet", facetCache))
                .flatMap(facets -> ServerResponse.ok().bodyValue(facets));
    }

    @CrossOrigin
    public Mono<ServerResponse> getFacet(ServerRequest request) {
        final String facet = request.pathVariable("facet");
        final String query = request.queryParam(QUERY_PARAMETER).orElse("*");
        final Map<String, ? extends Iterable<String>> filters = getFiltersFromRequest(request);

        final FacetsRequest fr = new FacetsRequest(query, filters, Optional.of(ImmutableList.of(facet)), Optional.empty());

        final Mono<List<Facet>> cachedFacetMono = Mono.fromFuture(facetCache.get(fr,
                (r, executor) -> facetsService.getFacet(r.getFacets().map(l -> l.get(0)).orElseThrow(), r.getQuery(), r.getFilters(), r.getValueCount())
                        //single facet to list
                        .map(ImmutableList::of)
                        .toFuture())
        );

        return cachedFacetMono
                // list to single facet
                .map(list -> list.get(0))
                .doOnNext(r -> logCacheStats("Facet", facetCache))
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class FacetsRequest {

        private final String query;
        private final Map<String, ? extends Iterable<String>> filters;
        private final Optional<List<String>> facets;
        private final Optional<Integer> valueCount;
    }
}
