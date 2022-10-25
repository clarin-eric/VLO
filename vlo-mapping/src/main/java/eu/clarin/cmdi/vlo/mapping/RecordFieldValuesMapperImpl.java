/*
 * Copyright (C) 2022 CLARIN
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
package eu.clarin.cmdi.vlo.mapping;

import com.google.common.collect.ImmutableMap;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.vlo.mapping.model.FieldMappingResult;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps a full record to field values
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class RecordFieldValuesMapperImpl implements RecordFieldValuesMapper {

    private ContextFactory contextFactory;
    private ContextFieldValueMapper contextFieldValueMapper;
    private FieldValuesProcessor fieldValuesProcessor;

    @Override
    public Map<String, Collection<ValueLanguagePair>> mapRecordToFields(VTDNav nav) {
        // Produce mapping results for all individual contexts
        final Map<String, List<FieldMappingResult>> resultsByField = mapAllContexts(nav);

        // Distil field values out of mapping results
        return produceFieldValues(resultsByField);
    }

    /**
     * Produces mapping results for all individual contexts in the record
     *
     * @param nav
     * @return
     */
    private Map<String, List<FieldMappingResult>> mapAllContexts(VTDNav nav) {
        return contextFactory.createContexts(nav) // gets all contexts in the record
                .flatMap(contextFieldValueMapper::mapContext) // maps all contexts to field value candidates
                .collect(Collectors.groupingBy(FieldMappingResult::getField)); // collects results grouped by field
    }

    /**
     * Distils field values out of mapping results
     *
     * @param resultsByField
     * @return
     */
    private Map<String, Collection<ValueLanguagePair>> produceFieldValues(final Map<String, List<FieldMappingResult>> resultsByField) {
        final ImmutableMap.Builder<String, Collection<ValueLanguagePair>> resultBuilder
                = ImmutableMap.builder();
        resultsByField.entrySet().forEach(e -> {
            // field specific processing of all individual mapping results into field values
            resultBuilder.put(e.getKey(), fieldValuesProcessor.process(e.getKey(), e.getValue()));
        });
        return resultBuilder.buildOrThrow();
    }

}
