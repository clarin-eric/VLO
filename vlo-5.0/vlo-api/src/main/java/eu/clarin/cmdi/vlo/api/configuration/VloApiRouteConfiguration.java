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
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordCountResult;
import eu.clarin.cmdi.vlo.data.model.VloRecordSearchResult;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.FACETS_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.FILTER_QUERY_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.FROM_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.QUERY_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.RECORDS_COUNT_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.RECORDS_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.RECORD_MAPPING_REQUEST_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.RECORD_MAPPING_RESULT_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.ROWS_PARAMETER;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

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
        return route()
                // GET /records
                .GET(RECORDS_PATH, recordHandler::getRecords,
                        ops -> ops
                                .operationId("getRecords")
                                .tag("Records")
                                .description("Get all records or a specific subset")
                                .parameter(recordQueryParamBuilder())
                                .parameter(recordFilterParamBuilder())
                                .parameter(parameterBuilder().name(FROM_PARAMETER)
                                        .description("Record offset (zero-based index)")
                                        .example("0"))
                                .parameter(parameterBuilder().name(ROWS_PARAMETER).description("Maximum number of rows to retrieve")
                                        .example("10"))
                                .response(responseBuilder().responseCode("200")
                                        .description("Records found")
                                        .content(contentBuilder()
                                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                                .schema(schemaBuilder().implementation(VloRecordSearchResult.class))))
                ).build()
                // GET /records/count
                .and(route().GET(RECORDS_COUNT_PATH, recordHandler::getRecordCount,
                        ops -> ops
                                .operationId("countRecords")
                                .tag("Records")
                                .description("Get the number of all records or a specific subset")
                                .parameter(recordQueryParamBuilder())
                                .parameter(recordFilterParamBuilder())
                                .response(responseBuilder().responseCode("200")
                                        .description("Records found")
                                        .content(contentBuilder()
                                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                                .schema(schemaBuilder().implementation(VloRecordCountResult.class))))
                ).build())
                // GET /records/{id}
                .and(route().GET(RECORDS_PATH + "/{" + ID_PATH_VARIABLE + "}", recordHandler::getRecordFromRepository,
                        ops -> ops
                                .operationId("getRecord")
                                .tag("Records")
                                .description("Get an individual record")
                                .parameter(parameterBuilder().in(ParameterIn.PATH).name(ID_PATH_VARIABLE))
                                .response(responseBuilder().responseCode("200")
                                        .description("Record found")
                                        .content(contentBuilder()
                                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                                .schema(schemaBuilder().implementation(VloRecord.class))))
                                .response(responseBuilder().responseCode("404")
                                        .description("Record not found"))
                ).build())
                // PUT /records
                .and(route().POST(RECORDS_PATH, recordHandler::saveRecord,
                        ops -> ops
                                .operationId("saveRecord")
                                .tag("Records")
                                .description("Save a new record")
                                .requestBody(requestBodyBuilder()
                                        .content(contentBuilder()
                                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                                .schema(schemaBuilder().implementation(VloRecord.class))))
                                .response(responseBuilder().responseCode("201")
                                        .description("Record created"))
                                .response(responseBuilder().responseCode("400")
                                        .description("Empty or invalid request body"))
                ).build());
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

    private static org.springdoc.core.fn.builders.parameter.Builder recordQueryParamBuilder() {
        return parameterBuilder().name(QUERY_PARAMETER)
                .description("Query")
                .example("*");
    }

    private static org.springdoc.core.fn.builders.parameter.Builder recordFilterParamBuilder() {
        return parameterBuilder().name(FILTER_QUERY_PARAMETER)
                .description("Filter query")
                .array(arraySchemaBuilder().minItems(1));
    }
}
