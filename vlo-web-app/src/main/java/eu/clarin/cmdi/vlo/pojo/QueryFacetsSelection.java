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
package eu.clarin.cmdi.vlo.pojo;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a query and any number of selected values for zero or more facets
 *
 * @author twagoo
 */
public class QueryFacetsSelection implements Serializable {

    private String queryString;
    private final Map<String, Collection<String>> selection;

    /**
     * Creates an empty selection (no string, no facet values)
     */
    public QueryFacetsSelection() {
        this(null, Collections.<String, Collection<String>>emptyMap());
    }

    /**
     * Creates a selection without a query
     *
     * @param selection facet values selection map
     */
    public QueryFacetsSelection(Map<String, Collection<String>> selection) {
        this(null, selection);
    }

    /**
     * Creates a selection with a textual query and facet value selection
     *
     * @param query textual query (can be null)
     * @param selection facet values selection map (can be null)
     */
    public QueryFacetsSelection(String query, Map<String, Collection<String>> selection) {
        this.queryString = query;
        if (selection == null) {
            this.selection = new HashMap<String, Collection<String>>();
        } else {
            this.selection = selection;
        }
    }

    /**
     *
     * @return a facet -> values map representing the current selection
     */
    public Map<String, Collection<String>> getSelection() {
        return selection;
    }

    /**
     *
     * @return the facets present in the current selection
     */
    public Collection<String> getFacets() {
        return selection.keySet();
    }

    /**
     *
     * @param facet facet to get values for
     * @return the selected values for the specified facet. Can be null.
     */
    public Collection<String> getSelectionValues(String facet) {
        return selection.get(facet);
    }

    /**
     *
     * @return the current textual query, may be null in case of no query
     */
    public String getQuery() {
        return queryString;
    }

    public void setQuery(String queryString) {
        this.queryString = queryString;
    }

    public void selectValues(String facet, Collection<String> values) {
        selection.put(facet, values);
    }

    @Override
    public String toString() {
        return String.format("[QueryFacetSelection queryString = %s, selection = %s]", queryString, selection);
    }

}
