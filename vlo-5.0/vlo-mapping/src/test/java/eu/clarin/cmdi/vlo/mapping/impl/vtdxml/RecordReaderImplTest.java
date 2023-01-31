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
import static eu.clarin.cmdi.vlo.mapping.VloMappingTestHelper.createStreamSourceForResource;
import eu.clarin.cmdi.vlo.mapping.model.CmdProfile;
import eu.clarin.cmdi.vlo.mapping.model.CmdRecord;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import javax.xml.transform.stream.StreamSource;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class RecordReaderImplTest {

    private final static VloMappingConfiguration mappingConfig = new VloMappingTestConfiguration();
    private static final String PROFILE_ID_1 = "p_1345561703673";
    private static final String TEST_RESOURCE_1 = "/records/" + PROFILE_ID_1 + ".cmdi";

    @Nested
    @DisplayName("Tests for reading of a single record")
    public class ReadRecordTest {

        static RecordReaderImpl instance;
        static StreamSource source;
        static CmdRecord result;

        @BeforeAll
        public static void setUpClass() throws Exception {
            instance = TestResourceVTDProfileParser.inNewDefaultRecordReader(mappingConfig);
            source = createStreamSourceForResource(RecordReaderImplTest.class, TEST_RESOURCE_1);
            result = instance.readRecord(source);
        }

        @Test
        public void testProcessing() throws Exception {
            assertNotNull(result);
            assertNotNull(result.getProfile(), "Profile read");
            assertNotNull(result.getContexts(), "Contexts read");
            assertNotNull(result.getHeader(), "Header read");
        }

        @Test
        public void testProfile() throws Exception {
            final CmdProfile profile = result.getProfile();
            assertNotNull(profile);
            assertEquals("clarin.eu:cr1:" + PROFILE_ID_1, profile.getId());
        }

        @Test
        public void testContexts() throws Exception {
            final Collection<ValueContext> contexts = result.getContexts();
            assertThat(contexts, not(anyOf(nullValue(), empty())));
            assertThat(contexts, hasItem(
                    allOf(
                            hasProperty("xpath", equalTo("/cmd:CMD/cmd:Components/cmdp:ArthurianFiction/cmdp:narrative/cmdp:id/text()")),
                            hasProperty("values", hasItem(hasProperty("value", equalTo("id3")))),
                            hasProperty("conceptPath", hasItem(equalTo("http://hdl.handle.net/11459/CCR_C-3894_4d08cc31-25fe-af0c-add4-ca7bdc12f5f7"))))));
        }

        @Test
        public void testHeader() throws Exception {
            CmdRecord.Header header = result.getHeader();
            assertNotNull(header);
            assertEquals("clarin.eu:cr1:" + PROFILE_ID_1, header.getProfileId());
            assertEquals("my/project/test/resources/records/p_1345561703673.cmdi", header.getSelfLink());
            assertEquals("Test collection", header.getCollectionDisplayName());
        }

        @Test
        public void testResources() throws Exception {
            // TODO
        }
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
        final StreamSource source = createStreamSourceForResource(RecordReaderImplTest.class, TEST_RESOURCE_1);

        final CmdRecord result = instance.readRecord(source);
        System.out.println("Done reading" + Objects.toString(result));

    }

}
