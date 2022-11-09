/*
 * Copyright (C) 2022 CLARIN ERIC <clarin@clarin.eu>
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
package eu.clarin.cmdi.vlo.mapping.processing;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import eu.clarin.cmdi.vlo.mapping.model.FieldMappingResult;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Root processor that produces the final field values map based on the result
 * of context - field value mapping.
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class FieldValuesRootProcessor implements FieldValuesProcessor {

    private final static IdentityProcessor identityProcessor = new IdentityProcessor();

    @Override
    public Map<String, Collection<ValueLanguagePair>> process(Map<String, List<FieldMappingResult>> resultsByField) {
        final Map<String, Collection<ValueLanguagePair>> singleFieldsResults
                = processSingleFields(resultsByField);
        //TODO: feed into global processor(s)? Integrators, extractors, summarizers...
        return singleFieldsResults;
    }

    private Map<String, Collection<ValueLanguagePair>> processSingleFields(Map<String, List<FieldMappingResult>> resultsByField) {
        return Maps.transformEntries(resultsByField, (k, v) -> processSingleField(k, v));
    }

    public Collection<ValueLanguagePair> processSingleField(String field, Iterable<FieldMappingResult> mappingResults) {
        log.debug("Processing field {} with mapping results {}", field, mappingResults);
        final ImmutableSet<ValueLanguagePair> values
                = ImmutableSet.copyOf(
                        Iterables.concat(
                                Iterables.transform(
                                        mappingResults,
                                        FieldMappingResult::getValues)));

        //TODO: apply processors from field-processor map (fall back to identity)
        return identityProcessor.process(field, values).toList();
    }

}
