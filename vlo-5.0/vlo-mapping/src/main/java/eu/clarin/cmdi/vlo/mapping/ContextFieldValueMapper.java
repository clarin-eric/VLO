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
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import java.util.stream.Stream;

/**
 * Responsible for mapping value contexts from metadata records to field value
 * candidates (subject to final processing by a {@link FieldValuesProcessor}
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public interface ContextFieldValueMapper {

    Stream<FieldMappingResult> mapContext(ValueContext context);
}
