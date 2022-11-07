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
package eu.clarin.cmdi.vlo.mapping;

import com.google.common.collect.Streams;
import eu.clarin.cmdi.vlo.mapping.model.FieldMappingResult;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class FieldValuesProcessorImpl implements FieldValuesProcessor {

    private final static IdentityProcessor identityProcessor = new IdentityProcessor();

    @Override
    public Stream<ValueLanguagePair> process(String field, Iterable<FieldMappingResult> mappingResults) {
        log.info("Processing field {} with mapping results {}", field, mappingResults);
        return identityProcessor.process(field, mappingResults);
    }

    public static class IdentityProcessor implements FieldValuesProcessor {

        @Override
        public Stream<ValueLanguagePair> process(String field, Iterable<FieldMappingResult> mappingResults) {
            return Streams.stream(mappingResults)
                    .map(FieldMappingResult::getValues) // get the values from the result
                    .flatMap(Collection::stream); // join them in the result stream
        }

    }

}
