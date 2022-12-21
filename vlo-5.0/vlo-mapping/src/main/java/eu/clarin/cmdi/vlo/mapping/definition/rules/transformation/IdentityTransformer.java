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
package eu.clarin.cmdi.vlo.mapping.definition.rules.transformation;

import com.google.common.collect.Streams;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@NoArgsConstructor
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@ToString
public class IdentityTransformer extends BaseTransformer {

    public IdentityTransformer(String field) {
        super(field);
    }

    @Override
    public Stream<ValueLanguagePair> apply(ValueContext valueContext, VloMappingConfiguration mappingConfig) {
        return Streams.stream(valueContext.getValues());
    }

}
