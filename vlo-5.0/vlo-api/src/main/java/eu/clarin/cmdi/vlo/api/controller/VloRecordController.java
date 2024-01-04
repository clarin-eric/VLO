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
import eu.clarin.cmdi.vlo.api.service.impl.solr.SolrService;
import eu.clarin.cmdi.vlo.data.model.VloRecordSearchResult;
import java.util.Collections;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author twagoo
 */
@RestController
@RequestMapping("/records")
public class VloRecordController {

    public VloRecordController(SolrService service) {
        this.service = service;
    }

    private final SolrService service;

    @GetMapping
    public VloRecordSearchResult records() {
        VloRecordsRequest request = new VloRecordsRequest("*:*", Collections.emptyMap(), 0, 10);
        return service.getRecords(request);
    }

}
