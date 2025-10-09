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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import eu.clarin.cmdi.vlo.api.model.VloRequest;
import eu.clarin.cmdi.vlo.api.service.FilterMapFactory;
import eu.clarin.cmdi.vlo.api.service.VloFacetService;
import eu.clarin.cmdi.vlo.data.model.Facet;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.FACETS_PATH;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.FACET_VALUES_COUNT_LIMIT;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.FILTER_QUERY_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.QUERY_PARAMETER;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.FIELDS_SELECTION_PARAMETER;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
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

    @Configuration
    @ConfigurationProperties(prefix = "api.facets")
    @Data
    public static class FacetControllerProperties {

        private Integer defaultFacetValueCountLimit;
    }

    private final FacetControllerProperties controllerProperties;
    private final VloFacetService service;
    private final FilterMapFactory filterMapFactory;

    /**
     * *
     * GET /facets
     *
     * @param query search query
     * @param fq filter query
     * @param fields fields to include facets for
     * @param valueCountLimit maximum number of values to include per facet
     * @return
     */
    @Operation(summary = "Get the facets and their (top) values and their counts")
    @GetMapping(produces = "application/json")
    public List<Facet> getFacets(@RequestParam(required = false, defaultValue = "*:*", name = QUERY_PARAMETER) String query,
            @RequestParam(required = false, name = FILTER_QUERY_PARAMETER) List<String> fq,
            @RequestParam(required = false, name = FIELDS_SELECTION_PARAMETER) List<String> fields,
            @RequestParam(required = false, name = FACET_VALUES_COUNT_LIMIT) Optional<Integer> valueCountLimit) {
        //count limit is either given as parameter, or else the default is used
        final Integer countLimit = valueCountLimit.orElse(controllerProperties.getDefaultFacetValueCountLimit());
        //retrieve one more so that we can decide which indication to given regarding "more values"
        final List<Facet> facets = getFacetsFromService(fields, query, fq, countLimit + 1);
        //apply the count limit
        facets.forEach(facet -> applyFacetValueLimit(facet, countLimit));
        return facets;
    }

    private List<Facet> getFacetsFromService(List<String> fields, String query, List<String> fq, final int retrievalValueCount) {
        if (ObjectUtils.isEmpty(fields)) {
            return service.getFacets(new VloRequest(query, filterMapFactory.createFilterMap(fq)), retrievalValueCount);
        } else {
            return service.getFacets(new VloRequest(query, filterMapFactory.createFilterMap(fq)), fields, retrievalValueCount);
        }
    }

    private void applyFacetValueLimit(final Facet facet, final Integer countLimit) {
        if (facet.getValueCount() > countLimit) {
            //there are more values than the requested limit; set inidicator and apply limit
            facet.setHasMore(true);
            facet.setValueCount(countLimit);
            final Iterable<Facet.ValueCount> limited = Iterables.limit(facet.getValues(), countLimit);
            facet.setValues(ImmutableList.copyOf(limited));
        } else {
            //there are fewer values than the requested limit, set inidicator and leave as is
            facet.setHasMore(false);
        }
    }

    /**
     * *
     * GET /facet/{facetName}
     *
     * @param facetName
     * @param query
     * @param fq
     * @param valueCountLimit limit to value counts
     * @return
     */
    @Operation(summary = "Get the facets and their (top) values and their counts")
    @GetMapping(path = "/{facetName}", produces = "application/json")
    public ResponseEntity<Facet> getFacet(@PathVariable("facetName") String facetName, @RequestParam(required = false, defaultValue = "*:*", name = QUERY_PARAMETER) String query,
            @RequestParam(required = false, name = FILTER_QUERY_PARAMETER) List<String> fq,
            @RequestParam(required = false, name = FACET_VALUES_COUNT_LIMIT) Optional<Integer> valueCountLimit) {
        return service.getFacet(facetName, new VloRequest(query, filterMapFactory.createFilterMap(fq)))
                .map(facet -> {
                    valueCountLimit.ifPresent(
                            countLimit -> applyFacetValueLimit(facet, countLimit));
                    return ResponseEntity.ok(facet);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

}
