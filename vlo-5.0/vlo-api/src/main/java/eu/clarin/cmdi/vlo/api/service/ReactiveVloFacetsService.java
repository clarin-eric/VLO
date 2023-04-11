/*
 * Copyright (C) 2023 twagoo
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
package eu.clarin.cmdi.vlo.api.service;

import eu.clarin.cmdi.vlo.data.model.Facet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author twagoo
 */
public interface ReactiveVloFacetsService {
    
    public Flux<Facet> getAllFacets(String query, Map<String, ? extends Iterable<String>> filters, Optional<List<String>> facets, Optional<Integer> valueCount);

    public Mono<Facet> getFacet(String facet, String query, Map<String, ? extends Iterable<String>> filters, Optional<Integer> valueCount);
    
}