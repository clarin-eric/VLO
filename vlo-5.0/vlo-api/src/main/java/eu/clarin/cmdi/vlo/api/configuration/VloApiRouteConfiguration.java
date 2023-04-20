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
package eu.clarin.cmdi.vlo.api.configuration;

import eu.clarin.cmdi.vlo.api.VloFacetHandler;
import eu.clarin.cmdi.vlo.api.VloMappingHandler;
import eu.clarin.cmdi.vlo.api.VloRecordHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static eu.clarin.cmdi.vlo.util.VloApiConstants.FACETS_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.RECORDS_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.RECORDS_COUNT_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.RECORD_MAPPING_REQUEST_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.RECORD_MAPPING_RESULT_PATH;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;

/**
 * TODO: OpenAPI documentation with springdoc-openapi-webflux-core (see
 * <a href="https://medium.com/walmartglobaltech/swagger-implementation-for-webflux-functional-programming-model-8ac55bfce2be">Swagger
 * Implementation for Webflux functional programming model</a> blog post)
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Configuration
public class VloApiRouteConfiguration {

    public final static String ID_PATH_VARIABLE = "id";
    public final static String FACET_PATH_VARIABLE = "facet";

    @Bean
    public RouterFunction<ServerResponse> mappingRoute(VloMappingHandler mappingHandler) {
        return RouterFunctions
                // POST /recordMapping/request
                .route(POST(RECORD_MAPPING_REQUEST_PATH),
                        mappingHandler::requestMapping)
                // GET /recordMapping/result/{id}
                .andRoute(GET(RECORD_MAPPING_RESULT_PATH + "/{" + ID_PATH_VARIABLE + "}"),
                        mappingHandler::getMappingResult);
    }

    @Bean
    public RouterFunction<ServerResponse> recordsRoute(VloRecordHandler recordHandler) {
        return RouterFunctions
                // GET /records
                .route(GET(RECORDS_PATH),
                        recordHandler::getRecords)
                // GET /records/count
                .andRoute(GET(RECORDS_COUNT_PATH),
                        recordHandler::getRecordCount)
                // GET /records/{id}
                .andRoute(GET(RECORDS_PATH + "/{" + ID_PATH_VARIABLE + "}"),
                        recordHandler::getRecordFromRepository)
                // PUT /records
                .andRoute(PUT(RECORDS_PATH),
                        recordHandler::saveRecord);
    }

    @Bean
    public RouterFunction<ServerResponse> facetsRoute(VloFacetHandler facetHandler) {
        return RouterFunctions
                // GET /facets
                .route(GET(FACETS_PATH),
                        facetHandler::getFacets)
                // GET /facets
                .andRoute(GET(FACETS_PATH + "/{" + FACET_PATH_VARIABLE + "}"),
                        facetHandler::getFacet);
    }

    private static RequestPredicate GET(String pattern) {
        return RequestPredicates.GET(pattern).and(RequestPredicates.accept(MediaType.APPLICATION_JSON));
    }

    private static RequestPredicate POST(String pattern) {
        return RequestPredicates.POST(pattern).and(RequestPredicates.accept(MediaType.APPLICATION_JSON));
    }

    private static RequestPredicate PUT(String pattern) {
        return RequestPredicates.PUT(pattern).and(RequestPredicates.accept(MediaType.APPLICATION_JSON));
    }
}
