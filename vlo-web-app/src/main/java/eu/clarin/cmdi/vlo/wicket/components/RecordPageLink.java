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

import eu.clarin.cmdi.vlo.VloWebAppParameters;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Link to the {@link RecordPage Record Page} for a document with an optional
 * search context, both provided by a model
 *
 * @author twagoo
 */
public class RecordPageLink extends Link {

    @SpringBean(name = "documentParamsConverter")
    private PageParametersConverter<SolrDocument> documentParamConverter;
    @SpringBean(name = "searchContextParamsConverter")
    private PageParametersConverter<SearchContext> contextParamConverter;

    private final IModel<SolrDocument> documentModel;
    private final IModel<SearchContext> selectionModel;
    private final String initialTab;

    /**
     *
     * @param id component id
     * @param documentModel document model
     */
    public RecordPageLink(String id, IModel<SolrDocument> documentModel) {
        this(id, documentModel, null, null);
    }

    /**
     *
     * @param id component id
     * @param documentModel document model
     * @param selectionModel search context model
     */
    public RecordPageLink(String id, IModel<SolrDocument> documentModel, IModel<SearchContext> selectionModel) {
        this(id, documentModel, selectionModel, null);
    }

    /**
     *
     * @param id component id
     * @param documentModel document model
     * @param selectionModel search context model
     * @param initialTab content tab to select initially (can be null)
     */
    public RecordPageLink(String id, IModel<SolrDocument> documentModel, IModel<SearchContext> selectionModel, String initialTab) {
        super(id);
        this.documentModel = documentModel;
        this.selectionModel = selectionModel;
        this.initialTab = initialTab;
    }

    @Override
    public void onClick() {
        final PageParameters params = documentParamConverter.toParameters(documentModel.getObject());
        if (selectionModel != null) {
            params.mergeWith(contextParamConverter.toParameters(selectionModel.getObject()));
        }
        if (initialTab != null) {
            params.add(VloWebAppParameters.RECORD_PAGE_TAB, initialTab);
        }
        setResponsePage(RecordPage.class, params);
    }

    @Override
    public void detachModels() {
        super.detachModels();
        documentModel.detach();
        if (selectionModel != null) {
            selectionModel.detach();
        }
    }

}
