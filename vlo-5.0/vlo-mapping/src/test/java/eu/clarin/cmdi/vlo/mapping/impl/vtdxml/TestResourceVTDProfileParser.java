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
package eu.clarin.cmdi.vlo.mapping.impl.vtdxml;

import com.ximpleware.VTDGen;
import eu.clarin.cmdi.vlo.mapping.CachingProfileFactory;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;

/**
 * Adapted profile parser that resolves profiles against the local test
 * resources
 *
 * @author twagoo
 */
@Slf4j
public class TestResourceVTDProfileParser extends DefaultVTDProfileParser {

    public TestResourceVTDProfileParser(VloMappingConfiguration mappingConfig) {
        super(mappingConfig);
    }

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

    /**
     * Create an instance of {@link RecordReaderImpl} with instances of
     * {@link CachingProfileFactory}, {@link ProfileReaderImpl}, {@link ProfileContextMapFactoryImpl}
     * and {@link TestResourceVTDProfileParser} based on the provided
     * configuration.
     *
     * @param mappingConfig
     * @return
     */
    public static RecordReaderImpl inNewDefaultRecordReader(VloMappingConfiguration mappingConfig) {
        return new RecordReaderImpl(
                new CachingProfileFactory(
                        new ProfileReaderImpl(
                                new ProfileContextMapFactoryImpl(mappingConfig,
                                        new TestResourceVTDProfileParser(mappingConfig)))));
    }

}
