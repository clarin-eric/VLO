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
package eu.clarin.cmdi.vlo.mapping.definition;

import eu.clarin.cmdi.vlo.mapping.VloMappingTestConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static eu.clarin.cmdi.vlo.mapping.definition.MappingDefinitionSample.MAPPING_DEFINITION_XML_SOURCE;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class MappingDefinitionProviderImplTest {

    public MappingDefinitionProviderImplTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getRules method, of class RulesFactoryImpl.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetRules() throws Exception {
        final MappingDefinitionProviderImpl instance = new MappingDefinitionProviderImpl(() -> MAPPING_DEFINITION_XML_SOURCE());
        final MappingDefinition definition = instance.getDefinition();
        assertNotNull(definition);
        
        MappingDefinitionSample.assertContents(definition);
    }

    @Test
    public void testBadLocation() throws Exception {
        final VloMappingTestConfiguration baseConfig = new VloMappingTestConfiguration();

        assertThrows(RuntimeException.class, () -> {
            final MappingDefinitionProviderImpl instance = new MappingDefinitionProviderImpl(baseConfig.withMappingDefinitionUri(""));
            instance.getDefinition();
        });

        assertThrows(RuntimeException.class, () -> {
            final MappingDefinitionProviderImpl instance = new MappingDefinitionProviderImpl(baseConfig.withMappingDefinitionUri("ftp://this.is.not.supported"));
            instance.getDefinition();
        });
    }

}
