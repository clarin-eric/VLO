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

import com.google.common.collect.ImmutableMap;
import com.ximpleware.VTDException;
import eu.clarin.cmdi.vlo.mapping.ProfileReader;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.model.CmdProfile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class ProfileReaderImpl implements ProfileReader {

    private final ConceptLinkPathMapper conceptLinkPathMapper;

    public ProfileReaderImpl(VloMappingConfiguration config) {
        this(new ConceptLinkPathMapperImpl(config));
    }

    public ProfileReaderImpl(ConceptLinkPathMapper conceptLinkPathMapper) {
        this.conceptLinkPathMapper = conceptLinkPathMapper;
    }

    @Override
    public CmdProfile readProfile(String profileId) throws IOException {
        final CmdProfile profile = new CmdProfile();

        try {
            final Map<String, List<Pattern>> conceptPathMap = conceptLinkPathMapper.createConceptLinkPathMapping(profileId);
            final Map<Pattern, String> patternConceptMap = createPatternConceptMap(conceptPathMap);
            profile.setPatternConceptMap(patternConceptMap);
            return profile;
        } catch (VTDException ex) {
            throw new IOException("XML processing exception while reading profile " + profileId, ex);
        }

    }

    private ImmutableMap<Pattern, String> createPatternConceptMap(final Map<String, List<Pattern>> conceptPathMap) {
        final ImmutableMap.Builder<Pattern, String> builder = ImmutableMap.builder();
        conceptPathMap.forEach((concept, patterns) -> {
            for (Pattern p : patterns) {
                builder.put(p, concept);
            }
        });
        return builder.build();
    }

}
