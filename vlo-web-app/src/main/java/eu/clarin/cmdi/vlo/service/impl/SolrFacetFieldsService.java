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

import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.service.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.SearchResultsDao;
import eu.clarin.cmdi.vlo.service.SolrQueryFactory;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;

/**
 *
 * @author twagoo
 */
public class SolrFacetFieldsService implements FacetFieldsService {

    private final SearchResultsDao searchResultsDao;
    private final SolrQueryFactory queryFatory;

    public SolrFacetFieldsService(SearchResultsDao searchResultsDao, SolrQueryFactory queryFatory) {
        this.searchResultsDao = searchResultsDao;
        this.queryFatory = queryFatory;
    }

    @Override
    public List<FacetField> getFacetFields(List<FacetSelection> selection) {
        return searchResultsDao.getFacets(queryFatory.createQuery(selection));
    }

    @Override
    public long getFacetFieldCount() {
        return (long) searchResultsDao.getFacets(COUNT_FACETS_QUERY).size(); //TODO: count facets
    }
    public static final SolrQuery COUNT_FACETS_QUERY = new SolrQuery("");

}
