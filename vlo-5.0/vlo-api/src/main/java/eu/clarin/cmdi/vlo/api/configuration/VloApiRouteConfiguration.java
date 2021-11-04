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

import eu.clarin.cmdi.vlo.api.VloMappingHandler;
import eu.clarin.cmdi.vlo.api.VloRecordHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static eu.clarin.cmdi.vlo.util.VloApiConstants.RECORDS_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.RECORDS_COUNT_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.RECORD_MAPPING_REQUEST_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.RECORD_MAPPING_RESULT_PATH;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Configuration
public class VloApiRouteConfiguration {

    public final static String ID_PATH_VARIABLE = "id";

    @Bean
    public RouterFunction<ServerResponse> route(VloMappingHandler mappingHandler, VloRecordHandler recordHandler) {
        return RouterFunctions
                // POST /recordMapping/request
                .route(POST(RECORD_MAPPING_REQUEST_PATH).and(accept(MediaType.APPLICATION_JSON)), mappingHandler::requestMapping)
                // GET /recordMapping/result/{id}
                .andRoute(GET(RECORD_MAPPING_RESULT_PATH + "/{" + ID_PATH_VARIABLE + "}").and(accept(MediaType.APPLICATION_JSON)), mappingHandler::getMappingResult)
                // GET /records
                .andRoute(GET(RECORDS_PATH).and(accept(MediaType.APPLICATION_JSON)), recordHandler::getRecords)
                // GET /records/count
                .andRoute(GET(RECORDS_COUNT_PATH).and(accept(MediaType.APPLICATION_JSON)), recordHandler::getRecordCount)
                // GET /records/{id}
                .andRoute(GET(RECORDS_PATH + "/{" + ID_PATH_VARIABLE + "}").and(accept(MediaType.APPLICATION_JSON)), recordHandler::getRecordFromRepository)
                // PUT /records
                .andRoute(PUT(RECORDS_PATH).and(accept(MediaType.APPLICATION_JSON)), recordHandler::saveRecord);
    }
}
