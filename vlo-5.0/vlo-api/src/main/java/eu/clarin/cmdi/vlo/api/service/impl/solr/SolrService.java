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
package eu.clarin.cmdi.vlo.api.service.impl.solr;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.api.model.VloRecordsRequest;
import eu.clarin.cmdi.vlo.api.service.FieldValueLabelService;
import eu.clarin.cmdi.vlo.api.service.ReactiveVloFacetsService;
import eu.clarin.cmdi.vlo.api.service.ReactiveVloRecordService;
import eu.clarin.cmdi.vlo.data.model.Facet;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordSearchResult;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.springframework.core.convert.converter.Converter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author twagoo
 */
@AllArgsConstructor
@Slf4j
public class SolrService implements ReactiveVloRecordService, ReactiveVloFacetsService {

    private final SolrDocumentQueryFactoryImpl queryFactory;

    private final SolrClient solrClient;

    private final String solrUsermame;

    private final String solrPassword;

    private final FieldValueLabelService fieldValueLabelService;

    private final Converter<SolrDocument, VloRecord> recordConverter;

    @Override
    public Mono<VloRecordSearchResult> getRecords(VloRecordsRequest request) {
        final SolrQuery solrQuery = queryFactory.createLimitedDocumentQuery(request.getFrom(), request.getSize());
        if (request.getQuery() != null) {
            solrQuery.setQuery(request.getQuery());
        }
        applyFilterQuery(solrQuery, request.getFilters());

        return queryToResponseMono(solrQuery)
                .map(response -> {
                    final SolrDocumentList results = response.getResults();
                    final List<VloRecord> records = FluentIterable.from(results)
                            .transform(recordConverter::convert)
                            .toList();
                    return new VloRecordSearchResult(records, response.getResults().getNumFound(), request.getFrom());
                });
    }

    @Override
    public Mono<Long> getRecordCount(String queryParam, Map<String, ? extends Iterable<String>> filters) {
        final SolrQuery query = queryFactory.createLimitedDocumentQuery(0, 0);
        if (query != null) {
            query.setQuery(queryParam);
        }
        applyFilterQuery(query, filters);

        return queryToResponseMono(query)
                .map(QueryResponse::getResults)
                .doOnNext(r -> log.debug("Query response: {}", r))
                .map(SolrDocumentList::getNumFound);
    }

    @Override
    public Mono<VloRecord> getRecordById(String id) {
        log.info("Get record by id: {}", id);

        final SolrQuery query = queryFactory.createDocumentQuery(id);
        return queryToRecordFlux(query)
                .next();
    }

    @Override
    public Mono<VloRecord> saveRecord(VloRecord record) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Flux<Facet> getAllFacets(String queryParam, Map<String, ? extends Iterable<String>> filters, Optional<List<String>> facets, Optional<Integer> valueCount) {
        final SolrQuery query = queryFactory.createFacetQuery(queryParam,
                // empty list for default fields selection
                Optional.empty(),
                // get default number of top values
                valueCount.or(() -> Optional.of(queryFactory.getDefaultFacetValueCount())));

        applyFilterQuery(query, filters);

        return queryToResponseMono(query)
                .mapNotNull(QueryResponse::getFacetFields)
                .flatMapMany(Flux::fromIterable)
                .map(this::solrFacetFieldToFacet);
    }

    @Override
    public Mono<Facet> getFacet(String facet, String queryParam, Map<String, ? extends Iterable<String>> filters, Optional<Integer> valueCount) {
        //TODO: check if facet exists; otherwise return an empty mono!!
        
        // query to get all facet values for a specific facet
        final SolrQuery query = queryFactory.createFacetQuery(queryParam,
                // singleton facet fields list
                Optional.of(ImmutableList.of(facet)),
                // get maximum number of values
                valueCount.or(() -> Optional.of(queryFactory.getMaxFacetValueCount())));

        applyFilterQuery(query, filters);

        return queryToResponseMono(query)
                .mapNotNull(QueryResponse::getFacetFields)
                .flatMapMany(Flux::fromIterable)
                .next()
                .map(this::solrFacetFieldToFacet);
    }

    private void applyFilterQuery(final SolrQuery query, Map<String, ? extends Iterable<String>> filters) {
        if (filters != null) {
            filters.forEach((field, values) -> {
                values.forEach(value -> {
                    query.addFilterQuery(ClientUtils.escapeQueryChars(field) + ":" + ClientUtils.escapeQueryChars(value));
                });
            });
        }
    }

    private Facet solrFacetFieldToFacet(FacetField facetField) {
        final String fieldName = facetField.getName();
        return new Facet(fieldName, facetField.getValueCount(),
                FluentIterable.from(facetField.getValues())
                        .transform(c -> new Facet.ValeCount(
                        c.getName(),
                        fieldValueLabelService.getLabelFor(fieldName, c.getName()),
                        c.getCount()))
                        .toList()
        );
    }

    private Flux<VloRecord> queryToRecordFlux(final SolrQuery query) {
        return queryToResponseMono(query)
                // results (solr documents) iterable as flux
                .flatMapIterable(QueryResponse::getResults)
                // map solr documents to VloRecord pojos
                .map(recordConverter::convert);
    }

    private Mono<QueryResponse> queryToResponseMono(final SolrQuery query) {
        return Mono.just(query)
                .doOnNext(q -> log.debug("Query: {}", q))
                .map(this::fireQuery);
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
