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
package eu.clarin.cmdi.vlo.api;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import eu.clarin.cmdi.vlo.api.configuration.VloElasticsearchConfiguration;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.elasticsearch.VloRecordRepository;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ReactiveElasticsearchClientAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.web.reactive.function.server.EntityResponse;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.iterableWithSize;
import org.springframework.http.HttpStatusCode;

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
public class VloRecordHandlerIntegrationTest {

    private final static Duration RESPONSE_TIMEOUT = Duration.ofSeconds(10);

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private VloRecordRepository respository;

    @Autowired
    private ReactiveElasticsearchOperations operations;

    private final Collection<String> insertedIds = Lists.newArrayList();

    private VloRecordHandler instance;
    private VloApiIntegrationTestHelper testHelper;

    @BeforeEach
    public void setUp() {
        testHelper = new VloApiIntegrationTestHelper("record", elasticsearchClient);
        instance = new VloRecordHandler(respository, operations);
    }

    @AfterEach
    public void cleanUp() {
        testHelper.cleanUp(insertedIds);
    }

    @Test
    public void testGetRecordCount() {
        final ServerRequest request = MockServerRequest.builder()
                .build();
        Mono<ServerResponse> responseMono = instance.getRecordCount(request);
        ServerResponse response = responseMono.block(RESPONSE_TIMEOUT);
        assertNotNull(response);
        assertInstanceOf(EntityResponse.class, response);
        Object entity = ((EntityResponse) response).entity();
        assertInstanceOf(Number.class, entity);
        final Number firstCount = (Number) entity;

        testHelper.newRecord(respository, insertedIds, Objects::requireNonNull);

        response = instance.getRecordCount(request).block(RESPONSE_TIMEOUT);
        assertNotNull(response);
        Number secondCount = (Number) ((EntityResponse) response).entity();

        assertTrue(secondCount.longValue() - firstCount.longValue() == 1);
    }

    @Test
    public void testGetRecordCountWithquery() {
        // random string that we will insert into a new record
        final String randomString = UUID.randomUUID().toString();
        final ServerRequest request = MockServerRequest.builder()
                .queryParam("q", randomString)
                .build();

        {
            Mono<ServerResponse> responseMono = instance.getRecordCount(request);
            ServerResponse response = responseMono.block(RESPONSE_TIMEOUT);
            assertNotNull(response);
            assertInstanceOf(EntityResponse.class, response);
            Object entity = ((EntityResponse) response).entity();
            assertInstanceOf(Number.class, entity);
            assertEquals(0, ((Number) entity).intValue());
        }

        testHelper.newRecord(respository, insertedIds, r -> {
            // set field 'name' with random string as value
            r.setFields(ImmutableMap.of("name", ImmutableList.of(randomString)));
        });

        {
            Mono<ServerResponse> responseMono = instance.getRecordCount(request);
            ServerResponse response = responseMono.block(RESPONSE_TIMEOUT);
            assertNotNull(response);
            assertEquals(1, ((Number) ((EntityResponse) response).entity()).intValue());
        }
    }

    @Test
    public void testGetRecords() {
        // random string that we will insert into a new record
        final String randomString = UUID.randomUUID().toString();

        // query for the string
        final ServerRequest request = MockServerRequest.builder()
                .queryParam("q", randomString)
                .build();

        // 
        {
            final ServerResponse response = instance.getRecords(request).block(RESPONSE_TIMEOUT);
            assertNotNull(response);
            assertInstanceOf(EntityResponse.class, response);
            final Object entity = ((EntityResponse) response).entity();
            assertInstanceOf(Collection.class, entity);
            final Collection<?> collection = (Collection) entity;
            assertTrue(collection.isEmpty());
        }

        // create and insert record with random string
        final VloRecord record = testHelper.newRecord(respository, insertedIds, r -> {
            r.setId("my_id_testGetRecords");
            // set field 'name' with random string as value
            r.setFields(ImmutableMap.of("name", ImmutableList.of(randomString)));
        });

        // search again after record insertion (assumed to be uniquely identifiable by random string)
        {
            final ServerResponse response = instance.getRecords(request).block(RESPONSE_TIMEOUT);
            assertNotNull(response);
            final Collection<VloRecord> collection = (Collection) ((EntityResponse) response).entity();
            assertEquals(1, collection.size());
            assertInstanceOf(VloRecord.class, collection.iterator().next());
            assertEquals("my_id_testGetRecords", ((VloRecord) collection.iterator().next()).getId());
        }

        // create and insert another record
        testHelper.newRecord(respository, insertedIds, Objects::nonNull);
        // get all records - there should be at least two
        {
            final ServerRequest unfilteredRequest = MockServerRequest.builder()
                    .build();
            final ServerResponse response = instance.getRecords(unfilteredRequest).block(RESPONSE_TIMEOUT);
            assertNotNull(response);
            final Collection<?> collection = (Collection) ((EntityResponse) response).entity();
            assertThat(collection.size(), greaterThanOrEqualTo(2));
        }
    }

    @Test
    public void testGetRecordsPagination() {
        final String randomString = UUID.randomUUID().toString();

        // add 10 records
        for (int i = 0; i < 10; i++) {
            testHelper.newRecord(respository, insertedIds, r -> {
                // set field 'name' with random string as value
                r.setFields(ImmutableMap.of("name", ImmutableList.of(randomString)));
            });
        }

        List<Integer> sizes = ImmutableList.of(1, 5, 10, 20);

        sizes.forEach(size -> {
            final int expected = Math.min(size, 10);

            final ServerRequest unfilteredRequest = MockServerRequest.builder()
                    .queryParam("q", randomString)
                    .queryParam("size", size.toString())
                    .build();
            final ServerResponse response = instance.getRecords(unfilteredRequest).block(RESPONSE_TIMEOUT);
            assertNotNull(response);
            final Collection<?> collection = (Collection) ((EntityResponse) response).entity();
            assertThat(collection, hasSize(expected));
        });

    }

    @Test
    public void testGetRecord() {
        // random string that we will insert into a new record
        final String id = "my_id_testGetRecord";
        // query for the string
        final ServerRequest request = MockServerRequest.builder()
                .pathVariable("id", id)
                .build();

        //  request record before insertion
        {
            final ServerResponse response = instance.getRecordFromRepository(request).block(RESPONSE_TIMEOUT);
            assertNotNull(response);
            assertEquals(HttpStatusCode.valueOf(404), response.statusCode());
        }

        // create and insert record with random string
        testHelper.newRecord(respository, insertedIds, r -> {
            r.setId(id);
            r.setFields(ImmutableMap.of("name", ImmutableList.of("test record name")));
        });

        // request again after record insertion 
        {
            final ServerResponse response = instance.getRecordFromRepository(request).block(RESPONSE_TIMEOUT);
            assertNotNull(response);
            assertInstanceOf(EntityResponse.class, response);
            final Object entity = ((EntityResponse) response).entity();
            assertInstanceOf(VloRecord.class, entity);

            final VloRecord record = (VloRecord) entity;
            assertEquals("my_id_testGetRecord", record.getId());
            assertThat(record.getFields(),
                    hasEntry(
                            equalTo("name"),
                            allOf(
                                    iterableWithSize(1),
                                    hasItem("test record name"))));
        }
    }

}
