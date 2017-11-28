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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a query and any number of selected values for zero or more facets
 *
 * @author twagoo
 */
public class QueryFacetsSelection implements Serializable {

    private String queryString;
    private final Map<String, FacetSelection> selection;

    /**
     * Creates an empty selection (no string, no facet values)
     */
    public QueryFacetsSelection() {
        this(null, new LinkedHashMap<String, FacetSelection>());
    }

    /**
     * Creates a selection with an empty facet selection
     *
     * @param query query string
     */
    public QueryFacetsSelection(String query) {
        this(query, new LinkedHashMap<String, FacetSelection>());
    }

    /**
     * Creates a selection without a query
     *
     * @param selection facet values selection map
     */
    public QueryFacetsSelection(Map<String, FacetSelection> selection) {
        this(null, selection);
    }

    /**
     * Creates a selection with a textual query and facet value selection
     *
     * @param query textual query (can be null)
     * @param selection facet values selection map (can be null)
     */
    public QueryFacetsSelection(String query, Map<String, FacetSelection> selection) {
        this.queryString = query;
        if (selection == null) {
            this.selection = new LinkedHashMap<>();
        } else {
            this.selection = selection;
        }
    }

    /**
     *
     * @return a facet -> values map representing the current selection
     */
    public Map<String, FacetSelection> getSelection() {
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
    public FacetSelection getSelectionValues(String facet) {
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

    public void selectValues(String facet, FacetSelection values) {
        if (values == null || values.isEmpty()) {
            selection.remove(facet);
        } else {
            if (values instanceof Serializable) {
                selection.put(facet, values);
            } else {
                selection.put(facet, values);
            }
        }
    }

    public void addNewFacetValue(String facet, FacetSelectionType selectionType, Collection<String> values) {
        FacetSelection curSel = selection.get(facet);
        if (curSel != null) {
            curSel.setValues(values);
        } else {
            curSel = new FacetSelection(selectionType, values);
        }

        selectValues(facet, curSel);
    }

    public void removeFacetValue(String facet, Collection<String> valuestoBeRemoved) {
        FacetSelection curSel = selection.get(facet);
        if (curSel != null) {
            curSel.removeValues(valuestoBeRemoved);
        }
        //to remove facet from map if does not have any value
        selectValues(facet, curSel);
    }

    @Override
    public String toString() {
        return String.format("[QueryFacetSelection queryString = %s, selection = %s]", queryString, selection);
    }

    public QueryFacetsSelection copy() {
        final Map<String, FacetSelection> selectionClone = new LinkedHashMap<>(selection.size());
        for (Entry<String, FacetSelection> entry : selection.entrySet()) {
            selectionClone.put(entry.getKey(), entry.getValue().getCopy());
        }
        return new QueryFacetsSelection(queryString, selectionClone);
    }

}
