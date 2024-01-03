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

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.google.common.collect.Lists;
import eu.clarin.cmdi.vlo.api.VloApiIntegrationTestHelper;
import eu.clarin.cmdi.vlo.api.configuration.VloElasticsearchConfiguration;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecord.Resource;
import eu.clarin.cmdi.vlo.elasticsearch.VloRecordRepository;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ReactiveElasticsearchClientAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@ExtendWith(SpringExtension.class)
@Profile("elastic-test")
@ContextConfiguration(classes = VloElasticsearchConfiguration.class)
@TestPropertySource(locations = {"classpath:elastic-test.properties"})
@EnabledIf(expression = "#{environment.acceptsProfiles('elastic-test')}", loadContext = true)
@EnableReactiveElasticsearchRepositories(basePackageClasses = VloRecordRepository.class)
@ImportAutoConfiguration({ElasticsearchClientAutoConfiguration.class, ReactiveElasticsearchClientAutoConfiguration.class, ElasticsearchDataAutoConfiguration.class})
@Slf4j
public class VloRecordRepositoryIntegrationTest {

    @Autowired
    private VloRecordRepository instance;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private final Collection<String> insertedIds = Lists.newCopyOnWriteArrayList();

    private VloApiIntegrationTestHelper testHelper;

    @BeforeEach
    public void setUp() {
        testHelper = new VloApiIntegrationTestHelper("record", elasticsearchClient);
    }

    @AfterEach
    public void cleanUp() {
        testHelper.cleanUp(insertedIds);
    }

    /**
     * Test of sendToIndex method, of class VloRecordIndexService.
     */
    @Test
    public void testSave() throws IOException {
        Assumptions.assumeTrue(testHelper::checkElasticConnection, "ElasticSearch not running. SKIPPING!");

        final ArrayList<Resource> resources = Lists.newArrayList(
                new Resource("resource1", "http://repo.eu/id123/resource1", "Resource", "text/plain"),
                new Resource("resource2", "http://repo.eu/id123/resource2", "Resource", "text/plain")
        );

        final VloRecord inputRecord = new VloRecord();
        inputRecord.setId("id123");
        inputRecord.setDataRoot("testDataRoot");
        inputRecord.setProfileId("clarin_profile_id");
        inputRecord.setSelflink("http://repo.eu/id123");
        inputRecord.setResources(resources);

        inputRecord.getFields().put("test", Lists.newArrayList("test1", "test2"));
        inputRecord.getResources().add(new Resource("resource1", "http://repo.eu/id123/resource1", "Resource", "text/plain"));

        final Mono<VloRecord> result = instance.save(inputRecord);
        final VloRecord response = result
                .doOnError((e) -> {
                    fail("Error in IndexResponse mono", e);
                })
                .block();

        assertNotNull(response);
        insertedIds.add(response.getId());

        // test with repo
        final Mono<VloRecord> repoRetrieved = instance.findById(Mono.just(inputRecord.getId()));
        repoRetrieved.flatMap(retrieved -> {
            assertResultRecord(inputRecord, retrieved);
            return Mono.empty();
        }).doOnError((e) -> {
            fail("Error in IndexResponse mono", e);
        }).block(Duration.ofSeconds(60));

        // TODO: Low-level verification?
        //
//        // test with client
//        final GetResponse<VloRecord> getResponse = elasticsearchClient.get(g -> g
//                .index("record")
//                .id(inputRecord.getId()), VloRecord.class);
//        assertTrue(getResponse.found());
//        assertResultRecord(inputRecord, getResponse.source());
//
//        // test with operations API
//        final VloRecord operationsRetrieved = operations.get(inputRecord.getId(), VloRecord.class);
//        assertResultRecord(inputRecord, operationsRetrieved);
    }

    private void assertResultRecord(final VloRecord inputRecord, VloRecord retrieved) {
        assertEquals(inputRecord.getId(), retrieved.getId());
        assertEquals(inputRecord.getDataRoot(), retrieved.getDataRoot());
        assertEquals(inputRecord.getSelflink(), retrieved.getSelflink());
        assertEquals(inputRecord.getDataRoot(), retrieved.getDataRoot());
    }
}
