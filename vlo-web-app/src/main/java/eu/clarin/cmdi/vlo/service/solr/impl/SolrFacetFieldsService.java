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

import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.SearchResultsDao;
import eu.clarin.cmdi.vlo.service.solr.SolrFacetQueryFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import eu.clarin.cmdi.vlo.FieldKey;

/**
 * Gets FacetFields from SOLR based on a selection and the queries constructed
 * by the provided query factory
 *
 * @author twagoo
 */
public class SolrFacetFieldsService implements FacetFieldsService {
    @Inject
    private FieldNameService fieldNameService;
    private final SearchResultsDao searchResultsDao;
    private final SolrFacetQueryFactory queryFatory;

    /**
     *
     * @param searchResultsDao DAO to use to retrieve facets
     * @param queryFatory factory to use to construct facet queries
     */
    public SolrFacetFieldsService(SearchResultsDao searchResultsDao, SolrFacetQueryFactory queryFatory) {
        this.searchResultsDao = searchResultsDao;
        this.queryFatory = queryFatory;
    }

    @Override
    public List<FacetField> getFacetFields(QueryFacetsSelection selection, List<String> facets, int valueLimit) {
        return removeSelectedValsFromResponse(selection, searchResultsDao.getFacets(queryFatory.createFacetQuery(selection, facets, valueLimit)));
    }

    @Override
    public long getFacetFieldCount(List<String> facets) {
        return (long) searchResultsDao.getFacets(queryFatory.createCountFacetsQuery(facets)).size();
    }

    private List<FacetField> removeSelectedValsFromResponse(QueryFacetsSelection query, List<FacetField> response) {
        List<FacetField> filteredFacets = new ArrayList<>();

        //for each facet from response
        for (FacetField facet : response) {
            FacetSelection facetSelection = query.getSelectionValues(facet.getName());

            if (facetSelection == null || facet.getName().equals(fieldNameService.getFieldName(FieldKey.LICENSE_TYPE))) {
                filteredFacets.add(facet);
                continue;
            } else {
                Collection<String> selectedValues = facetSelection.getValues();

                FacetField _newFacetField = new FacetField(facet.getName());
                //for each value from facet check if is selected and if not add it to the new response
                for (Count val : facet.getValues()) {
                    if (!selectedValues.contains(val.getName())) {
                        _newFacetField.add(val.getName(), val.getCount());
                    }
                }

                filteredFacets.add(_newFacetField);
            }
        }

        return filteredFacets;
    }

}
