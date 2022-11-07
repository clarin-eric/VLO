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
package eu.clarin.cmdi.vlo.mapping;

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.mapping.model.FieldMappingResult;
import eu.clarin.cmdi.vlo.mapping.model.SimpleValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import eu.clarin.cmdi.vlo.mapping.rules.transformation.Transformation;
import eu.clarin.cmdi.vlo.mapping.rules.MappingRule;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@ExtendWith(MockitoExtension.class)
public class ContextFieldValueMapperImplTest {

    @Mock
    private MappingRule rule;

    @Mock
    private Transformation transformation;

    private final ValueContext context = SimpleValueContext.builder()
            .values(ImmutableList.of(new ValueLanguagePair("value1", "en")))
            .build();
    private ContextFieldValueMapperImpl instance;

    @BeforeEach
    public void beforeEach() {
        instance = new ContextFieldValueMapperImpl(ImmutableList.of(rule, rule, rule));
    }

    /**
     * Test of mapContext method, of class ContextFieldValueMapperImpl.
     */
    @Test
    public void testMapContextNoMatchingRules() {
        when(rule.applies(context)).thenReturn(false);

        Stream<FieldMappingResult> result = instance.mapContext(context);
        verify(rule, times(3)).applies(any(ValueContext.class));
        verify(rule, never()).getTransformations();
        verify(rule, never()).isTerminal();
        assertThat(result.collect(Collectors.toList()), empty());
    }

    @Test
    public void testMapContext() {
        {
            ValueLanguagePair output = new ValueLanguagePair("output1", "en");
            when(rule.applies(context))
                    .thenReturn(false, true, true);
            when(rule.getTransformations())
                    .thenReturn(Stream.of(transformation), Stream.of(transformation));
            when(transformation.apply(context))
                    .thenReturn(Stream.of(output), Stream.of(output));

            // map and collect
            List<FieldMappingResult> result = instance.mapContext(context).collect(Collectors.toList());
            assertThat(result, hasSize(2));

            verify(rule, times(2)).getTransformations();
            verify(transformation, times(2)).apply(context);
        }
    }

    @Test
    public void testMapContextTerminal() {
        {
            ValueLanguagePair output = new ValueLanguagePair("output1", "en");
            when(rule.applies(context))
                    .thenReturn(false, true, true);
            when(rule.isTerminal())
                    .thenReturn(true);
            when(rule.getTransformations())
                    .thenReturn(Stream.of(transformation));
            when(transformation.apply(context))
                    .thenReturn(Stream.of(output));

            // map and collect
            List<FieldMappingResult> result = instance.mapContext(context).collect(Collectors.toList());
            assertThat(result, hasSize(1));

            verify(rule, times(1)).getTransformations();
            verify(transformation, atLeastOnce()).apply(context);
        }
    }
}
