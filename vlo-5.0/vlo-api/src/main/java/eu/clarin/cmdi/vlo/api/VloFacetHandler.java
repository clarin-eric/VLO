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

import eu.clarin.cmdi.vlo.api.service.ReactiveVloFacetsService;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.QUERY_PARAMETER;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 *
 * @author twagoo
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VloFacetHandler extends VloHandlerProvider {

    @Value("${vlo.api.cache.facet.ttl.seconds:60}")
    private Long facetCacheTTL;

    private final ReactiveVloFacetsService facetsService;

    @CrossOrigin
    public Mono<ServerResponse> getFacets(ServerRequest request) {
        final String query = request.queryParam(QUERY_PARAMETER).orElse("*");
        final Map<String, ? extends Iterable<String>> filters = getFiltersFromRequest(request);
        return facetsService.getAllFacets(query, filters, Optional.empty(), Optional.empty())
                .collectList()
                .flatMap(facets -> ServerResponse.ok().bodyValue(facets));
    }

    @CrossOrigin
    public Mono<ServerResponse> getFacet(ServerRequest request) {
        final String facet = request.pathVariable("facet");
        final String query = request.queryParam(QUERY_PARAMETER).orElse("*");
        final Map<String, ? extends Iterable<String>> filters = getFiltersFromRequest(request);
        return facetsService.getFacet(facet, query, filters, Optional.empty())
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }
}
