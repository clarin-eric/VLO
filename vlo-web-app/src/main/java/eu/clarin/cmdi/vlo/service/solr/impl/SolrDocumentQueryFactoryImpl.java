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
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentQueryFactory;
import java.util.Collection;

import javax.inject.Inject;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;

import eu.clarin.cmdi.vlo.FieldKey;


/**
 *
 * @author twagoo
 */
public class SolrDocumentQueryFactoryImpl extends AbstractSolrQueryFactory implements SolrDocumentQueryFactory {
    
    private final String ID;
    private final String SELF_LINK;
    

    /**
     * Template query for new document queries
     */
    private final SolrQuery defaultQueryTemplate;

    /**
     *
     * @param documentFields fields that should be included in document queries
     */
    public SolrDocumentQueryFactoryImpl(Collection<String> documentFields, FieldNameService fieldNameService) {
        this.ID = fieldNameService.getFieldName(FieldKey.ID);
        this.SELF_LINK = fieldNameService.getFieldName(FieldKey.SELF_LINK);
        defaultQueryTemplate = new SolrQuery();
        defaultQueryTemplate.setFields(documentFields.toArray(new String[]{}));
//        //TODO: qf (all fields with weights - make configurable (later)
//        defaultQueryTemplate.setParam(DisMaxParams.QF, "name^20 description^10");
    }

    @Override
    public SolrQuery createDocumentQuery(QueryFacetsSelection selection, int first, int count) {
        // make a query to get all documents that match the selection criteria
        final SolrQuery query = getDefaultDocumentQuery();
        // apply selection
        addQueryFacetParameters(query, selection);
        // set offset and limit
        query.setStart(first);
        query.setRows(count);
        return query;
    }

    @Override
    public SolrQuery createDocumentQuery(String docId) {
        // make a query to look up a specific document by its ID
        final SolrQuery query = getDefaultDocumentQuery();
        // we can use the 'fast' request handler here, document ranking is of no interest
        query.setRequestHandler(FacetConstants.SOLR_REQUEST_HANDLER_FAST);
        // consider all documents
        query.setQuery(SOLR_SEARCH_ALL);
        // filter by ID
        // check for ID value in both 'id' and 'self link' fields, both ought to
        // be unique and self link use to be ID in old VLO, so this should keep old
        // URL's valid with a minimal likelihood of clashes
        final ImmutableMap<String, String> idOrQueryMap = ImmutableMap.<String, String>builder()
                .put(ID, docId)
                .put(SELF_LINK, docId)
                .build();
        query.addFilterQuery(createFilterOrQuery(idOrQueryMap));

        // one result max
        query.setRows(1);
        return query;
    }

    @Override
    public SolrQuery createSimilarDocumentsQuery(String docId) {
        final SolrQuery query = new SolrQuery(String.format("%s:\"%s\"", ID, ClientUtils.escapeQueryChars(docId)));
        query.setRequestHandler("/mlt");
        return query;
    }

    private SolrQuery getDefaultDocumentQuery() {
        return defaultQueryTemplate.getCopy();
    }
}
