/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.vlo.service.solr.impl;

import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.SearchResultsDao;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionList;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionPair;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentQueryFactory;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class SolrDocumentServiceImpl implements SolrDocumentService {

    private final static Logger logger = LoggerFactory.getLogger(SolrDocumentServiceImpl.class);
    private final SearchResultsDao searchResultsDao;
    private final SolrDocumentQueryFactory queryFactory;

    public SolrDocumentServiceImpl(SearchResultsDao searchResultsDao, SolrDocumentQueryFactory queryFactory) {
        this.searchResultsDao = searchResultsDao;
        this.queryFactory = queryFactory;
    }

    @Override
    public SolrDocument getDocument(String docId) {
        final SolrQuery query = queryFactory.createDocumentQuery(docId);
        final SolrDocumentList result = searchResultsDao.getDocuments(query);
        if (result.size() < 1) {
            return null;
        } else {
            logger.debug("Document with docId {} retrieved:", result);
            return result.get(0);
        }
    }

    @Override
    public SolrDocumentExpansionPair getDocumentWithExpansion(String docId, String collapseField, int expansionCount) {
        final SolrDocument doc = getDocument(docId);
        if (doc == null) {
            return null;
        } else {
            final String signature = doc.getFieldValue(collapseField).toString();
            final SolrQuery duplicatesQuery = queryFactory.createDuplicateDocumentsQuery(docId, collapseField, signature, expansionCount);
            final QueryResponse duplicatesQueryResponse = searchResultsDao.getQueryResponse(duplicatesQuery);
            final SolrDocumentList result = duplicatesQueryResponse.getResults();
            final Map<String, SolrDocumentList> expansionMap;
            if (result == null) {
                expansionMap = ImmutableMap.of();
            } else {
                expansionMap = ImmutableMap.of(signature, result);
            }

            return new SolrDocumentExpansionPairImpl(doc, expansionMap, collapseField);
        }
    }

    @Override
    public List<SolrDocument> getDocuments(QueryFacetsSelection selection, int first, int count) {
        final SolrQuery query = queryFactory.createDocumentQuery(selection, first, count);
        return searchResultsDao.getDocuments(query);
    }

    @Override
    public SolrDocumentExpansionList getDocumentsWithExpansion(QueryFacetsSelection selection, int first, int count, String collapseField) {
        final SolrQuery query = queryFactory.createDocumentQueryWithExpansion(selection, first, count);
        final QueryResponse queryResponse = searchResultsDao.getQueryResponse(query);
        if (queryResponse.getResults() == null) {
            return SolrDocumentExpansionListImpl.empty();
        }
        return new SolrDocumentExpansionListImpl(queryResponse, collapseField);
    }

    @Override
    public long getDocumentCount(QueryFacetsSelection selection) {
        final SolrQuery query = queryFactory.createDocumentQuery(selection, 0, 0);
        return searchResultsDao.getDocuments(query).getNumFound();
    }

}
