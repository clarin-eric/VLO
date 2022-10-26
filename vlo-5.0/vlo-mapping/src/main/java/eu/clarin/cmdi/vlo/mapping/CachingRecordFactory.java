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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import eu.clarin.cmdi.vlo.mapping.model.CmdRecord;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class CachingRecordFactory implements RecordFactory {

    private final LoadingCache<File, CmdRecord> recordCache;

    public CachingRecordFactory(RecordReader recordReader) {
        recordCache = CacheBuilder.newBuilder().build(new CacheLoader<File, CmdRecord>() {
            @Override
            public CmdRecord load(File f) throws Exception {
                return recordReader.readRecord(f);
            }

        });
    }

    @Override
    public CmdRecord getRecord(File file) throws IOException, VloMappingException {
        try {
            return recordCache.get(file);
        } catch (ExecutionException ex) {
            throw new VloMappingException("Error while loading reacord into cache: " + file.getAbsolutePath(), ex);
        }
    }

    public void invalidateCache() {
        recordCache.invalidateAll();
    }

    public void invalidateCache(File file) {
        recordCache.invalidate(file);
    }

}
