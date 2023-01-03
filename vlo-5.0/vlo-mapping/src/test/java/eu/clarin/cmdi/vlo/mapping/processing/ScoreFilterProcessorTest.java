/*
 * Copyright (C) 2022 twagoo
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
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author twagoo
 */
@ExtendWith(MockitoExtension.class)
public class ScoreFilterProcessorTest {

    @Mock
    private ValueContext vc;

    @Mock
    private ValueLanguagePair vlp1;

    @Mock
    private ValueLanguagePair vlp2;

    @Mock
    private ValueLanguagePair vlp3;

    private ScoreFilterProcessor instance;
    private Map<String, List<FieldMappingResult>> input;

    @BeforeEach
    public void setUp() {
        instance = new ScoreFilterProcessor();
        input = ImmutableMap.of(
                "fieldA", ImmutableList.of(
                        new FieldMappingResult("fieldA", vc, ImmutableList.of(vlp1), 1),
                        new FieldMappingResult("fieldA", vc, ImmutableList.of(vlp2), 3),
                        new FieldMappingResult("fieldA", vc, ImmutableList.of(vlp3), 2)),
                "fieldB", ImmutableList.of(
                        new FieldMappingResult("fieldB", vc, ImmutableList.of(vlp1), 2),
                        new FieldMappingResult("fieldB", vc, ImmutableList.of(vlp2), 2),
                        new FieldMappingResult("fieldB", vc, ImmutableList.of(vlp3), 1)),
                "fieldC", ImmutableList.of(
                        new FieldMappingResult("fieldC", vc, ImmutableList.of(vlp1), 1),
                        new FieldMappingResult("fieldC", vc, ImmutableList.of(vlp2), 2),
                        new FieldMappingResult("fieldC", vc, ImmutableList.of(vlp3), 3)));
    }

    /**
     * Test of process method, of class ScoreFilterProcessor.
     */
    @Test
    public void testKeepTop() {
        instance.setFields("fieldA fieldC");
        instance.setKeepTop(1);

        final Optional<Map<String, Collection<ValueLanguagePair>>> result = instance.process(input);

        assertTrue(result.isPresent());

        final Map<String, Collection<ValueLanguagePair>> output = result.get();

        assertThat("Processed field", output, hasEntry(
                equalTo("fieldA"),
                contains(sameInstance(vlp2))));

        assertThat("Untouched field", output, hasEntry(equalTo(
                "fieldB"),
                contains(sameInstance(vlp1),
                        sameInstance(vlp2),
                        sameInstance(vlp3))));

        assertThat("Processed field", output, hasEntry(equalTo(
                "fieldC"),
                contains(sameInstance(vlp3))));
    }

    /**
     * Test of process method, of class ScoreFilterProcessor.
     */
    @Test
    public void testKeepHighestScoring() {
        instance.setFields("fieldA fieldB");
        instance.setKeepHighestScoring(true);

        final Optional<Map<String, Collection<ValueLanguagePair>>> result = instance.process(input);

        assertTrue(result.isPresent());

        final Map<String, Collection<ValueLanguagePair>> output = result.get();

        assertThat("Processed field", output, hasEntry(equalTo(
                "fieldA"),
                contains(sameInstance(vlp2))));

        assertThat("Processed field", output, hasEntry(equalTo(
                "fieldB"),
                contains(sameInstance(vlp1), sameInstance(vlp2))));

        assertThat("Untouched field", output, hasEntry(equalTo(
                "fieldC"),
                contains(sameInstance(vlp1),
                        sameInstance(vlp2),
                        sameInstance(vlp3))));
    }

}
