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

import eu.clarin.cmdi.vlo.api.service.ReactiveVloRecordService;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import java.io.IOException;
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
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
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
    public Mono<Long> getRecordCount(String query) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Mono<VloRecord> getRecordById(String id) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Flux<VloRecord> getRecords(Optional<String> queryParam, int offset, int size) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Mono<VloRecord> saveRecord(VloRecord record) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private SolrDocumentList getDocuments(SolrQuery query) {
        QueryResponse queryResponse = fireQuery(query);
        final SolrDocumentList documents = queryResponse.getResults();
        if (documents != null) {
            log.debug("Found {} documents", documents.getNumFound());
            return documents;
        } else {
            log.warn("Null result for query {}", query);
            return new SolrDocumentList();
        }
    }

    private QueryResponse getQueryResponse(SolrQuery query) {
        return fireQuery(query);
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
