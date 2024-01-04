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
package eu.clarin.cmdi.vlo.api.solr;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.util.ClientUtils;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author twagoo
 */
public class SolrDocumentQueryFactoryImpl {

    public static final String COLLAPSE_FIELD_QUERY = "{!collapse field=_signature}";
    public static final String SOLR_REQUEST_HANDLER_FAST = "/fast";

    protected static final String SOLR_SEARCH_ALL = "*:*";
    protected static final String EXPAND_ROWS = "0"; //expansion rows to actually fetch

    private final String ID = "id";

    private static final int FACET_LIMIT_DEFAULT = 10;
    private static final int FACET_LIMIT_MAX = 10_0000;

    private static final String[] FACET_FIELDS_DEFAULT = {
        "languageCode",
        "collection",
        "resourceClass",
        "modality",
        "format",
        "keywords",
        "genre",
        "subject",
        "country",
        "organisation"
    };

    /**
     * Template query for new document queries
     */
    private final SolrQuery fullQueryTemplate;

    private final SolrQuery minimalQueryTemplate;

    /**
     *
     * @param minimalDocumentFields fields that should be included for all
     * document queries
     * @param extraDocumentFields fields that should be included for full
     * document queries
     */
    public SolrDocumentQueryFactoryImpl(Collection<String> minimalDocumentFields, Collection<String> extraDocumentFields) {
        minimalQueryTemplate = new SolrQuery();
        minimalQueryTemplate.setFields(FluentIterable.from(minimalDocumentFields).toArray(String.class));

        fullQueryTemplate = new SolrQuery();
        fullQueryTemplate.setFields(FluentIterable.concat(minimalDocumentFields, extraDocumentFields).toArray(String.class));
    }

    /**
     *
     * @param start 0 based index of first item to include from results
     * @param rows
     * @return
     */
    public SolrQuery createDocumentQuery(int start, int rows) {
        // make a query to get all documents that match the selection criteria
        final SolrQuery query = getFullDocumentQueryTemplate();

        // set start record and result size limit
        query.setStart(Math.max(0, start));
        query.setRows(Math.max(0, rows));
        return query;
    }

    /**
     *
     * @param start 0 based index of first item to include from results
     * @param rows
     * @return
     */
    public SolrQuery createLimitedDocumentQuery(int start, int rows) {
        // make a query to get all documents that match the selection criteria
        final SolrQuery query = getMinimalDocumentQueryTemplate();

        // set start record and result size limit
        query.setStart(Math.max(0, start));
        query.setRows(Math.max(0, rows));
        return query;
    }

    public SolrQuery createSortedDocumentQuery(String sortField, String sortDirection, int start, int rows) {
        // make a query to get all documents that match the selection criteria
        final SolrQuery query = getFullDocumentQueryTemplate();
        // apply field Sorting
        query.addSort(sortField, ORDER.valueOf(sortDirection.toLowerCase()));

        // set start record and result size limit
        query.setStart(start);
        query.setRows(rows);
        return query;
    }

    public SolrQuery createExpandedDocumentQuery(int start, int rows) {
        // make a query to get all documents that match the selection criteria
        final SolrQuery query = getFullDocumentQueryTemplate();
        // we use the 'fast' request handler here to avoid collapsing (assume ranking is not of interest)
        query.setRequestHandler(SOLR_REQUEST_HANDLER_FAST);
        // set offset and limit
        query.setStart(start);
        query.setRows(rows);
        return query;
    }

    public SolrQuery createDocumentQueryWithExpansion(int start, int rows) {
        final SolrQuery query = createDocumentQuery(start, rows);
        return enableExpansion(query);
    }

    public SolrQuery createDocumentQuery(String docId) {
        // make a query to look up a specific document by its ID
        final SolrQuery query = getFullDocumentQueryTemplate();
        // we can use the 'fast' request handler here, document ranking is of no interest
        query.setRequestHandler(SOLR_REQUEST_HANDLER_FAST);
        // consider all documents
        query.setQuery(SOLR_SEARCH_ALL);
        // filter by ID
        query.addFilterQuery(createFilterQuery(ID, docId));

        // one result max
        query.setRows(1);
        return query;
    }

    public SolrQuery createDuplicateDocumentsQuery(String docId, String collapseField, String collapseValue, int start, int expansionLimit) {
        // make a query to look up a specific document by its ID
        SolrQuery query = getFullDocumentQueryTemplate()
                // we can use the 'fast' request handler here, document ranking is of no interest
                .setRequestHandler(SOLR_REQUEST_HANDLER_FAST)
                // consider all documents
                .setQuery(SOLR_SEARCH_ALL)
                // limit to matching signature
                .addFilterQuery(createFilterQuery(collapseField, collapseValue))
                // exclude target document
                .addFilterQuery(createNegativeFilterQuery(ID, docId))
                .setStart(start)
                .setRows(expansionLimit);
        return query;

    }

    public SolrQuery createSimilarDocumentsQuery(String docId) {
        final SolrQuery query = new SolrQuery(String.format("%s:\"%s\"", ID, ClientUtils.escapeQueryChars(docId)));
        query.setRequestHandler("/mlt");
        return query;
    }

    public SolrQuery createFacetQuery(String queryParam, Optional<List<String>> facetFields, Optional<Integer> valueLimit) {
        final String[] facetFieldsArray = facetFields.map(l -> l.toArray(String[]::new)).orElse(FACET_FIELDS_DEFAULT);
        return getMinimalDocumentQueryTemplate()
                .setRows(0)
                .setQuery(queryParam)
                .setFacet(true)
                .setFacetLimit(valueLimit.orElse(FACET_LIMIT_DEFAULT))
                .setFacetMinCount(1)
                .addFacetField(facetFieldsArray);
    }

    public int getDefaultFacetValueCount() {
        return FACET_LIMIT_DEFAULT;
    }

    public int getMaxFacetValueCount() {
        return FACET_LIMIT_MAX;
    }

    private SolrQuery getFullDocumentQueryTemplate() {
        return fullQueryTemplate.getCopy();
    }

    private SolrQuery getMinimalDocumentQueryTemplate() {
        return minimalQueryTemplate.getCopy();
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
