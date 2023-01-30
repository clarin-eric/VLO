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

import eu.clarin.cmdi.vlo.mapping.model.FieldMappingResult;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import eu.clarin.cmdi.vlo.mapping.processing.FieldValuesProcessor;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.transform.stream.StreamSource;
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
    public Map<String, Collection<ValueLanguagePair>> mapRecordToFields(StreamSource source) throws IOException, VloMappingException {
        log.debug("Field mapping of record ({})", source.getSystemId());

        log.debug("Mapping all contexts (record {})", source.getSystemId());
        // Produce mapping results for all individual contexts
        final Map<String, List<FieldMappingResult>> resultsByField = mapAllContexts(source);

        log.debug("Producing field values (record {})", source.getSystemId());
        // Distil field values out of mapping results
        return fieldValuesProcessor.process(resultsByField).orElseGet(() -> {
            log.warn("Field values processor returns empty for {}. No fields produced!", source.getSystemId());
            return Collections.emptyMap();
        });
    }

    /**
     * Produces mapping results for all individual contexts in the record
     *
     * @param recordFile
     * @return
     */
    private Map<String, List<FieldMappingResult>> mapAllContexts(StreamSource source) throws IOException, VloMappingException {
        return contextFactory.createContexts(source) // gets all contexts in the record
                .flatMap(contextFieldValueMapper::mapContext) // maps all contexts to field value candidates
                .collect(Collectors.groupingBy(FieldMappingResult::getField)); // collects results grouped by field
    }

}
