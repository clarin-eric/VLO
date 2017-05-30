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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents the selection for a single facet
 *
 * @author twagoo
 */
public class FacetSelection implements Serializable {

    private final FacetSelectionType selectionType;
    private Collection<String> values;
    private final Map<String, FacetSelectionValueQualifier> qualifiers;

    /**
     * Creates an empty selection with the specified type
     *
     * @param type
     */
    public FacetSelection(FacetSelectionType type) {
        this(type, Lists.<String>newArrayList());
    }

    public FacetSelection(FacetSelectionType selectionType, Collection<String> values) {
        this(selectionType, values, Maps.<String, FacetSelectionValueQualifier>newHashMap());
    }

    public FacetSelection(FacetSelectionType selectionType, Collection<String> values, Map<String, FacetSelectionValueQualifier> qualifiers) {

        this.selectionType = selectionType;

        // always store as array list, which is modifiable and serialisable
        if (values instanceof ArrayList) {
            this.values = values;
        } else {
            // copy to new list
            this.values = Lists.newArrayList(values);
        }
        if (qualifiers instanceof HashMap) {
            this.qualifiers = qualifiers;
        } else {
            //copy to new hashmap
            this.qualifiers = Maps.newHashMap(qualifiers);
        }
    }

    /**
     *
     * @return type of selection
     */
    public FacetSelectionType getSelectionType() {
        return selectionType;
    }

    /**
     *
     * @return values subject to selection type
     */
    public Collection<String> getValues() {
        return values;
    }

    public void setValues(Collection<String> values) {
        //this.values = values;
        this.values.addAll(values);
    }

    /**
     * Sets a qualifier for one value within this facet selection, allow for
     * negation
     *
     * @param value value to qualify
     * @param qualifier qualifier for this value
     */
    public void setQualifier(String value, FacetSelectionValueQualifier qualifier) {
        qualifiers.put(value, qualifier);
    }

    /**
     * Gets the qualifier (such as
     * {@link FacetSelectionValueQualifier#NOT negation}) for one value within
     * this facet selection, can be null
     *
     * @param value value to get qualifier for
     * @return qualifier for this value
     */
    public FacetSelectionValueQualifier getQualifier(String value) {
        return qualifiers.get(value);
    }

    public void removeValues(Collection<String> valuesToBeRemoved) {
        if (valuesToBeRemoved != null) {
            for (String val : valuesToBeRemoved) {
                this.values.remove(val);
                this.qualifiers.remove(val);
            }
        }
    }

    /**
     * Adds a value to the selection, optionally with qualifier
     *
     * @param value value to add to selection
     * @param qualifier qualifier for this value, if null any existing qualifier
     * for this value is removed
     */
    public void addValue(String value, FacetSelectionValueQualifier qualifier) {
        if (!values.contains(value)) {
            values.add(value);
        }
        if (qualifier == null) {
            qualifiers.remove(value);
        } else {
            qualifiers.put(value, qualifier);
        }
    }

    public FacetSelection getCopy() {
        return new FacetSelection(selectionType, new ArrayList<>(values), new HashMap<>(qualifiers));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.selectionType != null ? this.selectionType.hashCode() : 0);
        hash = 79 * hash + (this.values != null ? this.values.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FacetSelection other = (FacetSelection) obj;
        if (this.selectionType != other.selectionType) {
            return false;
        }
        if (this.values != other.values && (this.values == null || !this.values.equals(other.values))) {
            return false;
        }
        return true;
    }

    /**
     *
     * @return whether this instance represents an actual selection
     */
    public boolean isEmpty() {
        return selectionType != FacetSelectionType.NOT_EMPTY // 'not empty' does not require any value, other types do
                && (values == null || values.isEmpty());
    }

    @Override
    public String toString() {
        return String.format("%s: %s", selectionType, values);
    }

}
