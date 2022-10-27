/*
 * Copyright (C) 2022 CLARIN
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
import com.ximpleware.VTDGen;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.model.CmdProfile;
import eu.clarin.cmdi.vlo.mapping.model.Context;
import java.net.URL;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class ProfileReaderImplTest {

    final VloMappingConfiguration mappingConfig = new VloMappingTestConfiguration();

    final VTDProfileParser parser = new DefaultVTDProfileParser(mappingConfig) {
        @Override
        protected boolean doParse(VTDGen vg, String profileId, boolean ns) {
            log.info("Trying to parse profile {} from schema in bundled resources", profileId);
            final String resource = String.format("/profiles/%s.xsd", profileId.replaceAll("[:_\\.\\/\\\\]", "_"));
            // get file from bundled resources
            final URL resourceUri = getClass().getResource(resource);
            if (resourceUri == null) {
                log.error("Profile XSD not found for id {}: {}", profileId, resource);
                throw new RuntimeException("Profile XSD not found - see logs");
            } else {
                return vg.parseFile(resourceUri.getFile(), ns);
            }
        }

    };

    /**
     * Test of readProfile method, of class ProfileReaderImpl.
     */
    @Test
    public void testReadProfile() throws Exception {

        final ConceptLinkPathMapper conceptLinkPathMapper = new ConceptLinkPathMapperImpl(mappingConfig, parser);
        final ProfileReaderImpl instance = new ProfileReaderImpl(conceptLinkPathMapper);

        final String profileId = "clarin.eu:cr1:p_1345561703673";
        final CmdProfile result = instance.readProfile(profileId);

        assertNotNull(result);
        Map<String, Context> xpathConceptPathMap = result.getXpathContextMap();
        assertNotNull(xpathConceptPathMap);
        assertThat(xpathConceptPathMap, hasKey("/cmd:CMD/cmd:Components/cmdp:ArthurianFiction/text()"));
        assertThat("Context's own concept", xpathConceptPathMap, hasEntry(
                //key
                equalTo("/cmd:CMD/cmd:Components/cmdp:ArthurianFiction/cmdp:manuscript/cmdp:id/text()"),
                //value
                hasProperty("conceptPath", hasItem("http://hdl.handle.net/11459/CCR_C-3894_4d08cc31-25fe-af0c-add4-ca7bdc12f5f7"))
        ));
        assertThat("Context's ancestor concept", xpathConceptPathMap, hasEntry(
                //key
                equalTo("/cmd:CMD/cmd:Components/cmdp:ArthurianFiction/cmdp:manuscript/cmdp:id/text()"),
                //value
                hasProperty("conceptPath", hasItem("http://hdl.handle.net/11459/CCR_C-4347_728552dd-4c23-0a69-390c-10214c05983f"))
        ));
    }

}
