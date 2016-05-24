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

import static eu.clarin.cmdi.vlo.VloWebAppParameters.RECORD_PAGE_TAB;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import eu.clarin.cmdi.vlo.wicket.LinkDisabledClassBehaviour;
import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Link that causes a navigation to a target record (disabled if no such
 * record exists) in the provided search context
 *
 * @author twagoo
 */
public abstract class RecordNavigationLink extends Link<SearchContext> {

    @SpringBean
    private SolrDocumentService documentService;
    @SpringBean(name = "documentParamsConverter")
    private PageParametersConverter<SolrDocument> documentParamConverter;
    @SpringBean(name = "searchContextParamsConverter")
    private PageParametersConverter<SearchContext> contextParamConverter;
    private final IModel<String> tabModel;

    public RecordNavigationLink(String id, IModel<SearchContext> model, IModel<String> tabModel) {
        super(id, model);
        add(new LinkDisabledClassBehaviour());
        this.tabModel = tabModel;
    }

    @Override
    public void onClick() {
        final SearchContext context = getModelObject();
        final IModel<SearchContext> targetModel = getTargetModel();
        final List<SolrDocument> documents = documentService.getDocuments(context.getSelection(), (int) targetModel.getObject().getIndex(), 1);
        if (documents.size() > 0) {
            // found it, go there
            final PageParameters params = documentParamConverter.toParameters(documents.get(0));
            params.mergeWith(contextParamConverter.toParameters(targetModel.getObject()));
            if (tabModel.getObject() != null) {
                params.add(RECORD_PAGE_TAB, tabModel.getObject());
            }
            setResponsePage(RecordPage.class, params);
        }
    }

    @Override
    public boolean isEnabled() {
        // disable for first item
        return targetExists();
    }

    @Override
    protected void onDetach() {
        if (tabModel != null) {
            tabModel.detach();
        }
        super.onDetach();
    }

    /**
     * 
     * @return search context model for the target record (evaluated when the link is clicked)
     */
    protected abstract IModel<SearchContext> getTargetModel();

    /**
     * 
     * @return whether the target record exists (false will disable the link)
     */
    protected abstract boolean targetExists();

}
