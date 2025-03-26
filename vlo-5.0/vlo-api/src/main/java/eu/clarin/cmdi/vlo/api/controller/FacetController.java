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
import eu.clarin.cmdi.vlo.api.service.VloFacetService;
import eu.clarin.cmdi.vlo.data.model.Facet;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.FACETS_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.FILTER_QUERY_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.QUERY_PARAMETER;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Offers information on the VLO's index contents in facet form.
 *
 * @author twagoo
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(FACETS_PATH)
@Tag(name = "Facets", description = "Searching, retrieving and submitting of VLO records")
public class FacetController {

    private final VloFacetService service;
    private final FilterMapFactory filterMapFactory;

    /**
     * *
     * GET /facets
     *
     * @param query
     * @param fq
     * @return
     */
    @Operation(summary = "Get the facets and their (top) values and their counts")
    @GetMapping(produces = "application/json")
    public List<Facet> getFacets(@RequestParam(required = false, defaultValue = "*:*", name = QUERY_PARAMETER) String query,
            @RequestParam(required = false, name = FILTER_QUERY_PARAMETER) List<String> fq) {
        return service.getFacets(new VloRequest(query, filterMapFactory.createFilterMap(fq)));
    }

    /**
     * *
     * GET /facet/{facetName}
     *
     * @param facetName
     * @param query
     * @param fq
     * @return
     */
    @Operation(summary = "Get the facets and their (top) values and their counts")
    @GetMapping(path = "/{facetName}", produces = "application/json")
    public ResponseEntity<Facet> getFacet(@PathVariable("facetName") String facetName, @RequestParam(required = false, defaultValue = "*:*", name = QUERY_PARAMETER) String query,
            @RequestParam(required = false, name = FILTER_QUERY_PARAMETER) List<String> fq) {
        return service.getFacet(facetName, new VloRequest(query, filterMapFactory.createFilterMap(fq)))
                .map(facet -> ResponseEntity.ok(facet))
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

}
