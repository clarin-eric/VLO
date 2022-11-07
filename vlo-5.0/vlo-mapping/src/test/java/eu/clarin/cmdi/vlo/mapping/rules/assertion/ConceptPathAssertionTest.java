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

import eu.clarin.cmdi.vlo.mapping.rules.assertions.ConceptPathAssertion;
import eu.clarin.cmdi.vlo.mapping.model.SimpleValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import java.util.Arrays;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class ConceptPathAssertionTest {

    private final ValueContext context = SimpleValueContext.builder()
            .conceptPath(Arrays.asList("concept1", "concept2", null, "concept3"))
            .build();

    /**
     * Test of evaluate method, of class ConceptPathAssertion.
     */
    @Test
    public void testEvaluateMisMatchNonPartial() {
        {
            ConceptPathAssertion instance = new ConceptPathAssertion("concept4");
            assertFalse(instance.evaluate(context), "Mismatch with non-partial match");
        }
        {
            ConceptPathAssertion instance = new ConceptPathAssertion("concept1");
            assertFalse(instance.evaluate(context), "Mismatch with non-partial match (single matching concept)");
        }
        {
            ConceptPathAssertion instance = new ConceptPathAssertion("concept1", "concept2", "concept3", "concept4");
            assertFalse(instance.evaluate(context), "Mismatch with non-partial match (target too deep)");
        }
    }

    @Test
    public void testEvaluateMisMatchPartial() {
        {
            ConceptPathAssertion instance = new ConceptPathAssertion("concept4", "*");
            assertFalse(instance.evaluate(context), "Mismatch with partial match (single concept)");
        }
        {
            ConceptPathAssertion instance = new ConceptPathAssertion("concept2", "concept1", "*");
            assertFalse(instance.evaluate(context), "Mismatch with partial match (multiple concepts)");
        }
        {
            ConceptPathAssertion instance = new ConceptPathAssertion("concept1", "concept2", "concept3", "concept4", "*");
            assertFalse(instance.evaluate(context), "Mismatch with partial match (target too deep)");
        }
    }

    @Test
    public void testEvaluatePartialMatch() {
        {
            ConceptPathAssertion instance = new ConceptPathAssertion("concept1", "*");
            assertTrue(instance.evaluate(context), "Match with partial match (single concept)");
        }
        {
            ConceptPathAssertion instance = new ConceptPathAssertion("concept1", "concept2", "*");
            assertTrue(instance.evaluate(context), "Match with partial match (multiple concept)");
        }
        {
            ConceptPathAssertion instance = new ConceptPathAssertion("concept1", null, "concept2", null, "*");
            assertTrue(instance.evaluate(context), "Match with partial match (multiple concept)");
        }
    }

    @Test
    public void testEvaluateCompleteMatch() {
        ConceptPathAssertion instance = new ConceptPathAssertion("concept1", "concept2", "concept3");
        assertTrue(instance.evaluate(context), "Match with partial match (single concept)");
    }

    {
        ConceptPathAssertion instance = new ConceptPathAssertion("concept1", "*", "concept3", null);
        assertTrue(instance.evaluate(context), "Match with partial match (multiple concept)");
    }

    {
        ConceptPathAssertion instance = new ConceptPathAssertion("concept1", null, "concept2", null, "concept3", null);
        assertTrue(instance.evaluate(context), "Match with partial match (multiple concept)");
    }
    
    @Test
    public void testGetConceptPath() {
        ConceptPathAssertion instance = new ConceptPathAssertion("concept1", "concept2", "concept3");
        assertEquals("concept1 concept2 concept3", instance.getConceptPath());
        instance = new ConceptPathAssertion("concept1", "*");
        assertEquals("concept1 *", instance.getConceptPath());
    }
    
    @Test
    public void testSetConceptPath() {
        ConceptPathAssertion instance = new ConceptPathAssertion("concept1", "concept2", "concept3");
        instance.setConceptPath("concept1 concept2 concept3");
        assertThat(instance.getTargetPath(), hasSize(3));
        assertThat(instance.getTargetPath(), hasItems("concept1", "concept2", "concept3"));
        instance.setConceptPath("concept1   concept2  *  ");
        assertThat(instance.getTargetPath(), hasSize(3));
        assertThat(instance.getTargetPath(), hasItems("concept1", "concept2", "*"));
    }

}
