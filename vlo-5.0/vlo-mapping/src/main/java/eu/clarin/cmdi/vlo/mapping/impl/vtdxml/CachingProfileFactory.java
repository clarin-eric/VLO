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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import eu.clarin.cmdi.vlo.mapping.ProfileFactory;
import eu.clarin.cmdi.vlo.mapping.ProfileReader;
import eu.clarin.cmdi.vlo.mapping.model.CmdProfile;
import java.io.File;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class CachingProfileFactory implements ProfileFactory {

    private final LoadingCache<String, CmdProfile> profileCache;

    public CachingProfileFactory(ProfileReader profileReader) {
        profileCache = CacheBuilder.newBuilder().build(new CacheLoader<String, CmdProfile>() {
            @Override
            public CmdProfile load(String id) throws Exception {
                return profileReader.readProfile(id);
            }

        });
    }

    @Override
    public CmdProfile getProfile(String profileId) {
        return profileCache.getUnchecked(profileId);
    }

    public void invalidateCache() {
        profileCache.invalidateAll();
    }

    public void invalidateCache(File file) {
        profileCache.invalidate(file);
    }
}
