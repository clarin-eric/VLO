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
package eu.clarin.cmdi.vlo.mapping.processing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.vlo.mapping.model.FieldMappingResult;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class FieldValuesRootProcessorTest {

    private static final Map<String, List<FieldMappingResult>> input = ImmutableMap.<String, List<FieldMappingResult>>builder()
            .put("field1", ImmutableList.of(
                    new FieldMappingResult("field1", null, ImmutableList.of(new ValueLanguagePair("f1v1a", null), new ValueLanguagePair("f1v1b", null))),
                    new FieldMappingResult("field1", null, ImmutableList.of(new ValueLanguagePair("f1v2a", null), new ValueLanguagePair("f1v2b", null))),
                    new FieldMappingResult("field1", null, ImmutableList.of(new ValueLanguagePair("f1v3a", null), new ValueLanguagePair("f1v3b", null)))))
            .put("field2", ImmutableList.of(
                    new FieldMappingResult("field2", null, ImmutableList.of(new ValueLanguagePair("f2v1a", null), new ValueLanguagePair("f2v1b", null))),
                    new FieldMappingResult("field2", null, ImmutableList.of(new ValueLanguagePair("f2v2a", null), new ValueLanguagePair("f2v2b", null))),
                    new FieldMappingResult("field2", null, ImmutableList.of(new ValueLanguagePair("f2v3a", null), new ValueLanguagePair("f2v3b", null)))))
            .build();

    /**
     * Test of process method, of class FieldValuesRootProcessor.
     */
    @Test
    public void testProcessEmpty() {
        final FieldValuesRootProcessor instance = new FieldValuesRootProcessor();
        Optional<Map<String, Collection<ValueLanguagePair>>> result = instance.process(input);
        assertTrue(result.isEmpty());
    }

    /**
     * Test of process method, of class FieldValuesRootProcessor.
     */
    @Test
    public void testProcessIdentity() {
        final FieldValuesRootProcessor instance = new FieldValuesRootProcessor(
                ImmutableList.of(
                        new IdentityProcessor()));
        Optional<Map<String, Collection<ValueLanguagePair>>> result = instance.process(input);
        result.ifPresentOrElse(r -> {
            assertThat(r, aMapWithSize(2));
        }, () -> {
            fail("Result expected");
        });
    }

    /**
     * Test of process method, of class FieldValuesRootProcessor.
     */
    @Test
    public void testProcessWithNoop() {
        final FieldValuesRootProcessor instance
                = new FieldValuesRootProcessor(ImmutableList.of(
                        noopProcessor(),
                        new IdentityProcessor(),
                        noopProcessor(),
                        new IdentityProcessor()));

        final Optional<Map<String, Collection<ValueLanguagePair>>> result = instance.process(input);

        result.ifPresentOrElse(r -> {
            assertThat(r, aMapWithSize(2));
        }, () -> {
            fail("Result expected");
        });
    }

    /**
     * Test of process method, of class FieldValuesRootProcessor.
     */
    @Test
    public void testProcessWithStatic() {

        final Map<String, Collection<ValueLanguagePair>> staticValue
                = ImmutableMap.<String, Collection<ValueLanguagePair>>builder()
                        .put("fieldX", ImmutableList.of(new ValueLanguagePair("val", "lang")))
                        .build();

        final FieldValuesRootProcessor instance
                = new FieldValuesRootProcessor(ImmutableList.of(
                        new IdentityProcessor(),
                        staticProcessor(staticValue),
                        new IdentityProcessor()));

        final Optional<Map<String, Collection<ValueLanguagePair>>> result = instance.process(input);

        result.ifPresentOrElse(r -> {
            assertThat(r, allOf(aMapWithSize(1), hasKey("fieldX")));
        }, () -> {
            fail("Result expected");
        });
    }

    private static FieldValuesProcessor noopProcessor() {
        return new LambdaProcessor(i -> Optional.empty());
    }

    private static FieldValuesProcessor staticProcessor(Map<String, Collection<ValueLanguagePair>> returnValue) {
        return new LambdaProcessor(i -> Optional.ofNullable(returnValue));
    }

    private static class LambdaProcessor extends FieldValuesProcessor {

        private final Function<Map<String, List<FieldMappingResult>>, Optional<Map<String, Collection<ValueLanguagePair>>>> function;

        public LambdaProcessor(Function<Map<String, List<FieldMappingResult>>, Optional<Map<String, Collection<ValueLanguagePair>>>> function) {
            this.function = function;
        }

        @Override
        public Optional<Map<String, Collection<ValueLanguagePair>>> process(Map<String, List<FieldMappingResult>> input) {
            return function.apply(input);
        }

    }
}
