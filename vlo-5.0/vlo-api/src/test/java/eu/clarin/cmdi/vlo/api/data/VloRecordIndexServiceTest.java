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
package eu.clarin.cmdi.vlo.api.data;

import com.google.common.collect.Lists;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import java.util.Collection;
import java.util.function.BooleanSupplier;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRestClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@ExtendWith(SpringExtension.class)
@Profile("elastic-test")
@EnabledIf(expression = "#{environment.acceptsProfiles('elastic-test')}", loadContext = true)
@EnableReactiveElasticsearchRepositories(basePackageClasses = VloRecordRepository.class)
@ImportAutoConfiguration({ReactiveElasticsearchRestClientAutoConfiguration.class, ElasticsearchDataAutoConfiguration.class})
@Slf4j
public class VloRecordIndexServiceTest {

    @Autowired
    private VloRecordRepository recordRepository;

    @Autowired
    private ReactiveElasticsearchClient reactiveElasticsearchClient;
    
    @Autowired
    private RestHighLevelClient elasticsearchClient;

    private final Collection<String> insertedIds = Lists.newCopyOnWriteArrayList();

    @AfterEach
    public void cleanUp() {
        if (elasticChecker().getAsBoolean()) {
            Flux.fromIterable(insertedIds)
                    .parallel()
                    .doOnNext(s -> {
                        log.debug("Cleanup: removing {}", s);
                    })
                    .doOnError(e -> {
                        log.warn("Error while cleaning up from index", e);
                    })
                    .flatMap(id -> {
                        return recordRepository.deleteById(id)
                                .then(Mono.fromCallable(() -> insertedIds.remove(id)));
                    })
                    .then()
                    .block();
        }
    }

    private BooleanSupplier elasticChecker() {
        return () -> elasticsearchClient.getLowLevelClient().isRunning();
    }

    /**
     * Test of sendToIndex method, of class VloRecordIndexService.
     */
    @Test
    public void testSendToIndex() {
        Assumptions.assumeTrue(elasticChecker(), "ElasticSearch not running. SKIPPING!");

        final VloRecord inputRecord = VloRecord.builder()
                .id("id123")
                .dataRoot("testDataRoot")
                .build();

        final VloRecordIndexService instance = new VloRecordIndexService(reactiveElasticsearchClient);

        Mono<IndexResponse> result = instance.sendToIndex(Mono.just(inputRecord));
        final IndexResponse response = result
                .doOnError((e) -> {
                    fail("Error in IndexResponse mono", e);
                })
                .block();

        assertNotNull(response);
        insertedIds.add(response.getId());

        final VloRecord foundRecord = recordRepository
                .findById(response.getId())
                .doOnError((e) -> {
                    fail("Error retrieving record mono", e);
                })
                .block();
        assertNotNull(foundRecord);
//        assertEquals(inputRecord.getDataRoot(), foundRecord.getDataRoot());
//        assertEquals(inputRecord.getId(), foundRecord.getId());
    }

    @Configuration
    public static class TestElasticsearchConfig extends AbstractElasticsearchConfiguration {

        @Override
        @Bean
        public RestHighLevelClient elasticsearchClient() {

            final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                    .connectedTo("localhost:9200")
                    .build();

            return RestClients.create(clientConfiguration).rest();
        }
    }

}
