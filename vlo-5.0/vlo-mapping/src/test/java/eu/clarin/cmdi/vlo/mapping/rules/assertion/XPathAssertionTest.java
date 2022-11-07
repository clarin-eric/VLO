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
package eu.clarin.cmdi.vlo.mapping.rules.assertion;

import eu.clarin.cmdi.vlo.mapping.rules.assertions.XPathAssertion;
import eu.clarin.cmdi.vlo.mapping.model.SimpleValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class XPathAssertionTest {

    private final ValueContext context = SimpleValueContext.builder()
            .xpath("/cmd:CMD/cmd:Header/cmd:MdCollectionDisplayName/text()")
            .build();

    //TODO: test XPaths with same canonical version!!

    /**
     * Test of evaluate method, of class XPathAssertion.
     */
    @Test
    public void testEvaluateTrue() {
        System.out.println("evaluate");
        final String target = "/cmd:CMD/cmd:Header/cmd:MdCollectionDisplayName/text()";
        XPathAssertion instance = new XPathAssertion(target);
        assertTrue(instance.evaluate(context));
    }
    /**
     * Test of evaluate method, of class XPathAssertion.
     */
    @Test
    public void testEvaluateFalse() {
        System.out.println("evaluate");
        final String target = "/cmd:CMD/cmd:Header/cmd:SomethingElse/text()";
        XPathAssertion instance = new XPathAssertion(target);
        assertFalse(instance.evaluate(context));
    }

}
