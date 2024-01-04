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

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import eu.clarin.cmdi.vlo.api.model.VloRecordsRequest;
import eu.clarin.cmdi.vlo.api.service.VloRecordService;
import eu.clarin.cmdi.vlo.data.model.VloRecordSearchResult;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.FILTER_QUERY_PARAMETER;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author twagoo
 */
@Slf4j
@RestController
@RequestMapping("/records")
public class VloRecordController {

    protected final Splitter FQ_SPLITTER = Splitter.on(':').limit(2);

    public VloRecordController(VloRecordService service) {
        this.service = service;
    }

    private final VloRecordService service;

    @GetMapping
    public VloRecordSearchResult records(@RequestParam(required = false, defaultValue = "*:*") String query,
            @RequestParam(required = false, defaultValue = "0") List<String> fq,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return service.getRecords(new VloRecordsRequest(query, createFilterMap(fq), from, size));
    }

    protected Map<String, ? extends Iterable<String>> createFilterMap(List<String> fq) {
        if (fq == null || fq.isEmpty()) {
            return Collections.emptyMap();
        } else {
            final ArrayListMultimap<String, String> map = ArrayListMultimap.<String, String>create();
            fq.forEach(fqVal -> {
                List<String> fqDecomposed = FQ_SPLITTER.splitToList(fqVal);
                if (fqDecomposed.size() == 2) {
                    map.put(fqDecomposed.get(0), fqDecomposed.get(1));
                } else {
                    log.warn("Ignoring invalid fq parameter: {}", fq);
                }
            });

            return map.asMap();
        }

    }

}
