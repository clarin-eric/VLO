/*
 * Copyright (C) 2024 twagoo
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
import java.util.Optional;

/**
 *
 * @author twagoo
 */
public interface VloRecordService {

    Optional<VloRecord> getRecordById(String id);

    Long getRecordCount(String queryParam, Map<String, ? extends Iterable<String>> filters);

    VloRecordSearchResult getRecords(VloRecordsRequest request);

    Optional<VloRecord> saveRecord(VloRecord record);
    
}
