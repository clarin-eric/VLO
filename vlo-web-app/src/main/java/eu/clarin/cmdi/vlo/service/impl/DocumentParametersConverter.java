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
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.FieldKey;

import javax.inject.Inject;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

/**
 *
 * @author twagoo
 */
public class DocumentParametersConverter implements PageParametersConverter<SolrDocument> {
    @Inject
    private FieldNameService fieldNameService;

    @Override
    public SolrDocument fromParameters(PageParameters params) {
        final StringValue docIdParam = params.get(VloWebAppParameters.DOCUMENT_ID);
        if (docIdParam.isEmpty()) {
            return null;
        } else {
            return VloWicketApplication.get().getDocumentService().getDocument(docIdParam.toString());
        }
    }

    @Override
    public PageParameters toParameters(SolrDocument document) {
        final PageParameters params = new PageParameters();
        if (document != null) {
            params.add(VloWebAppParameters.DOCUMENT_ID,
                document.getFieldValue(fieldNameService.getFieldName(FieldKey.ID)));
        }
        return params;
    }

}
