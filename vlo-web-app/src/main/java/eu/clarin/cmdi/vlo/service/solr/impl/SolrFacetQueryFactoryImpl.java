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

    private final SolrQuery baseQuery;

    /**
     *
     */
    public SolrFacetQueryFactoryImpl() {
        // create the base query (copied on each request to create new queries)
        baseQuery = new SolrQuery();
        baseQuery.setRows(0);
        baseQuery.setFacet(true);
        baseQuery.setFacetMinCount(1);
    }

    @Override
    public SolrQuery createFacetQuery(QueryFacetsSelection queryFacetsSelections, List<String> facets, int facetValueLimit) {
        final SolrQuery query = getBaseQuery(facets);
        addQueryFacetParameters(query, queryFacetsSelections);
        query.setFacetLimit(facetValueLimit);
        return query;
    }

    @Override
    public synchronized SolrQuery createCountFacetsQuery(List<String> facets) {
        final SolrQuery query = getBaseQuery(facets);
        query.setFacetLimit(0);
        return query;
    }

    private SolrQuery getBaseQuery(List<String> facets) {
        SolrQuery query = baseQuery.getCopy();
        query.addFacetField(facets.toArray(new String[facets.size()]));
        return query;
    }

}
