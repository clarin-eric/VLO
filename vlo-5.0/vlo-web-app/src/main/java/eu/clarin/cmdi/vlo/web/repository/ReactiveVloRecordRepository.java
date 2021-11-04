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
package eu.clarin.cmdi.vlo.web.repository;

import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.web.service.VloApiClient;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Repository
@Slf4j
public class ReactiveVloRecordRepository implements VloRecordRepository {

    private final VloApiClient apiClient;

    public ReactiveVloRecordRepository(VloApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<VloRecord> findAll(Pageable pageable) {
        return search("*", Optional.of(pageable));
    }

    @Override
    public List<VloRecord> search(String query, Pageable pageable) {
        return search(query, Optional.of(pageable));
    }

    private List<VloRecord> search(String query, Optional<Pageable> pageable) {
        return apiClient.getRecords(query, getRows(pageable), getStart(pageable));
    }

    private Optional<Long> getRows(Optional<Pageable> pageable) {
        return pageable.map(Pageable::getPageSize).map(Long::valueOf);
    }

    private Optional<Long> getStart(Optional<Pageable> pageable) {
        return pageable.map(p -> p.getOffset() + 1);
    }

}
