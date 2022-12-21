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
package eu.clarin.cmdi.vlo.mapping.definition.rules.assertion;

import eu.clarin.cmdi.vlo.mapping.definition.rules.assertions.XPathAssertion;
import eu.clarin.cmdi.vlo.mapping.model.SimpleValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import eu.clarin.cmdi.vlo.mapping.model.XPathAware;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class XPathAssertionTest {

    private static final String XPATH = "/cmd:CMD/cmd:Header/cmd:MdCollectionDisplayName/text()";
    private final ValueContext context = SimpleValueContext.builder()
            .xpath(XPATH)
            .build();

    //TODO: test XPaths with same canonical version!!
    /**
     * Test of evaluate method, of class XPathAssertion.
     */
    @Test
    public void testEvaluateCanonicalPathMatch() {
        XPathAssertion instance = new XPathAssertion(XPATH);
        assertTrue(instance.evaluate(context));
    }

    /**
     * Test of evaluate method, of class XPathAssertion.
     */
    @Test
    public void testEvaluateCanonicalPathMismatch() {
        XPathAssertion instance = new XPathAssertion("/foo/bar");
        assertFalse(instance.evaluate(context));
    }

    /**
     * Test of evaluate method, of class XPathAssertion.
     */
    @Test
    public void testEvaluateXPathAwareMatch() {
        final XPathAwareContextMock xPathAwareContext = new XPathAwareContextMock(true);

        final XPathAssertion instance = new XPathAssertion("/foo/bar");

        assertTrue(instance.evaluate(xPathAwareContext));
    }

    /**
     * Test of evaluate method, of class XPathAssertion.
     */
    @Test
    public void testEvaluateXPathAwareMismatch() {
        final XPathAwareContextMock xPathAwareContext = new XPathAwareContextMock(false);

        final XPathAssertion instance = new XPathAssertion("/foo/bar");

        assertFalse(instance.evaluate(xPathAwareContext));
    }

    /**
     * Test of evaluate method, of class XPathAssertion.
     */
    @Test
    public void testEvaluateXPathAwareIgnored() {
        final XPathAwareContextMock xPathAwareContext = new XPathAwareContextMock(false, XPATH);

        final XPathAssertion instance = new XPathAssertion(XPATH);

        assertTrue(instance.evaluate(xPathAwareContext));
    }

    private class XPathAwareContextMock extends SimpleValueContext implements XPathAware {

        private final boolean matches;

        public XPathAwareContextMock(boolean matches) {
            super(null, null, null, null);
            this.matches = matches;
        }

        public XPathAwareContextMock(boolean matches, String xpath) {
            super(xpath, null, null, null);
            this.matches = matches;
        }

        @Override
        public boolean matchesXPath(String xPath) {
            return matches;
        }

    }

}
