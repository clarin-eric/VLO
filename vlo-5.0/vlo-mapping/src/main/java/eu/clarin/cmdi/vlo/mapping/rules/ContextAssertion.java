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

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@XmlTransient
@XmlSeeAlso({ConceptPathAssertion.class})
//    ContextAssertionAndOperator.class,
//    ContextAssertionBooleanOperator.class,
//    ContextAssertionNotOperator.class})
//    ValueAssertion.class,
//    XPathAssertion.class})
public abstract class ContextAssertion {

    public abstract Boolean evaluate(ValueContext context);

    public static ContextAssertion TRUE() {
        return new ContextAssertionBooleanOperator(true);
    }

    public static ContextAssertion FALSE() {
        return new ContextAssertionBooleanOperator(false);
    }

    public static ContextAssertion NOT(ContextAssertion assertion) {
        return new ContextAssertionNotOperator(assertion);
    }

    public static ContextAssertion AND(ContextAssertion... assertions) {
        return new ContextAssertionAndOperator(ImmutableList.copyOf(assertions));
    }

}
