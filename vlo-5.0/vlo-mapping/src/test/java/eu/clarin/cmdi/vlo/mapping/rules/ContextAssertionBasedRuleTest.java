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
import eu.clarin.cmdi.vlo.mapping.model.SimpleValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import eu.clarin.cmdi.vlo.mapping.processing.Transformation;
import java.util.List;
import java.util.stream.Collectors;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@ExtendWith(MockitoExtension.class)
public class ContextAssertionBasedRuleTest {

    @Mock
    Transformation transformation;

    /**
     * Test of applies method, of class ContextAssertionBasedRule.
     */
    @Test
    public void testApplies() {
        final ImmutableList<ContextAssertion> assertions = ImmutableList.of(ContextAssertion.FALSE(), ContextAssertion.TRUE());
        final ImmutableList<Transformation> transformations = ImmutableList.of(transformation);

        final ContextAssertionBasedRule instance = new ContextAssertionBasedRule(assertions, transformations, false);
        final SimpleValueContext context = SimpleValueContext.builder()
                .values(ImmutableList.of(new ValueLanguagePair("value1", "en")))
                .build();
        final boolean result = instance.applies(context);
        assertTrue(result);
    }

    /**
     * Test of isTerminal method, of class ContextAssertionBasedRule.
     */
    @Test
    public void testIsTerminal() {
        final ImmutableList<ContextAssertionBooleanOperator> assertions = ImmutableList.of(new ContextAssertionBooleanOperator(false), new ContextAssertionBooleanOperator(true));
        final ImmutableList<Transformation> transformations = ImmutableList.of(transformation);

        {
            final ContextAssertionBasedRule instance = new ContextAssertionBasedRule(assertions, transformations, true);
            assertTrue(instance.isTerminal());
        }
        {
            final ContextAssertionBasedRule instance = new ContextAssertionBasedRule(assertions, transformations, false);
            assertFalse(instance.isTerminal());
        }
    }

    /**
     * Test of getTransformations method, of class ContextAssertionBasedRule.
     */
    @Test
    public void testGetTransformations() {
        final ImmutableList<ContextAssertionBooleanOperator> assertions = ImmutableList.of(new ContextAssertionBooleanOperator(false), new ContextAssertionBooleanOperator(true));
        final ImmutableList<Transformation> transformations = ImmutableList.of(transformation);

        {
            final ContextAssertionBasedRule instance = new ContextAssertionBasedRule(assertions, transformations, true);
            final List<Transformation> collected = instance.getTransformations().collect(Collectors.toList());
            assertThat(collected, hasItem(transformation));
        }
    }

}
