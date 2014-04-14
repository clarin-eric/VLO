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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author twagoo
 */
public class FacetSelection implements Serializable {

    private final FacetSelectionType selectionType;
    private final Collection<String> values;

    public FacetSelection(Collection<String> values) {
        this(FacetSelectionType.AND, values);
    }

    public FacetSelection(FacetSelectionType type) {
        this(type, Lists.<String>newArrayList());
    }

    public FacetSelection(FacetSelectionType selectionType, Collection<String> values) {
        this.selectionType = selectionType;
        // always store as array list, which is modifiable and serialisable
        if (values instanceof ArrayList) {
            this.values = values;
        } else {
            // copy to new list
            this.values = Lists.newArrayList(values);
        }
    }

    public FacetSelectionType getSelectionType() {
        return selectionType;
    }

    public Collection<String> getValues() {
        return values;
    }

    public FacetSelection getCopy() {
        return new FacetSelection(selectionType, values);
    }

}
