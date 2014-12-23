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
package eu.clarin.cmdi.vlo.wicket.components;

import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import eu.clarin.cmdi.vlo.wicket.model.SearchContextModel;
import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Link that causes a navigation to the previous record (disabled if no such
 * record exists) in the provided search context
 *
 * @author twagoo
 * @see NextRecordLink
 */
public class PreviousRecordLink extends Link<SearchContext> {

    @SpringBean
    private SolrDocumentService documentService;
    @SpringBean(name="documentParamsConverter")
    private PageParametersConverter<SolrDocument> documentParamConverter;
    @SpringBean(name="searchContextParamsConverter")
    private PageParametersConverter<SearchContext> contextParamConverter;

    public PreviousRecordLink(String id, IModel<SearchContext> model) {
        super(id, model);
    }

    @Override
    public void onClick() {
        final SearchContext context = getModelObject();
        final int index = (int) context.getIndex() - 1;
        // get the previous record as from the service
        final List<SolrDocument> documents = documentService.getDocuments(context.getSelection(), index, 1);
        if (documents.size() > 0) {
            // found it, go there
            final PageParameters params = documentParamConverter.toParameters(documents.get(0));
            params.mergeWith(contextParamConverter.toParameters(SearchContextModel.previous(context)));
            setResponsePage(RecordPage.class, params);
        }
    }

    @Override
    public boolean isEnabled() {
        // disable for first item
        return getModelObject().getIndex() > 0;
    }

}
