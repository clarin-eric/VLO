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

import eu.clarin.cmdi.vlo.data.model.VloRecord;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Component
public class VloRecordIndexService {

    private final ReactiveElasticsearchClient reactiveElasticsearchClient;

    public VloRecordIndexService(ReactiveElasticsearchClient reactiveElasticsearchClient) {
        this.reactiveElasticsearchClient = reactiveElasticsearchClient;
    }

    public Mono<IndexResponse> sendToIndex(Mono<VloRecord> recordMono) {
        return recordMono.flatMap(
                vloRecord -> reactiveElasticsearchClient.index(ir
                        -> ir.index("record").source(vloRecord, XContentType.JSON)));
    }

}
