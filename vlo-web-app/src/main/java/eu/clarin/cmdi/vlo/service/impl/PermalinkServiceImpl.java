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

import eu.clarin.cmdi.vlo.VloWebAppParameters;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.service.PermalinkService;

import javax.inject.Inject;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import eu.clarin.cmdi.vlo.FieldKey;

/**
 * Permalink service that uses a parameter converter and the current wicket
 * request cycle to compute a permanent link for a given page and selection
 * state
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class PermalinkServiceImpl implements PermalinkService {
    @Inject
    private FieldNameService fieldNameService;

    private final PageParametersConverter<QueryFacetsSelection> paramsConverter;

    public PermalinkServiceImpl(PageParametersConverter<QueryFacetsSelection> paramsConverter) {
        this.paramsConverter = paramsConverter;
    }

    @Override
    public String getUrlString(Class<? extends Page> pageClass, QueryFacetsSelection selection, SolrDocument document) {
        final PageParameters params = new PageParameters();
        if (selection != null) {
            params.mergeWith(paramsConverter.toParameters(selection));
        }
        
        if (document != null) {
            params.add(VloWebAppParameters.DOCUMENT_ID, document.getFirstValue(fieldNameService.getFieldName(FieldKey.ID)));
        }

        final String style = Session.get().getStyle();
        if (style != null) {
            params.add(VloWebAppParameters.THEME, style);
        }

        final CharSequence url = RequestCycle.get().urlFor(pageClass, params);
        final String absoluteUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
        return absoluteUrl;
    }

}
