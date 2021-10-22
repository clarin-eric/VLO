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
package eu.clarin.cmdi.vlo.batchimporter.configuration;

import eu.clarin.cmdi.vlo.batchimporter.FileProcessor;
import eu.clarin.cmdi.vlo.batchimporter.MetadataFilesBatchReaderFactory;
import eu.clarin.cmdi.vlo.batchimporter.VloApiClient;
import eu.clarin.cmdi.vlo.batchimporter.VloApiClientImpl;
import eu.clarin.cmdi.vlo.exception.VloImporterConfigurationException;
import eu.clarin.cmdi.vlo.batchimporter.VloRecordWriter;
import eu.clarin.cmdi.vlo.batchimporter.configuration.MetadataSourceConfiguration.DataRootConfiguration;
import eu.clarin.cmdi.vlo.data.model.MetadataFile;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.naming.OperationNotSupportedException;
import javax.validation.constraints.NotEmpty;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Configuration
@Validated
@EnableBatchProcessing
@EnableConfigurationProperties(MetadataSourceConfiguration.class)
@Slf4j
public class BatchConfiguration {

    @NotEmpty
    @Value("${vlo.importer.api.base-url}")
    private String apiBaseUrl;

    @Autowired
    private MetadataSourceConfiguration metadataSourceConfiguration;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Bean
    public ItemReader<MetadataFile> reader() throws Exception {
        // data roots config to map
        final Map<String, String> rootsMap
                = Optional.ofNullable(metadataSourceConfiguration.getRoots())
                        .orElseThrow(() -> new VloImporterConfigurationException("No valid data roots are configured"))
                        .stream()
                        .collect(Collectors.toMap(DataRootConfiguration::getName, DataRootConfiguration::getPath));

        final MetadataFilesBatchReaderFactory metadataFilesBatchReaderFactory = new MetadataFilesBatchReaderFactory(rootsMap);
        return metadataFilesBatchReaderFactory.getObject();
    }

    @Bean(name = "vloApiWebClient")
    WebClient apiWebClient() {
        return webClientBuilder
                .baseUrl(apiBaseUrl)
                .build();
    }

    @Bean
    public VloApiClient apiClient() {
        final WebClient webClient = apiWebClient();
        return new VloApiClientImpl(webClient);
    }

    @Bean
    public FileProcessor processor() {
        return new FileProcessor(apiClient());
    }

    @Bean
    public ItemWriter<Mono<VloRecord>> writer() throws OperationNotSupportedException {
        return new VloRecordWriter();
    }

    @Bean
    public Job processFileJob(Step step1) {
        //TODO: Construct metadata hierarchy before processing

        //TODO: separate into multiple steps (multiple processors)?
        // -----> Read to mapping input  object, send to API (for mapping)
        // -----> Collect VLO record results, send to API (for index)
        return jobBuilderFactory.get("processFileJob")
                .incrementer(new RunIdIncrementer())
                //.listener(listener)
                .flow(step1)
                .end()
                .build();
    }
    
    @Bean
    public TaskExecutor taskExecutor() {
        final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(8);
        taskExecutor.setMaxPoolSize(20);
        taskExecutor.setThreadNamePrefix("vlo-batch-");
        return taskExecutor;
    }

    @Bean
    public Step step1(ItemWriter<Mono<VloRecord>> writer, TaskExecutor taskExecutor) throws Exception {
        return stepBuilderFactory.get("step1")
                .<MetadataFile, Mono<VloRecord>>chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .taskExecutor(taskExecutor)
                .build();
    }
}
