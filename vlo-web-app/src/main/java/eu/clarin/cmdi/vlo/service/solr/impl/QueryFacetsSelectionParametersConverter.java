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

import com.google.common.collect.Maps;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

/**
 * Page parameter conversion service for {@link QueryFacetsSelection}
 *
 * @author twagoo
 */
public class QueryFacetsSelectionParametersConverter implements PageParametersConverter<QueryFacetsSelection> {

    @Override
    public QueryFacetsSelection fromParameters(PageParameters params) {
        // Assuming AND. TODO: decode NOT,OR,not empty. Abandon multimap stategy?
        // Get query string from params
        final String query = params.get("q").toOptionalString();

        // Get facet selections from params
        final List<StringValue> facetValues = params.getValues("fq");

        final HashMap<String, FacetSelection> selection = Maps.newHashMapWithExpectedSize(facetValues.size());
        for (StringValue facetValue : facetValues) {
            if (!facetValue.isEmpty()) {
                String[] fq = facetValue.toString().split(":");
                if (fq.length == 2) {
                    // we have a facet - value pair
                    final String facet = fq[0];
                    final String value = fq[1];
                    if (selection.containsKey(facet)) {
                        selection.get(facet).getValues().add(value);
                    } else {
                        selection.put(facet, new FacetSelection(Arrays.asList(value)));
                    }
                }
            }
        }

        return new QueryFacetsSelection(query, selection);
    }

    @Override
    public PageParameters toParameters(QueryFacetsSelection selection) {
        final PageParameters params = new PageParameters();

        // put the query in the 'q' parameter
        final String query = selection.getQuery();
        if (query != null) {
            params.add("q", query);
        }

        // put all selections in 'fq' parameters
        for (Entry<String, FacetSelection> facetSelection : selection.getSelection().entrySet()) {
            //Assuming AND            
            //TODO: encode NOT,OR
            for (String value : facetSelection.getValue().getValues()) {
                params.add("fq", String.format("%s:%s", facetSelection.getKey(), value));
            }
        }

        return params;
    }
}
