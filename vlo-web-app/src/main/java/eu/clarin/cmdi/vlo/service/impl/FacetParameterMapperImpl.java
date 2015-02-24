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
package eu.clarin.cmdi.vlo.service.impl;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.service.FacetParameterMapper.IdentityMapper;

/**
 * A mapper that maps facet names and values to new names and values, primarily
 * for compatibility purposes, {@literal i.e.} not breaking bookmarks and links.
 *
 * Implemented mappings:
 * <ul>
 * <li>language -> languageCode</li>
 * </ul>
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class FacetParameterMapperImpl extends IdentityMapper {

    private final LanguageCodeUtils languageCodeUtils;

    public FacetParameterMapperImpl(LanguageCodeUtils languageCodeUtils) {
        this.languageCodeUtils = languageCodeUtils;
    }

    /**
     *
     * @param facet name of the original facet
     * @return facet name that the original maps to that should be used
     */
    @Override
    public String getFacet(String facet) {
        switch (facet) {
            case FacetConstants.DEPRECATED_FIELD_LANGUAGE:
                return FacetConstants.FIELD_LANGUAGE_CODE;
            default:
                return super.getFacet(facet);
        }
    }

    /**
     *
     * @param facet name of the <em>original</em> facet
     * @param value original value
     * @return value that the original maps to that should be used
     */
    @Override
    public String getValue(String facet, String value) {
        switch (facet) {
            case FacetConstants.DEPRECATED_FIELD_LANGUAGE:
                return mapToLanguageCode(value);
            default:
                return super.getValue(facet, value);
        }
    }

    private String mapToLanguageCode(String languageName) {
        final String code = languageCodeUtils.getLanguageNameToIso639Map().get(languageName);
        if (code != null) {
            return "code:" + code;
        } else {
            return "name:" + languageName;
        }
    }

}
