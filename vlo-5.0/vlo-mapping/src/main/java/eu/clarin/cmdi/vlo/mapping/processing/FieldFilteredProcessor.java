/*
 * Copyright (C) 2022 twagoo
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

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import eu.clarin.cmdi.vlo.mapping.model.FieldMappingResult;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author twagoo
 */
@Getter
@Setter
public abstract class FieldFilteredProcessor extends FieldValuesProcessor {

    @XmlElement
    private String fields;

    @Override
    public Optional<Map<String, Collection<ValueLanguagePair>>> process(Map<String, List<FieldMappingResult>> resultsByField) {
        if (fields.isBlank()) {
            return Optional.empty();
        }

        final List<String> matchedFields = Splitter.on(CharMatcher.whitespace())
                .splitToList(this.fields);

        if (matchedFields.isEmpty()) {
            return Optional.empty();
        }

        final boolean matchAll = matchedFields.contains("*");
        return Optional.of(Maps.transformEntries(resultsByField, (field, results) -> {
            if (matchAll || matchedFields.contains(field)) {
                return processForMatchedField(results);
            } else {
                //unmatched fields are not processed
                return IdentityProcessor.mappingResultsToValues(results);
            }
        }));
    }

    protected abstract Collection<ValueLanguagePair> processForMatchedField(List<FieldMappingResult> results);

}
