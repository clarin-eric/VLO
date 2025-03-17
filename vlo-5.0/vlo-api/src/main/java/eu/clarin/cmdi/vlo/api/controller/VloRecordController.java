/*
 * Copyright (C) 2024 twagoo
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
package eu.clarin.cmdi.vlo.api.controller;

import eu.clarin.cmdi.vlo.api.model.VloRequest;
import eu.clarin.cmdi.vlo.api.service.FilterMapFactory;
import eu.clarin.cmdi.vlo.api.service.VloRecordService;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordSearchResult;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.COUNT_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.FILTER_QUERY_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.FROM_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.QUERY_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.RECORDS_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.ROWS_PARAMETER;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves and accepts VLO records. The main controller for search operations,
 * retrieval of full record content, and ingestion of new records into the
 * store.
 *
 * @author twagoo
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(RECORDS_PATH)
@Tag(name = "Records", description = "Searching, retrieving and submitting of VLO records")
public class VloRecordController {

    private final VloRecordService service;
    private final FilterMapFactory filterMapFactory;

    /**
     * *
     * GET /records
     *
     * @param query
     * @param fq
     * @param from
     * @param size
     * @return
     */
    @Operation(summary = "Retrieve one or more records by query and/or filtered")
    @GetMapping(produces = "application/json")
    public VloRecordSearchResult getRecords(@RequestParam(required = false, defaultValue = "*:*", name = QUERY_PARAMETER) String query,
            @RequestParam(required = false, name = FILTER_QUERY_PARAMETER) List<String> fq,
            @RequestParam(required = false, defaultValue = "0", name = FROM_PARAMETER) Integer from,
            @RequestParam(required = false, defaultValue = "10", name = ROWS_PARAMETER) Integer size) {
        return service.getRecords(new VloRequest(query, filterMapFactory.createFilterMap(fq), from, size));
    }

    /**
     * *
     * GET /records/count
     *
     * @param query
     * @param fq
     * @return
     */
    @Operation(summary = "Retrieve the exact number of records that can be retrieved by query and/or filtered as JSON object with a single property 'numFound'")
    @GetMapping(path = COUNT_PATH, produces = "application/json")
    public Map<String, Object> getRecordsCount(@RequestParam(required = false, defaultValue = "*:*") String query,
            @RequestParam(required = false, name = FILTER_QUERY_PARAMETER) List<String> fq) {
        return Collections.singletonMap("numFound", service.getRecordCount(query, filterMapFactory.createFilterMap(fq)));
    }

    /**
     * *
     * GET /records/{id}
     *
     * @param id
     * @return
     */
    @Operation(summary = "Retrieve an individual record by its identifier")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "A record with the specified identifier that is present in the catalogue"),
        @ApiResponse(responseCode = "404", description = "No record with the specified identifier is present in the catalogue", useReturnTypeSchema = false)
    })
    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<VloRecord> getRecord(@PathVariable String id) {
        return service.getRecordById(id)
                .map((record) -> ResponseEntity.ok().body(record)
                ).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * *
     * PUT /records
     *
     * @param record
     * @return
     */
    @Operation(summary = "Submit a new record")
    @PostMapping(consumes = "application/json", produces = "application/json")
    public VloRecord saveRecord(@RequestBody VloRecord record) {
        return service.saveRecord(record)
                .orElseThrow(() -> new VloApiProcessingException(
                "Unexpectedly failed to save record. Service did not return VloRecord upon save request.",
                record));
    }

}
