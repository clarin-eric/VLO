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
package eu.clarin.cmdi.vlo.api;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.HealthStatus;
import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.elasticsearch.VloRecordRepository;
import java.io.IOException;
import java.util.Collection;
import java.util.Random;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 *
 * @author twagoo
 */
@Slf4j
public class VloApiIntegrationTestHelper {

    private final ElasticsearchClient esClient;
    private final String index;

    public VloApiIntegrationTestHelper(String index, ElasticsearchClient elasticsearchClient) {
        this.index = index;
        this.esClient = elasticsearchClient;
    }

    public void cleanUp(Collection<String> insertedIds) {
        if (checkElasticConnection()) {

            ImmutableList.copyOf(insertedIds).forEach(id -> {
                try {
                    esClient.delete(dr -> dr.index(index).id(id));
                    insertedIds.remove(id);
                } catch (ElasticsearchException | IOException ex) {
                    log.error("Failed to delete item with id {} from index", id, ex);
                }
            });
        }
    }

    public boolean checkElasticConnection() {
        try {
            return esClient.cluster().health().status() != HealthStatus.Red;
        } catch (ElasticsearchException | IOException ex) {
            log.warn("Error while retrieving clust health status", ex);
            return false;
        }
    }

    private Random random = new Random();

    public VloRecord newRecord(VloRecordRepository repo, Collection<String> identifiers, Consumer<VloRecord> processor) {
        final String id = "id_" + random.nextLong();
        VloRecord record = new VloRecord();
        record.setId(id);
        // custom processing
        processor.accept(record);
        repo.save(record)
                .then(Mono.fromCallable(() -> identifiers.add(id)))
                .block();

        return record;
    }
}
