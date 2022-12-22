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
package eu.clarin.cmdi.vlo.mapping.definition;

import eu.clarin.cmdi.vlo.mapping.definition.rules.assertions.ContextAssertionBasedRule;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import eu.clarin.cmdi.vlo.mapping.definition.rules.transformation.Transformer;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.stream.Stream;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@XmlTransient
@XmlSeeAlso(ContextAssertionBasedRule.class)
public abstract class MappingRule {

    public abstract boolean applies(ValueContext context);

    public abstract boolean isTerminal();

    public abstract Stream<Transformer> getTransformerStream();
    
    public abstract int getScore();

}
