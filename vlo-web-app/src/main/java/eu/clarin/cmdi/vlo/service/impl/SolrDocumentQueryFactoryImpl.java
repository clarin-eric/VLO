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
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.SolrDocumentQueryFactory;
import org.apache.solr.client.solrj.SolrQuery;

/**
 *
 * @author twagoo
 */
public class SolrDocumentQueryFactoryImpl extends AbstractSolrQueryFactory implements SolrDocumentQueryFactory{
    
    @Override
    public SolrQuery createDocumentQuery(QueryFacetsSelection selection, int first, int count) {
        final SolrQuery query = getDefaultDocumentQuery();
        addQueryFacetParameters(query, selection);
        query.setStart(first);
        query.setRows(count);
        return query;
    }
    
    private SolrQuery getDefaultDocumentQuery() {
        SolrQuery query = new SolrQuery();
        query.setFields(FacetConstants.FIELD_NAME, FacetConstants.FIELD_ID, FacetConstants.FIELD_DESCRIPTION, FacetConstants.FIELD_COLLECTION, FacetConstants.FIELD_RESOURCE);
        query.setSort(SolrQuery.SortClause.asc(FacetConstants.FIELD_NAME));
        return query;
    }
}
