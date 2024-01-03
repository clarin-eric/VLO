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
package eu.clarin.cmdi.vlo.api.service.impl;

import eu.clarin.cmdi.vlo.api.service.FieldValueLabelService;
import java.util.Objects;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author twagoo
 */
@AllArgsConstructor
@Getter
@Slf4j
public class FieldSpecificValueLabelServiceImpl implements FieldValueLabelService {
    
    private final String field;
    private final Function<String, String> labelFunction;

    /**
     *
     * @param requestedField should match field of this service
     * @param value value to get label for
     * @return label provided by the function for the value; or null if field
     * does not match
     */
    @Override
    public String getLabelFor(String requestedField, String value) {
        if (Objects.equals(field, requestedField)) {
            return labelFunction.apply(value);
        } else {
            log.warn("Field mismatch: expected {} but got {}", field, requestedField);
            return null;
        }
    }
    
}
