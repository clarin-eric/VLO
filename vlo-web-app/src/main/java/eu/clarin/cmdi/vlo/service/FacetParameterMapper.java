/*
 * Copyright (C) 2015 CLARIN
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
package eu.clarin.cmdi.vlo.service;

/**
 * Interface for services that map facet names and values to new names and
 * values, primarily for compatibility purposes, {@literal i.e.} not breaking
 * bookmarks and links. These mappers should always fallback to 'identity'
 * behaviour, {@literal i.e.} returning the input in case no other mapping is
 * defined.
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public interface FacetParameterMapper {

    /**
     *
     * @param facet name of the original facet
     * @return facet name that the original maps to that should be used
     * (original should be returned if no other mapping has been defined)
     */
    String getFacet(String facet);

    /**
     *
     * @param facet name of the <em>original</em> facet
     * @param value original value
     * @return value that the original maps to that should be used (original
     * should be returned if no other mapping has been defined)
     */
    String getValue(String facet, String value);

    /**
     * Implementation that maps input to output unchanged
     */
    public static class IdentityMapper implements FacetParameterMapper {

        @Override
        public String getFacet(String facet) {
            return facet;
        }

        @Override
        public String getValue(String facet, String value) {
            return value;
        }
    }

}
