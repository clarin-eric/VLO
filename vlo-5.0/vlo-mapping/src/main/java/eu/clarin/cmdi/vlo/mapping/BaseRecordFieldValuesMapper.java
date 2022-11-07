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

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import eu.clarin.cmdi.vlo.mapping.model.FieldMappingResult;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * Maps a full record to field values
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class BaseRecordFieldValuesMapper implements RecordFieldValuesMapper {

    private final ContextFactory contextFactory;
    private final ContextFieldValueMapper contextFieldValueMapper;
    private final FieldValuesProcessor fieldValuesProcessor;

    public BaseRecordFieldValuesMapper(ContextFactory contextFactory, ContextFieldValueMapper contextFieldValueMapper, FieldValuesProcessor fieldValuesProcessor) {
        this.contextFactory = contextFactory;
        this.contextFieldValueMapper = contextFieldValueMapper;
        this.fieldValuesProcessor = fieldValuesProcessor;
    }

    @Override
    public Map<String, Collection<ValueLanguagePair>> mapRecordToFields(File recordFile) throws IOException, VloMappingException {
        log.info("Field mapping of record ({})", recordFile);

        log.debug("Mapping all contexts (record {})", recordFile);
        // Produce mapping results for all individual contexts
        final Map<String, List<FieldMappingResult>> resultsByField = mapAllContexts(recordFile);

        log.debug("Producing field values (record {})", recordFile);
        // Distil field values out of mapping results
        return produceFieldValues(resultsByField);
    }

    /**
     * Produces mapping results for all individual contexts in the record
     *
     * @param recordFile
     * @return
     */
    private Map<String, List<FieldMappingResult>> mapAllContexts(File recordFile) throws IOException, VloMappingException {
        return contextFactory.createContexts(recordFile) // gets all contexts in the record
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
            resultBuilder.put(e.getKey(), fieldValuesProcessor.process(e.getKey(), e.getValue()).toList());
        });
        return resultBuilder.buildOrThrow();
    }

}
