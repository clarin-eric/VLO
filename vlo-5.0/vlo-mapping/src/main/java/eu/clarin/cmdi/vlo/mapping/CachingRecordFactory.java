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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import eu.clarin.cmdi.vlo.mapping.model.CmdRecord;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.CompletionException;
import javax.xml.transform.stream.StreamSource;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class CachingRecordFactory implements CmdRecordFactory {

    private final Cache<String, CmdRecord> recordCache;
    private final RecordReader recordReader;

    public CachingRecordFactory(RecordReader recordReader) {
        this.recordReader = recordReader;
        recordCache = Caffeine.newBuilder()
                .maximumSize(1_000) // TODO: configurable?
                .expireAfterWrite(Duration.ofMinutes(60)) // TODO: configurable?
                .build();
    }

    @Override
    public CmdRecord getRecord(StreamSource source) throws IOException, VloMappingException {
        final String systemId = source.getSystemId();
        try {
            return recordCache.get(systemId, id -> readRecord(id, source));
        } catch (CompletionException ex) {
            throw new VloMappingException("Error while loading reacord into cache: " + systemId, ex);
        }
    }

    private CmdRecord readRecord(String systemId, StreamSource source) throws RuntimeException {
        try {
            if (source != null && systemId.equals(source.getSystemId())) {
                log.trace("SystemId matches provided source, checking for input stream or reader availability: {}", systemId);
                // the source we have has a matching systemId
                if (source.getInputStream() != null && source.getInputStream().available() > 0) {
                    // input stream is available
                    log.trace("Input stream available in source for {}", systemId);
                    return recordReader.readRecord(source);
                } else if (source.getReader() != null && source.getReader().ready()) {
                    // reader is available
                    log.trace("Reader available in source for {}", systemId);
                    return recordReader.readRecord(source);
                }
            }

            // Non-matching systemId OR source is not reading for streaming/reading
            // We will attempt to create new stream source using systemId
            log.debug("Creating a new stream source for systemId {}", systemId);
            final StreamSource src = new StreamSource(systemId);
            src.setInputStream(new URL(systemId).openStream());
            return recordReader.readRecord(src);
        } catch (IOException | VloMappingException ex) {
            throw new RuntimeException("Error while reading record into cache: " + source.getSystemId(), ex);
        }
    }

    public void invalidateCache() {
        recordCache.invalidateAll();
    }

    public void invalidateCache(String systemId) {
        recordCache.invalidate(systemId);
    }

}
