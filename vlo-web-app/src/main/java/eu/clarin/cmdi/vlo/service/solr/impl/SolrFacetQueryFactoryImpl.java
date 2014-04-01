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

import eu.clarin.cmdi.vlo.service.solr.SolrFacetQueryFactory;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;

/**
 * Implements a SOLR query factory, to be used by SOLR service implementation
 * for the VLO
 *
 * @author twagoo
 */
public class SolrFacetQueryFactoryImpl extends AbstractSolrQueryFactory implements SolrFacetQueryFactory {
    
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
    public SolrQuery createFacetQuery(QueryFacetsSelection queryFacetsSelections, int facetValueLimit) {
        final SolrQuery query = getDefaultFacetQuery();
        addQueryFacetParameters(query, queryFacetsSelections);
        query.setFacetLimit(facetValueLimit); 
        return query;
    }
    
    private SolrQuery getDefaultFacetQuery() {
        SolrQuery query = new SolrQuery();
        query.setRows(0);
        query.setFacet(true);
        query.setFacetMinCount(1);
        query.addFacetField(facets);
        return query;
    }
    
    @Override
    public synchronized SolrQuery createCountFacetsQuery() {
        return facetCountQuery;
    }
    
}
