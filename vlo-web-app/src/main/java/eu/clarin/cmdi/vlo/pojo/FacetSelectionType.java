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

/**
 *
 * @author twagoo
 */
public enum FacetSelectionType {

    /**
     * Specifies a selection of all documents with a non-empty value for this
     * facet; values supplied in the selection should be ignored
     */
    NOT_EMPTY,
    /**
     * Specifies a selection of all documents that match all values supplied in
     * the selection
     */
    AND,
    /**
     * Specifies a selection of all documents that match at least one of the
     * values supplied in the selection
     */
    OR

}
