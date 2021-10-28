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
package eu.clarin.cmdi.vlo.batchimporter;

import eu.clarin.cmdi.vlo.data.model.VloRecord;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class VloRecordWriter implements ItemWriter<Mono<VloRecord>> {

    //TODO: make scheduler configurable
    private final Scheduler scheduler = Schedulers.newBoundedElastic(10, Integer.MAX_VALUE, "VRWWorker");

    private final VloApiClient apiClient;

    public VloRecordWriter(VloApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void write(List<? extends Mono<VloRecord>> items) throws Exception {
        log.debug("Writing items");
        final ParallelFlux<VloRecord> itemsFlux = Flux.concat(items).parallel();

        itemsFlux
                .runOn(scheduler)
                .doOnNext(record -> {
                    log.debug("Writing record {}", record.getId());
                })
                .flatMap(record -> apiClient.saveRecord(Mono.just(record)))
                .doOnError(e -> {
                    log.error("Error while sending record to API for indexing", e);
                })
                .then()
                .onErrorResume(e -> Mono.empty())
                .block();
    }

}
