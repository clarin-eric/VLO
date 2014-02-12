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

import java.util.Collection;

/**
 *
 * @author twagoo
 */
public class FacetStatus {

    private final FacetSelection selection;
    private final Collection<FacetSelection> context;

    public FacetStatus(FacetSelection selection, Collection<FacetSelection> context) {
        this.selection = selection;
        this.context = context;
    }

    /**
     * 
     * @return selections of other facets
     */
    public Collection<FacetSelection> getContext() {
        return context;
    }

    /**
     * 
     * @return facet and value for the current facet
     */
    public FacetSelection getSelection() {
        return selection;
    }

}
