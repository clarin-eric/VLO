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

import eu.clarin.cmdi.vlo.mapping.model.FieldMappingResult;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Processes mapping results into a final map of field values that actually will
 * be stored. This is to be applied after all transformations on record values
 * have been carried out. This can include processing of individual fields but
 * also implement integration and harmonisation across fields.
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@XmlTransient
@XmlSeeAlso({FieldValuesRootProcessor.class, IdentityProcessor.class, ScoreFilterProcessor.class})
public abstract class FieldValuesProcessor {

    public abstract Optional<Map<String, Collection<ValueLanguagePair>>> process(final Map<String, List<FieldMappingResult>> resultsByField);
}
