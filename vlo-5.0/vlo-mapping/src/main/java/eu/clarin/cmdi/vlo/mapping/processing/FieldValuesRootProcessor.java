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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import eu.clarin.cmdi.vlo.mapping.model.FieldMappingResult;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Root processor that produces the final field values map based on the result
 * of context - field value mapping.
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FieldValuesRootProcessor extends FieldValuesProcessor {

    @XmlElement(name = "processor")
    @Getter
    @Setter
    private List<? extends FieldValuesProcessor> processors;

    public FieldValuesRootProcessor() {
        this(new ArrayList<>());
    }

    public FieldValuesRootProcessor(List<? extends FieldValuesProcessor> processors) {
        this.processors = processors;
    }

    @Override
    public Optional<Map<String, Collection<ValueLanguagePair>>> process(Map<String, List<FieldMappingResult>> input) {
        Map<String, List<FieldMappingResult>> currentInput = input;
        Optional<Map<String, Collection<ValueLanguagePair>>> intermediateResult = Optional.empty();

        for (FieldValuesProcessor processor : processors) {
            currentInput = intermediateResult
                    .map(this::valuesToMappingResult)
                    .orElse(currentInput);

            final Optional<Map<String, Collection<ValueLanguagePair>>> previousResult = intermediateResult;
            intermediateResult = processor.process(currentInput).or(() -> previousResult);
        }
        return intermediateResult;
    }

    private Map<String, List<FieldMappingResult>> valuesToMappingResult(Map<String, Collection<ValueLanguagePair>> valuesMap) {

        return ImmutableMap.copyOf(
                Maps.transformEntries(
                        valuesMap,
                        (k, v) -> ImmutableList.of(new FieldMappingResult(k, null, v))));
    }

}
