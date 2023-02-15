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
package eu.clarin.cmdi.vlo.api.service.solr;

import eu.clarin.cmdi.vlo.FacetConstants;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.util.ClientUtils;

import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author twagoo
 */
public class SolrDocumentQueryFactoryImpl {

    public static final String COLLAPSE_FIELD_QUERY = "{!collapse field=_signature}";

    protected static final String SOLR_SEARCH_ALL = "*:*";
    protected static final String EXPAND_ROWS = "0"; //expansion rows to actually fetch

    private final String ID = "id";

    /**
     * Template query for new document queries
     */
    private final SolrQuery defaultQueryTemplate;

    /**
     *
     * @param documentFields fields that should be included in document queries
     */
    public SolrDocumentQueryFactoryImpl(Collection<String> documentFields) {
        defaultQueryTemplate = new SolrQuery();
        defaultQueryTemplate.setFields(documentFields.toArray(new String[]{}));
    }

    public SolrQuery createDocumentQuery(int first, int count) {
        // make a query to get all documents that match the selection criteria
        final SolrQuery query = getDefaultDocumentQuery();

        // set offset and limit
        query.setStart(first);
        query.setRows(count);
        return query;
    }

    public SolrQuery createSortedDocumentQuery(String sortField, String sortDirection, int first, int count) {
        // make a query to get all documents that match the selection criteria
        final SolrQuery query = getDefaultDocumentQuery();
        // apply field Sorting
        query.addSort(sortField, ORDER.valueOf(sortDirection.toLowerCase()));
        // set offset and limit
        query.setStart(first);
        query.setRows(count);
        return query;
    }

    public SolrQuery createExpandedDocumentQuery(int first, int count) {
        // make a query to get all documents that match the selection criteria
        final SolrQuery query = getDefaultDocumentQuery();
        // we use the 'fast' request handler here to avoid collapsing (assume ranking is not of interest)
        query.setRequestHandler(FacetConstants.SOLR_REQUEST_HANDLER_FAST);
        // set offset and limit
        query.setStart(first);
        query.setRows(count);
        return query;
    }

    public SolrQuery createDocumentQueryWithExpansion(int first, int count) {
        final SolrQuery query = createDocumentQuery(first, count);
        return enableExpansion(query);
    }

    public SolrQuery createDocumentQuery(String docId) {
        // make a query to look up a specific document by its ID
        final SolrQuery query = getDefaultDocumentQuery();
        // we can use the 'fast' request handler here, document ranking is of no interest
        query.setRequestHandler(FacetConstants.SOLR_REQUEST_HANDLER_FAST);
        // consider all documents
        query.setQuery(SOLR_SEARCH_ALL);
        // filter by ID
        query.addFilterQuery(createFilterQuery(ID, docId));

        // one result max
        query.setRows(1);
        return query;
    }

    public SolrQuery createDuplicateDocumentsQuery(String docId, String collapseField, String collapseValue, int offset, int expansionLimit) {
        // make a query to look up a specific document by its ID
        SolrQuery query = getDefaultDocumentQuery()
                // we can use the 'fast' request handler here, document ranking is of no interest
                .setRequestHandler(FacetConstants.SOLR_REQUEST_HANDLER_FAST)
                // consider all documents
                .setQuery(SOLR_SEARCH_ALL)
                // limit to matching signature
                .addFilterQuery(createFilterQuery(collapseField, collapseValue))
                // exclude target document
                .addFilterQuery(createNegativeFilterQuery(ID, docId))
                .setStart(offset)
                .setRows(expansionLimit);
        return query;

    }

    public SolrQuery createSimilarDocumentsQuery(String docId) {
        final SolrQuery query = new SolrQuery(String.format("%s:\"%s\"", ID, ClientUtils.escapeQueryChars(docId)));
        query.setRequestHandler("/mlt");
        return query;
    }

    private SolrQuery getDefaultDocumentQuery() {
        return defaultQueryTemplate.getCopy();
    }

    protected String createFilterQuery(String facetName, String value) {
        // escape value and wrap in quotes to make literal query
        return createFilterQuery("%s:\"%s\"", facetName, value);
    }

    protected String createNegativeFilterQuery(String facetName, String value) {
        // escape value and wrap in quotes to make literal query, prepend negator
        return createFilterQuery("-%s:\"%s\"", facetName, value);
    }

    private String createFilterQuery(String _formatString, String facetName, String value) {
        return String.format(_formatString, facetName, ClientUtils.escapeQueryChars(value));
    }

    /**
     * Creates an OR filter query over the provided facet/value pairs (a query
     * that requests all records matching ANY of the facet/value pairs)
     *
     * @param facetValues map with facet/value pairs that should be matched
     * @return
     */
    protected final String createFilterOrQuery(Map<String, String> facetValues) {
        return facetValues.entrySet()
                .stream()
                .map(e -> createFilterQuery(e.getKey(), e.getValue()))
                .collect(Collectors.joining(" OR "));
    }

    /**
     * Creates an OR filter query for a single facet for a number of values
     *
     * @param facetName facet that should be matched
     * @param values allowed values
     * @return
     */
    private String createFacetOrQuery(String facetName, Collection<String> values) {
        assert (!values.isEmpty());

        // escape value and wrap in quotes to make literal query
        // prefix field name with tag statement (see <http://wiki.apache.org/solr/SimpleFacetParameters#Multi-Select_Faceting_and_LocalParams>)
        final String prefix = String.format("{!tag=%1$s}%1$s:(", facetName);
        //close parentheses
        final String postfix = ")";

        // escape and join 
        return values.stream()
                .map(ClientUtils::escapeQueryChars)
                .collect(Collectors.joining(" OR ", prefix, postfix));
    }

    protected SolrQuery enableExpansion(SolrQuery query) {
        query.set("expand", true);
        query.set("expand.rows=" + EXPAND_ROWS);
        return query;
    }
}
