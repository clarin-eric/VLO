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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import java.io.Serializable;
import java.util.Collection;
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
        // Get query string from params
        final String query = params.get("q").toOptionalString();

        // Get facet selections from params
        final List<StringValue> facetValues = params.getValues("fq");
        // Store in a multimap to allow for multiple selections per facet
        final Multimap<String, String> selectionMap = HashMultimap.<String, String>create(facetValues.size(), 1);
        for (StringValue facetValue : facetValues) {
            if (!facetValue.isEmpty()) {
                String[] fq = facetValue.toString().split(":");
                if (fq.length == 2) {
                    // we have a facet - value pair
                    selectionMap.put(fq[0], fq[1]);
                }
            }
        }

        // Facet selection expects a mutable and serializable map, so first convert
        // back to ordinary map, then insert serializable values
        final HashMap<String, Collection<String>> selection = multimapToSerializableCollectionMap(selectionMap);

        // Facet selection expects a mutable and serializable map, so first convert 
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
        for (Entry<String, Collection<String>> facetSelection : selection.getSelection().entrySet()) {
            for (String value : facetSelection.getValue()) {
                params.add("fq", String.format("%s:%s", facetSelection.getKey(), value));
            }
        }

        return params;
    }

    /**
     *
     * @param selectionMap multimap holding the selection
     * @return a fully serializable map with collection values
     */
    private HashMap<String, Collection<String>> multimapToSerializableCollectionMap(final Multimap<String, String> selectionMap) {
        final HashMap<String, Collection<String>> selection = Maps.newHashMapWithExpectedSize(selectionMap.size());
        for (Entry<String, Collection<String>> entry : selectionMap.asMap().entrySet()) {
            final Collection<String> value = entry.getValue();
            if (value instanceof Serializable) {
                // keep serializable collection value
                selection.put(entry.getKey(), value);
            } else {
                // copy to a serializable collection
                selection.put(entry.getKey(), Lists.newArrayList(value));
            }
        }
        return selection;
    }
}
