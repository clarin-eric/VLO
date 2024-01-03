/*
 * Copyright (C) 2021 CLARIN ERIC <clarin@clarin.eu>
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
package eu.clarin.cmdi.vlo.api.processing;

import eu.clarin.cmdi.vlo.data.model.VloRecord;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Component
@Slf4j
public class MapBasedMappingResultStore implements MappingResultStore<UUID> {

    //TODO: methods to invalidate!!
    private final Map<UUID, VloRecord> store;

    public MapBasedMappingResultStore() {
        store = new ConcurrentHashMap<>();
    }

    @Override
    public void storeResult(UUID identifier, VloRecord record) {
        log.debug("Storing record with id {}", identifier);
        store.put(identifier, record);
    }

    @Override
    public Optional<VloRecord> getMappingResult(UUID identifier) {
        log.trace("Retrieving record with id {}", identifier);
        return Optional.ofNullable(store.get(identifier));
    }

}
