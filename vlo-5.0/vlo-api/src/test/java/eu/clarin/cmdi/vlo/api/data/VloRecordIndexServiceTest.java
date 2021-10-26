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

import eu.clarin.cmdi.vlo.api.VloApiConfiguration;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import org.elasticsearch.action.index.IndexResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRestClientAutoConfiguration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@ExtendWith(SpringExtension.class)
@ImportAutoConfiguration({ReactiveElasticsearchRestClientAutoConfiguration.class, ElasticsearchDataAutoConfiguration.class})
public class VloRecordIndexServiceTest {

    /**
     * Test of sendToIndex method, of class VloRecordIndexService.
     */
    @Test
    public void testSendToIndex(@Autowired ReactiveElasticsearchClient reactiveElasticsearchClient) {
        Mono<VloRecord> recordMono
                = Mono.just(VloRecord.builder()
                        .id("id123")
                        .dataRoot("testDataRoot")
                        .build());
        
        final VloRecordIndexService instance = new VloRecordIndexService(reactiveElasticsearchClient);

        Mono<IndexResponse> result = instance.sendToIndex(recordMono);
        final IndexResponse response = result
                .doOnError((e) -> {
                    fail("Error in IndexResponse mono", e);
                })
                .block();
    }

}
