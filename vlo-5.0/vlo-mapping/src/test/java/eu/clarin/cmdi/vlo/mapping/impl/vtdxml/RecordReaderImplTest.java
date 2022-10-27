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
package eu.clarin.cmdi.vlo.mapping.impl.vtdxml;

import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.model.CmdProfile;
import eu.clarin.cmdi.vlo.mapping.model.CmdRecord;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class RecordReaderImplTest {

    final VloMappingConfiguration mappingConfig = new VloMappingTestConfiguration();

    public RecordReaderImplTest() {
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
     * Test of readRecord method, of class RecordReaderImpl.
     */
    @Test
    public void testReadRecord() throws Exception {
        final RecordReaderImpl instance = new RecordReaderImpl(mappingConfig);
        final URL recordUrl = getClass().getResource("/records/p_1345561703673.cmdi");
        final File file = new File(recordUrl.getFile());

        final CmdRecord result = instance.readRecord(file);
        assertNotNull(result);
        
        final CmdProfile profile = result.getProfile();
        assertNotNull(profile);
        assertEquals("clarin.eu:cr1:p_1345561703673", profile.getId());
        
        final Collection<ValueContext> contexts = result.getContexts();
        assertNotNull(contexts);
    }

}
