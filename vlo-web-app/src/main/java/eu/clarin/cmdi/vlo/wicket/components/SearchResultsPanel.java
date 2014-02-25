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

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.SolrDocumentService;
import eu.clarin.cmdi.vlo.wicket.provider.SolrDocumentProvider;
import java.util.Arrays;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.navigation.paging.IPageableItems;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Panel that has a data view on the current search results
 *
 * @author twagoo
 */
public class SearchResultsPanel extends Panel {

    public static final List<Long> ITEMS_PER_PAGE_OPTIONS = Arrays.asList(5L, 10L, 25L, 50L, 100L);

    @SpringBean
    private SolrDocumentService documentService;
    private final IDataProvider<SolrDocument> solrDocumentProvider;
    private final DataView<SolrDocument> resultsView;

    public SearchResultsPanel(String id, IModel<QueryFacetsSelection> model) {
        super(id, model);
        solrDocumentProvider = new SolrDocumentProvider(documentService, model);

        // data view for search results
        resultsView = new DataView<SolrDocument>("resultItem", solrDocumentProvider, 10) {

            @Override
            protected void populateItem(Item<SolrDocument> item) {
                // single result item
                item.add(new SearchResultItemPanel("resultItemDetails", item.getModel()));
            }
        };
        add(resultsView);

        // pagination navigators
        add(new AjaxPagingNavigator("pagingTop", resultsView));
        add(new AjaxPagingNavigator("pagingBottom", resultsView));

        // total result counter
        add(createResultCount("resultCount"));

        // page result indicater
        add(createResultPageIndicator("resultPageIndicator", resultsView));

        // form to select number of results per page
        add(createResultPageSizeForm("resultPageSizeForm", resultsView));

        //For Ajax updating of search results
        setOutputMarkupId(true);
    }

    /**
     * Gets called on each request before render
     */
    @Override
    protected void onConfigure() {
        super.onConfigure();
        
        // only show pagination navigators if there's more than one page
        final boolean showPaging = resultsView.getPageCount() > 1;
        this.get("pagingTop").setVisible(showPaging);
        this.get("pagingBottom").setVisible(showPaging);
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

    private Form createResultPageSizeForm(String id, final IPageableItems resultsView) {
        final Form resultPageSizeForm = new Form(id);

        final DropDownChoice<Long> pageSizeDropDown
                = new DropDownChoice<Long>("resultPageSize",
                        // bind to items per page property of pageable
                        new PropertyModel<Long>(resultsView, "itemsPerPage"),
                        ITEMS_PER_PAGE_OPTIONS);
        pageSizeDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(SearchResultsPanel.this);
            }
        });
        resultPageSizeForm.add(pageSizeDropDown);

        return resultPageSizeForm;
    }

}
