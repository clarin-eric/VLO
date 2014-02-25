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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.SolrDocumentService;
import eu.clarin.cmdi.vlo.wicket.model.NullFallbackModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.provider.SolrDocumentProvider;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.navigation.paging.IPageableItems;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Panel that has a data view on the current search results
 *
 * @author twagoo
 */
public class SearchResultsPanel extends Panel {

    @SpringBean
    private SolrDocumentService documentService;
    private final IDataProvider<SolrDocument> solrDocumentProvider;

    public SearchResultsPanel(String id, IModel<QueryFacetsSelection> model) {
        super(id, model);
        solrDocumentProvider = new SolrDocumentProvider(documentService, model);

        // dynamic results view
        final DataView<SolrDocument> resultsView = createResultsView("resultItem");
        add(resultsView);

        // pagination navigators
        add(new AjaxPagingNavigator("pagingTop", resultsView));
        add(new AjaxPagingNavigator("pagingBottom", resultsView));

        // total result counter
        add(createResultCount("resultCount"));

        // page result indicater
        add(createResultPageIndicator("resultPageIndicator", resultsView));

        //For Ajax updating of search results
        setOutputMarkupId(true);
    }

    private DataView<SolrDocument> createResultsView(String id) {
        final DataView<SolrDocument> resultsView = new DataView<SolrDocument>(id, solrDocumentProvider, 10) {

            @Override
            protected void populateItem(Item<SolrDocument> item) {
                final IModel<SolrDocument> documentModel = item.getModel();
                item.add(new SolrFieldLabel("title", documentModel, FacetConstants.FIELD_NAME));
                item.add(new SolrFieldLabel("description", documentModel, FacetConstants.FIELD_DESCRIPTION, "<no description>"));
                //TODO: get resource information
            }
        };
        return resultsView;
    }

    private Label createResultCount(String id) {
        final IModel<String> resultCountModel = new AbstractReadOnlyModel<String>() {
            
            @Override
            public String getObject() {
                return String.format("%d results", solrDocumentProvider.size());
            }
        };
        return new Label(id, resultCountModel);
    }

    private Label createResultPageIndicator(String id, final IPageableItems resultsView) {
        IModel<String> indicatorModel = new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                final long firstShown = 1 + resultsView.getCurrentPage() * resultsView.getItemsPerPage();
                final long lastShown = Math.min(resultsView.getItemCount(), firstShown + resultsView.getItemsPerPage() - 1);
                return String.format("Showing %d to %d", firstShown, lastShown);
            }
        };
        return new Label(id, indicatorModel);
    }

    public static class SolrFieldLabel extends Label {

        public SolrFieldLabel(String id, IModel<SolrDocument> documentModel, String fieldName) {
            super(id, new SolrFieldModel(documentModel, fieldName));
        }

        public SolrFieldLabel(String id, IModel<SolrDocument> documentModel, String fieldName, String nullFallback) {
            super(id,
                    new NullFallbackModel(
                            new SolrFieldModel(documentModel, fieldName), nullFallback));
        }

    }

}
