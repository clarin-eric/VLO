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

import eu.clarin.cmdi.vlo.data.model.MetadataFile;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingRequest;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingProcessingTicket;
import eu.clarin.cmdi.vlo.exception.InputProcessingException;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
@AllArgsConstructor
public class FileProcessor implements ItemProcessor<MetadataFile, Mono<VloRecord>> {

    private final VloApiClient apiClient;

    //TODO: make scheduler configurable
    private final Scheduler scheduler = Schedulers.newBoundedElastic(10, Integer.MAX_VALUE, "FPWorker");

    @Override
    public Mono<VloRecord> process(MetadataFile inputFile) throws Exception {
        log.info("Processing metadata file {}", inputFile);

        try {
            //Make a request object for the API
            final VloRecordMappingRequest importRequest = VloRecordMappingRequest.builder()
                    .dataRoot(inputFile.getDataRoot())
                    .file(inputFile.getLocation().toString())
                    .xmlContent(xmlContentFromFile(inputFile))
                    .build();

            //Publish it to start the reactive chain
            final Mono<VloRecordMappingRequest> importRequestMono
                    = Mono.just(importRequest).publishOn(scheduler);

            //Send request object to the API
            final Mono<VloRecordMappingProcessingTicket> ticketMono
                    = importRequestMono.flatMap(apiClient::sendRecordMappingRequest);

            //Retrieve record
            final Mono<VloRecord> recordMono
                    = ticketMono.flatMap(apiClient::retrieveRecord);

            return recordMono;
        } catch (IOException ex) {
            throw new InputProcessingException("Error while processing input from " + inputFile.toString(), ex);
        }
    }

    private byte[] xmlContentFromFile(MetadataFile file) throws IOException {
        return java.nio.file.Files.readAllBytes(file.getLocation());
    }

}
