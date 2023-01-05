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
import eu.clarin.cmdi.vlo.batchimporter.VloBatchImporterListenersConfiguration;
import eu.clarin.cmdi.vlo.exception.VloImporterConfigurationException;
import eu.clarin.cmdi.vlo.batchimporter.VloRecordWriter;
import eu.clarin.cmdi.vlo.batchimporter.configuration.MetadataSourceConfiguration.DataRootConfiguration;
import eu.clarin.cmdi.vlo.data.model.MetadataFile;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.naming.OperationNotSupportedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.StepListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Configuration
@Validated
@EnableConfigurationProperties(MetadataSourceConfiguration.class)
@Import(VloBatchImporterListenersConfiguration.class)
@Slf4j
public class BatchConfiguration {

    @NotEmpty
    @Value("${vlo.importer.api.base-url}")
    private String apiBaseUrl;

    @Autowired
    private MetadataSourceConfiguration metadataSourceConfiguration;

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
        return new VloRecordWriter(apiClient());
    }

    @Bean
    public Job processFileJob(JobRepository jobRepository, Step step, JobExecutionListener listener) {
        //TODO: Construct metadata hierarchy before processing
        //TODO: separate into multiple steps (multiple processors)?
        // -----> Read to mapping input  object, send to API (for mapping)
        // -----> Collect VLO record results, send to API (for index)

        return new JobBuilder("processFileJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(step)
                .end()
                .listener(listener)
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
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager, ItemWriter<Mono<VloRecord>> writer, TaskExecutor taskExecutor,
            ItemReadListener<MetadataFile> readListener, ItemProcessListener<MetadataFile, Mono<VloRecord>> processListener, ItemWriteListener writeListener
    ) throws Exception {
        return new StepBuilder("step1", jobRepository)
                .<MetadataFile, Mono<VloRecord>>chunk(10, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .taskExecutor(taskExecutor)
                .listener(processListener)
                .listener(readListener)
                .listener(writeListener)
                .build();
    }
}
