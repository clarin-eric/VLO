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
import eu.clarin.cmdi.vlo.mapping.processing.Transformation;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@AllArgsConstructor
public class ContextAssertionBasedRule implements MappingRule {

    private final List<? extends ContextAssertion> assertions;
    private final Collection<Transformation> transformations;
    private final Boolean terminal;

    @Override
    public boolean applies(ValueContext context) {
        return assertions.stream().anyMatch(a -> a.evaluate(context));
    }

    @Override
    public boolean isTerminal() {
        return terminal;
    }

    @Override
    public Stream<Transformation> getTransformations() {
        return transformations.stream();
    }

}
