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

import eu.clarin.cmdi.vlo.batchimporter.model.MetadataFile;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import java.util.List;
import javax.naming.OperationNotSupportedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Value("${vlo.importer.recordsDirectory}")
    private String recordsDirectoryPath;

    @Bean(name = "reader")
    public MetadataFilesBatchReaderFactory readerFactory() {
        return new MetadataFilesBatchReaderFactory(recordsDirectoryPath);
    }

    @Bean
    public ItemReader<MetadataFile> reader() throws Exception {
        return readerFactory().getObject();
    }

    @Bean
    public FileProcessor processor() {
        return new FileProcessor();
    }

    @Bean
    public ItemWriter<VloRecord> writer() throws OperationNotSupportedException {
        return (List<? extends VloRecord> items) -> {
            log.info("Writing items {}", items);
        };
    }

    @Bean
    public Job processFileJob(Step step1) {
        return jobBuilderFactory.get("processFileJob")
                .incrementer(new RunIdIncrementer())
                //.listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(ItemWriter<VloRecord> writer) throws Exception {
        return stepBuilderFactory.get("step1")
                .<MetadataFile, VloRecord>chunk(5)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .build();
    }
}
