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
package eu.clarin.cmdi.vlo.mapping.model;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Getter
@AllArgsConstructor
public class FieldMappingResult {

    private final String field;
    private final ValueContext context;
    private final Collection<ValueLanguagePair> values;
    private final int score;

    public FieldMappingResult(String field, ValueContext context, Collection<ValueLanguagePair> valueLanguagePair) {
        this(field, context, valueLanguagePair, 0);
    }

}
