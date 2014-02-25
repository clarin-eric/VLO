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
package eu.clarin.cmdi.vlo.service.impl;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.service.SolrFacetQueryFactory;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;

/**
 * Implements a SOLR query factory, to be used by SOLR service implementation
 * for the VLO
 *
 * @author twagoo
 */
public class SolrFacetQueryFactoryImpl implements SolrFacetQueryFactory {
    
    private static final String SOLR_SEARCH_ALL = "*:*";
    private final SolrQuery facetCountQuery;
    private final String[] facets;
    
    /**
     * 
     * @param facets names of facets to include in query
     */
    public SolrFacetQueryFactoryImpl(List<String> facets) {
        this.facets = facets.toArray(new String[facets.size()]);

        // create the query used to count facets (will never change)
        facetCountQuery = getDefaultFacetQuery();
        facetCountQuery.setRows(0);
    }
    
    @Override
    public SolrQuery createFacetQuery(QueryFacetsSelection queryFacetsSelections) {
        final SolrQuery query = getDefaultFacetQuery();
        addQueryFacetParameters(query, queryFacetsSelections);
        return query;
    }
    
    @Override
    public SolrQuery createDocumentQuery(QueryFacetsSelection selection, int first, int count) {
        final SolrQuery query = getDefaultDocumentQuery();
        addQueryFacetParameters(query, selection);
        query.setStart(first);
        query.setRows(count);
        return query;
    }
    
    protected void addQueryFacetParameters(final SolrQuery query, QueryFacetsSelection queryFacetsSelections) {
        final String queryString = queryFacetsSelections.getQuery();
        
        if (queryString == null) {
            query.setQuery(SOLR_SEARCH_ALL);
        } else {
            query.setQuery(ClientUtils.escapeQueryChars(queryString));
        }
        
        final Map<String, Collection<String>> selections = queryFacetsSelections.getSelection();
        if (selections != null) {
            final List<String> encodedQueries = new ArrayList(selections.size()); // assuming every facet has one selection, most common scenario
            for (Map.Entry<String, Collection<String>> selection : selections.entrySet()) {
                final String facetName = selection.getKey();
                final Collection<String> values = selection.getValue();
                if (values != null) {
                    for (String value : values) {
                        encodedQueries.add(String.format("%s:%s", facetName, ClientUtils.escapeQueryChars(value)));
                    }
                }
            }
            query.setFilterQueries(encodedQueries.toArray(new String[encodedQueries.size()]));
        }
    }
    
    private SolrQuery getDefaultFacetQuery() {
        SolrQuery query = new SolrQuery();
        query.setRows(0);
        query.setFacet(true);
        query.setFacetMinCount(1);
        query.addFacetField(facets);
        return query;
    }
    
    private SolrQuery getDefaultDocumentQuery() {
        SolrQuery query = new SolrQuery();
        query.setFields(FacetConstants.FIELD_NAME, FacetConstants.FIELD_ID, FacetConstants.FIELD_DESCRIPTION, FacetConstants.FIELD_COLLECTION, FacetConstants.FIELD_RESOURCE);
        query.setSort(SolrQuery.SortClause.asc(FacetConstants.FIELD_NAME));
        return query;
    }
    
    @Override
    public synchronized SolrQuery createCountFacetsQuery() {
        return facetCountQuery;
    }
    
}
