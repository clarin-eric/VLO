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
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement
public class ContextAssertionAndOperator extends ContextAssertion {

    @XmlElement(name = "assertion")
    private Collection<ContextAssertion> assertions;

    public ContextAssertionAndOperator(ContextAssertion... assertions) {
        this(Arrays.asList(assertions));
    }

    @Override
    public Boolean evaluate(ValueContext context) {
        return assertions.stream().allMatch(a -> a.evaluate(context));
    }

    protected Collection<ContextAssertion> getAssertions() {
        return assertions;
    }

}
