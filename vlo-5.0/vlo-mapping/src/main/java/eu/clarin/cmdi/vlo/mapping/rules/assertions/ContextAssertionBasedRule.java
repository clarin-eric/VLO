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
package eu.clarin.cmdi.vlo.mapping.rules.assertions;

import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import eu.clarin.cmdi.vlo.mapping.rules.MappingRule;
import eu.clarin.cmdi.vlo.mapping.rules.transformation.Transformer;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ContextAssertionBasedRule extends MappingRule {

    @XmlElementWrapper(name = "assertions")
    @XmlElement(name = "assertion")
    private List<? extends ContextAssertion> assertions;

    @XmlElementWrapper(name = "transformations")
    @XmlElement(name = "transformation")

    private Collection<Transformer> transformations;

    @XmlElement
    private Boolean terminal = false;

    public ContextAssertionBasedRule() {

    }

    public ContextAssertionBasedRule(List<? extends ContextAssertion> assertions, Collection<Transformer> transformations, Boolean terminal) {
        this.assertions = assertions;
        this.transformations = transformations;
        this.terminal = terminal;
    }

    @Override
    public boolean applies(ValueContext context) {
        return assertions.stream().anyMatch(a -> a.evaluate(context));
    }

    @Override
    public boolean isTerminal() {
        return terminal;
    }

    @Override
    public Stream<Transformer> getTransformations() {
        return transformations.stream();
    }

    public List<? extends ContextAssertion> getAssertions() {
        return assertions;
    }

}
