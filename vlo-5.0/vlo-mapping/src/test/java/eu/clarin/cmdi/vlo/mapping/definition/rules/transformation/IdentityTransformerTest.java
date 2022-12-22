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
package eu.clarin.cmdi.vlo.mapping.definition.rules.transformation;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.model.SimpleValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
public class IdentityTransformerTest {

    private final static String FIELD = "field";

    private IdentityTransformer instance;

    @BeforeEach
    public void setUp() {
        instance = new IdentityTransformer();
        instance.setField(FIELD);
    }

    @Test
    public void testGetScore() {
        // no score
        instance = new IdentityTransformer(FIELD);
        assertEquals(5, instance.getScore(5));
        // override
        instance = new IdentityTransformer(FIELD, 10);
        assertEquals(10, instance.getScore(5));
    }

}
