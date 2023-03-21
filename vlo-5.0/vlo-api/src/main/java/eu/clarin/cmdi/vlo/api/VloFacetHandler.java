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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@AllArgsConstructor
@Slf4j
public class VloFacetHandler {

    private final ReactiveVloFacetsService facetsService;

    @CrossOrigin
    public Mono<ServerResponse> getFacets(ServerRequest request) {
        final String query = request.queryParam(QUERY_PARAMETER).orElse("*");
        return facetsService.getAllFacets(query)
                .collectList()
                .flatMap(count -> ServerResponse.ok().bodyValue(count));
    }

    @CrossOrigin
    public Mono<ServerResponse> getFacet(ServerRequest request) {
        final String facet = request.pathVariable("facet");
        final String query = request.queryParam(QUERY_PARAMETER).orElse("*");
        return facetsService.getFacet(facet, query)
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }
}
