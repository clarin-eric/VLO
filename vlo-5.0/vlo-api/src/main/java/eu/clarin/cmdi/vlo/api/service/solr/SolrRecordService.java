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
package eu.clarin.cmdi.vlo.api.service.solr;

import com.google.common.base.Functions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.vlo.api.service.ReactiveVloRecordService;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordSearchResult;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author twagoo
 */
@Slf4j
public class SolrRecordService implements ReactiveVloRecordService {

    private final SolrDocumentQueryFactoryImpl queryFactory;

    private final SolrClient solrClient;

    private final String solrUsermame;

    private final String solrPassword;

    public SolrRecordService(SolrDocumentQueryFactoryImpl queryFactory, SolrClient solrClient, String solrUsermame, String solrPassword) {
        this.queryFactory = queryFactory;
        this.solrClient = solrClient;
        this.solrUsermame = solrUsermame;
        this.solrPassword = solrPassword;
    }

    @Override
    public Mono<VloRecordSearchResult> getRecords(Optional<String> queryParam, int offset, int size) {
        final SolrQuery query = queryFactory.createDocumentQuery(offset, size);
        queryParam.ifPresent(query::setQuery);

        return queryToResponseMono(query)
                .map(response -> {
                    final SolrDocumentList results = response.getResults();
                    final List<VloRecord> records = FluentIterable.from(results)
                            .transform(this::createVloRecord)
                            .toList();
                    return new VloRecordSearchResult(records, response.getResults().getNumFound(), offset);
                });
    }

    @Override
    public Mono<Long> getRecordCount(String queryParam) {
        final SolrQuery query = queryFactory.createDocumentQuery(1, 0);
        query.setQuery(queryParam);

        return queryToResponseMono(query)
                .map(QueryResponse::getResults)
                .doOnNext(r -> log.debug("Query response: {}", r))
                .map(SolrDocumentList::getNumFound);
    }

    @Override
    public Mono<VloRecord> getRecordById(String id) {
        final SolrQuery query = queryFactory.createDocumentQuery(id);
        return queryToRecordFlux(query)
                .next();
    }

    @Override
    public Mono<VloRecord> saveRecord(VloRecord record) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private Flux<VloRecord> queryToRecordFlux(final SolrQuery query) {
        return queryToResponseMono(query)
                // results (solr documents) iterable as flux
                .flatMapIterable(QueryResponse::getResults)
                // map solr documents to VloRecord pojos
                .map(this::createVloRecord);
    }

    private Mono<QueryResponse> queryToResponseMono(final SolrQuery query) {
        return Mono.just(query)
                .doOnNext(q -> log.debug("Query: {}", q))
                .map(this::fireQuery);
    }

    private VloRecord createVloRecord(SolrDocument solrDoc) {
        final VloRecord record = new VloRecord();
        record.setId(Objects.toString(solrDoc.getFieldValue("id")));
        record.setFields(createFieldValuesdMap(solrDoc));
        return record;
    }

    private Map<String, List<Object>> createFieldValuesdMap(SolrDocument solrDoc) {
        // Note: this can NOT be implemented as a transformation of 
        // solrDoc.getFieldValueMap() using Guava's Maps.transformValues(), as
        // the map provided by the former does not support iteration

        return solrDoc
                .getFieldNames()
                .stream()
                //collect as map
                .collect(ImmutableMap.toImmutableMap(
                        // key: field name
                        Functions.identity(),
                        // value: field values from solr doc as a list
                        f -> ImmutableList.copyOf(solrDoc.getFieldValues(f))));
    }

    protected QueryResponse fireQuery(SolrQuery query) {
        try {
            log.debug("Executing query: {}", query);
            QueryRequest req = new QueryRequest(query);
            req.setBasicAuthCredentials(solrUsermame, solrPassword);
            final QueryResponse response = req.process(solrClient);
            log.trace("Response: {}", response);
            return response;
        } catch (SolrException | SolrServerException e) {
            log.error("Error getting data:", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("IO error:", e);
            throw new RuntimeException(e);
        }
    }

    public SolrDocument getSolrDocument(String docId) {
        if (docId == null) {
            throw new NullPointerException("Cannot get SOLR document for null docId");
        }
        SolrDocument result = null;
        SolrQuery query = new SolrQuery();
        query.setQuery("id:" + ClientUtils.escapeQueryChars(docId));
        query.setFields("*");
        SolrDocumentList docs = fireQuery(query).getResults();
        if (docs.getNumFound() > 1) {
            log.error("Error: found multiple documents for id (will return first one): " + docId + " \nDocuments found: " + docs);
            result = docs.get(0);
        } else if (docs.getNumFound() == 1) {
            result = docs.get(0);
        }
        return result;
    }

}
