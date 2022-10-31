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
package eu.clarin.cmdi.vlo.mapping.rules;

import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Data
@AllArgsConstructor
public class TypedContextAssertion implements ContextAssertion {

    public enum ContextAssertionType {
        CONCEPT,
        XPATH,
        VALUE
    }

    public enum MatchType {
        EQUALS,
        PATTERN_MATCH,
        SUBPATH,
        REGEX
    }

    private ContextAssertionType type;
    private MatchType match;
    private List<String> target;

    @Override
    public Boolean evaluate(ValueContext context) {
        //TODO
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
