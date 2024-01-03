/*
 * Copyright (C) 2023 twagoo
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
package eu.clarin.cmdi.vlo.api.service;

import eu.clarin.cmdi.vlo.api.model.VloRecordsRequest;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordSearchResult;
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 *
 * @author twagoo
 */
public interface ReactiveVloRecordService {

    /**
     *
     * @param query query for filtering (may be null or empty)
     * @param filters filter queries in the format 'field -> [OR] values' (may
     * be null or empty)
     * @return
     */
    Mono<Long> getRecordCount(String query, Map<String, ? extends Iterable<String>> filters);

    Mono<VloRecord> getRecordById(String id);

    /**
     *
     * @param request search parameters (filtering and pagination)
     * @return
     */
    Mono<VloRecordSearchResult> getRecords(VloRecordsRequest request);

    Mono<VloRecord> saveRecord(VloRecord record);

}
