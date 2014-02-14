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
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.service.SolrQueryFactory;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;

/**
 *
 * @author twagoo
 */
public class SolrQueryFactoryImpl implements SolrQueryFactory {

    private static final String SOLR_SEARCH_ALL = "*:*";
    private final SolrQuery countQuery;
    private final VloConfig config;

    public SolrQueryFactoryImpl(VloConfig config) {
        this.config = config;

        // create the query used to count facets (will never change)
        countQuery = getDefaultFacetQuery();
        countQuery.setRows(0);
    }

    @Override
    public SolrQuery createFacetQuery(List<FacetSelection> selections, String queryString) {
        SolrQuery query = getDefaultFacetQuery();

        if (queryString == null) {
            query.setQuery(SOLR_SEARCH_ALL);
        } else {
            query.setQuery(ClientUtils.escapeQueryChars(queryString));
        }

        if (selections != null) {
            final List<String> encodedQueries = new ArrayList(selections.size());
            for (FacetSelection selection : selections) {
                final String facetName = selection.getFacetName();
                final Collection<String> values = selection.getValue();
                if (values != null) {
                    for (String value : values) {
                        encodedQueries.add(String.format("%s:%s", facetName, ClientUtils.escapeQueryChars(value)));
                    }
                }
            }
            query.setFilterQueries(encodedQueries.toArray(new String[encodedQueries.size()]));
        }
        return query;
    }

    private SolrQuery getDefaultFacetQuery() {
        SolrQuery result = new SolrQuery();
        result.setRows(10);
        result.setStart(0);
        result.setFields(FacetConstants.FIELD_NAME, FacetConstants.FIELD_ID, FacetConstants.FIELD_DESCRIPTION);
        result.setFacet(true);
        result.setFacetMinCount(1);
        result.addFacetField(config.getFacetFields());
        return result;
    }

    @Override
    public synchronized SolrQuery createCountFacetsQuery() {
        return countQuery;
    }

}
