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
import eu.clarin.cmdi.vlo.data.model.Facet;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordCountResult;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingProcessingTicket;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingRequest;
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
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.core.fn.builders.securityrequirement.Builder.securityRequirementBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

/**
 *
 * This class defines the routes for the API, and specifies the OpenAPI
 * documentation
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Configuration
public class VloApiRouteConfiguration {

    public final static String ID_PATH_VARIABLE = "id";
    public final static String FACET_PATH_VARIABLE = "facet";

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
                                .security(securityRequirementBuilder().name("basicAuth"))
                                .requestBody(requestBodyBuilder()
                                        .content(contentBuilder()
                                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                                .schema(schemaBuilder().implementation(VloRecord.class))))
                                .response(responseBuilder().responseCode("201")
                                        .description("Record created"))
                                .response(responseBuilder().responseCode("400")
                                        .description("Empty or invalid request body"))
                                .response(responseBuilder().responseCode("401")
                                        .description("Not logged in with valid credentials"))
                                .response(responseBuilder().responseCode("403")
                                        .description("The authenticated user does not have the right to save new records"))
                ).build());
    }

    @Bean
    public RouterFunction<ServerResponse> facetsRoute(VloFacetHandler facetHandler) {
        return route()
                // GET /facets
                .GET(FACETS_PATH, facetHandler::getFacets,
                        ops -> ops
                                .operationId("getFacets")
                                .tag("Facets")
                                .description("Get the facets and their (top) values and their counts")
                                .parameter(recordQueryParamBuilder())
                                .parameter(recordFilterParamBuilder())
                                .response(responseBuilder().responseCode("200")
                                        .description("Facets found")
                                        .content(contentBuilder()
                                                .array(arraySchemaBuilder()
                                                        .schema(schemaBuilder().implementation(Facet.class))
                                                )
                                                .mediaType(MediaType.APPLICATION_JSON_VALUE)))
                ).build()
                // GET /facets/{facet}
                .and(route().GET(FACETS_PATH + "/{" + FACET_PATH_VARIABLE + "}", facetHandler::getFacet,
                        ops -> ops
                                .operationId("getFacet")
                                .tag("Facets")
                                .description("Get an individual facet and its (top) values and its counts")
                                .parameter(parameterBuilder().in(ParameterIn.PATH).name(FACET_PATH_VARIABLE))
                                .parameter(recordQueryParamBuilder())
                                .parameter(recordFilterParamBuilder())
                                .response(responseBuilder().responseCode("200")
                                        .description("Facet found")
                                        .content(contentBuilder()
                                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                                .schema(schemaBuilder().implementation(Facet.class))))
                                .response(responseBuilder().responseCode("404")
                                        .description("Facet not found"))
                ).build());
    }

    @Bean
    public RouterFunction<ServerResponse> mappingRoute(VloMappingHandler mappingHandler) {
        // POST /recordMapping/request
        return route()
                .POST(RECORD_MAPPING_REQUEST_PATH, mappingHandler::requestMapping,
                        ops -> ops
                                .operationId("submitMappingRequest")
                                .tag("Mapping")
                                .description("Submit a new mapping request")
                                .security(securityRequirementBuilder().name("basicAuth"))
                                .requestBody(requestBodyBuilder()
                                        .content(contentBuilder()
                                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                                .schema(schemaBuilder().implementation(VloRecordMappingRequest.class))))
                                .response(responseBuilder().responseCode("200")
                                        .description("Request accepted, ticket created")
                                        .content(contentBuilder()
                                                .schema(schemaBuilder()
                                                        .implementation(VloRecordMappingProcessingTicket.class)))
                                )
                                .response(responseBuilder().responseCode("400")
                                        .description("Empty or invalid request body"))
                                .response(responseBuilder().responseCode("401")
                                        .description("Not logged in with valid credentials"))
                                .response(responseBuilder().responseCode("403")
                                        .description("The authenticated user does not have the right to save new records"))
                ).build()
                // GET /recordMapping/result/{id}
                .and(route().GET(RECORD_MAPPING_RESULT_PATH + "/{" + ID_PATH_VARIABLE + "}", mappingHandler::getMappingResult,
                        ops -> ops
                                .operationId("getMappingResult")
                                .tag("Mapping")
                                .description("Get results for mapping request")
                                .parameter(parameterBuilder().in(ParameterIn.PATH)
                                        .name(ID_PATH_VARIABLE)
                                        .description("Mapping request identifier from the ticket obtained on request creation"))
                                .response(responseBuilder().responseCode("200")
                                        .description("Record found for mapping request")
                                        .content(contentBuilder()
                                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                                .schema(schemaBuilder().implementation(VloRecord.class))))
                ).build());
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
