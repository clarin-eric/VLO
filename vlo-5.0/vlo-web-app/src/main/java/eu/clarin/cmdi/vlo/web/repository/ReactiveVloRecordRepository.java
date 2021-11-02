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
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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

    private static final int FETCH_ROWS = 3;

    public ReactiveVloRecordRepository(VloApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Flux<VloRecord> findAll() {
        final Flux<Flux<VloRecord>> recordsFlux = Flux.generate(() -> new RetrievalState(FETCH_ROWS, 1),
                (state, sink) -> {
                    log.debug("Requesting more records [start={}, rows={}]", state.getStart(), state.getRows());
                    sink.next(apiClient.getRecords("*", Optional.of(state.getRows()), Optional.of(state.getStart())));
                    //advance
                    return new RetrievalState(state.getRows(), state.getStart() + state.getRows());
                }
        );
        
        return Flux.concat(recordsFlux);
    }

    @Data
    @AllArgsConstructor
    private class RetrievalState {

        private Integer rows;
        private Integer start;
    }

}
