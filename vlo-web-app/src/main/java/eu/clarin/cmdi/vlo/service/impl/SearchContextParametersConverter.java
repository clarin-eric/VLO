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
package eu.clarin.cmdi.vlo.service.impl;

import eu.clarin.cmdi.vlo.VloWebAppParameters;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.wicket.model.SearchContextModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.StringValueConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter for the {@link VloWebAppParameters#SEARCH_INDEX} and
 * {@link VloWebAppParameters#SEARCH_COUNT} parameters, which function to carry
 * the result (paging) context (result count and offset) to and between record
 * pages
 *
 * @author twagoo
 */
public class SearchContextParametersConverter implements PageParametersConverter<SearchContext> {

    private final static Logger logger = LoggerFactory.getLogger(SearchContextParametersConverter.class);
    private final PageParametersConverter<QueryFacetsSelection> selectionConverter;

    public SearchContextParametersConverter(PageParametersConverter<QueryFacetsSelection> selectionConverter) {
        this.selectionConverter = selectionConverter;
    }

    @Override
    public SearchContext fromParameters(PageParameters params) {
        final StringValue indexParam = params.get(VloWebAppParameters.SEARCH_INDEX);
        final StringValue countParam = params.get(VloWebAppParameters.SEARCH_COUNT);
        final QueryFacetsSelection selection = selectionConverter.fromParameters(params);
        if (!indexParam.isEmpty() && !countParam.isEmpty() && selection != null) {
            try {
                return new SearchContextModel(indexParam.toLong(), countParam.toLong(), Model.of(selection));
            } catch (StringValueConversionException ex) {
                logger.warn("Illegal query parameter value", ex);
            }
        }
        return null;
    }

    @Override
    public PageParameters toParameters(SearchContext object) {
        // start with encoding selection
        final PageParameters params = selectionConverter.toParameters(object.getSelection());
        // add index and count
        params.add(VloWebAppParameters.SEARCH_INDEX, object.getIndex());
        params.add(VloWebAppParameters.SEARCH_COUNT, object.getResultCount());
        return params;
    }

}
