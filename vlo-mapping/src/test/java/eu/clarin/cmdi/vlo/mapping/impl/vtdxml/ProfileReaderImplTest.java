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

import com.ximpleware.VTDGen;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.model.CmdProfile;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class ProfileReaderImplTest {

    final VloMappingConfiguration mappingConfig = new VloMappingConfiguration() {
        {
            setProfileSchemaUrl("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/{PROFILE_ID}/xsd");
            setVocabularyRegistryUrl("http://clavas.clarin.eu/clavas/public/api/find-concepts");
        }
    };

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
    }

}
