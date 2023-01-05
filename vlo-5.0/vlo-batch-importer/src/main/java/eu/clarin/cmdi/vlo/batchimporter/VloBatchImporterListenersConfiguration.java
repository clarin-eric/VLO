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
package eu.clarin.cmdi.vlo.batchimporter;

import eu.clarin.cmdi.vlo.data.model.MetadataFile;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

/**
 * Configures a number of listeners (for logging/debugging purposes) for the
 * import batch process.
 *
 * @author twagoo
 */
@Slf4j
public class VloBatchImporterListenersConfiguration {

    @Bean
    public JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {

            @Override
            public void beforeJob(JobExecution jobExecution) {
                log.debug("Before job: {}", jobExecution);
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                log.debug("After job: {}", jobExecution);
            }
        };
    }

    @Bean
    public ItemReadListener<MetadataFile> itemReadListener() {
        return new ItemReadListener<MetadataFile>() {
            @Override
            public void beforeRead() {
                log.trace("Before read");
            }

            @Override
            public void afterRead(MetadataFile item) {
                log.trace("After read");
            }
        };
    }

    @Bean
    public ItemProcessListener<MetadataFile, Mono<VloRecord>> itemProcessListener() {
        return new ItemProcessListener<MetadataFile, Mono<VloRecord>>() {

            @Override
            public void beforeProcess(MetadataFile item) {
                log.trace("Before process: {}", item);
            }

            @Override
            public void afterProcess(MetadataFile item, Mono<VloRecord> result) {
                log.trace("After process: {} -> {}", item, result);
            }
        };
    }

    @Bean
    public ItemWriteListener<Mono<VloRecord>> itemWriteListener() {
        return new ItemWriteListener<Mono<VloRecord>>() {
            @Override
            public void beforeWrite(Chunk<? extends Mono<VloRecord>> items) {
                log.trace("Before write: {}", items);
            }

            @Override
            public void afterWrite(Chunk<? extends Mono<VloRecord>> items) {
                log.trace("After write: {}", items);
            }
        };
    }

//    @Bean
//    public StepExecutionListener stepExecutionListener() {
//        return new StepExecutionListener() {
//
//            @Override
//            public void beforeStep(StepExecution stepExecution) {
//                log.debug("Before step: {}", stepExecution);
//            }
//
//            @Override
//            public ExitStatus afterStep(StepExecution stepExecution) {
//                log.debug("After step: {}", stepExecution);
//                return stepExecution.getExitStatus();
//            }
//        };
//    }
}
