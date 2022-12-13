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
package eu.clarin.cmdi.vlo.mapping;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import eu.clarin.cmdi.vlo.mapping.model.CmdProfile;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletionException;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class CachingProfileFactory implements ProfileFactory {

    private final LoadingCache<String, CmdProfile> profileCache;

    public CachingProfileFactory(ProfileReader profileReader) {
        profileCache = Caffeine.newBuilder()
                .maximumSize(100_000) // TODO: configurable?
                .expireAfterWrite(Duration.ofMinutes(5)) // TODO: configurable?
                .build(profileReader::readProfile);
    }

    @Override
    public CmdProfile getProfile(String profileId) throws IOException, VloMappingException {
        try {
            return profileCache.get(profileId);
        } catch (CompletionException ex) {
            throw new VloMappingException("Error while loading profile into cache: " + profileId, ex);
        }
    }

    public void invalidateCache() {
        profileCache.invalidateAll();
    }

    public void invalidateCache(String profile) {
        profileCache.invalidate(profile);
    }
}
