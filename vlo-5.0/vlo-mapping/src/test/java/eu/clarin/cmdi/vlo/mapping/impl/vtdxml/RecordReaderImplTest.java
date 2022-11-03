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

import eu.clarin.cmdi.vlo.mapping.VloMappingTestConfiguration;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.VloMappingException;
import eu.clarin.cmdi.vlo.mapping.model.CmdProfile;
import eu.clarin.cmdi.vlo.mapping.model.CmdRecord;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class RecordReaderImplTest {

    private final static VloMappingConfiguration mappingConfig = new VloMappingTestConfiguration();

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
        assertThat(contexts, not(anyOf(nullValue(), empty())));
        assertThat(contexts, hasItem(
                allOf(
                        hasProperty("xpath", equalTo("/cmd:CMD/cmd:Components/cmdp:ArthurianFiction/cmdp:narrative/cmdp:id/text()")),
                        hasProperty("values", hasItem(hasProperty("value", equalTo("id3")))),
                        hasProperty("conceptPath", hasItem(equalTo("http://hdl.handle.net/11459/CCR_C-3894_4d08cc31-25fe-af0c-add4-ca7bdc12f5f7"))))));
    }

    /**
     * Main method for profiling
     *
     * @param args
     * @throws IOException
     * @throws VloMappingException
     */
    public static void main(String[] args) throws IOException, VloMappingException {
        final RecordReaderImpl instance = new RecordReaderImpl(mappingConfig);
        final URL recordUrl = RecordReaderImplTest.class.getResource("/records/p_1345561703673.cmdi");
        final File file = new File(recordUrl.getFile());

        final CmdRecord result = instance.readRecord(file);
        System.out.println("Done reading" + Objects.toString(result));

    }

}
