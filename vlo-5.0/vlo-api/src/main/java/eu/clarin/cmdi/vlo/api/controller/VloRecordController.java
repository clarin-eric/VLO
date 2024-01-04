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

import eu.clarin.cmdi.vlo.api.model.VloRecordsRequest;
import eu.clarin.cmdi.vlo.api.service.FilterMapFactory;
import eu.clarin.cmdi.vlo.api.service.VloRecordService;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordSearchResult;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.FILTER_QUERY_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.FROM_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.RECORDS_COUNT_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.RECORDS_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.ROWS_PARAMETER;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author twagoo
 */
@Slf4j
@AllArgsConstructor
@RestController
@Profile("solr")
public class VloRecordController {

    private final VloRecordService service;
    private final FilterMapFactory filterMapFactory;

    @GetMapping(path = RECORDS_PATH, produces = "application/json")
    public VloRecordSearchResult getRecords(@RequestParam(required = false, defaultValue = "*:*") String query,
            @RequestParam(required = false, name = FILTER_QUERY_PARAMETER) List<String> fq,
            @RequestParam(required = false, defaultValue = "0", name = FROM_PARAMETER) Integer from,
            @RequestParam(required = false, defaultValue = "10", name = ROWS_PARAMETER) Integer size) {
        return service.getRecords(new VloRecordsRequest(query, filterMapFactory.createFilterMap(fq), from, size));
    }

    @GetMapping(path = RECORDS_COUNT_PATH, produces = "text/plain")
    public Long recordsCount(@RequestParam(required = false, defaultValue = "*:*") String query,
            @RequestParam(required = false, name = FILTER_QUERY_PARAMETER) List<String> fq) {
        return service.getRecordCount(query, filterMapFactory.createFilterMap(fq));
    }

    @GetMapping(path = RECORDS_PATH + "/{id}", produces = "application/json")
    public VloRecord getRecord(@PathVariable String id) {
        return service.getRecordById(id).orElseThrow();
    }

    @PutMapping(path = RECORDS_PATH, consumes = "application/json", produces = "application/json")
    public VloRecord saveRecord(@RequestBody VloRecord record) {
        return service.saveRecord(record).orElseThrow();
    }

}
