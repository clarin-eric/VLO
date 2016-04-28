/*
 * Copyright (C) 2016 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.panels.search;

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import static eu.clarin.cmdi.vlo.wicket.panels.search.SearchResultsPanel.ITEMS_PER_PAGE_OPTIONS;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.navigation.paging.IPageableItems;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.AbstractPageableView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class SearchResultsHeaderPanel extends GenericPanel<QueryFacetsSelection> {

    private final IDataProvider<SolrDocument> solrDocumentProvider;
    private final AbstractPageableView<SolrDocument> resultsView;

    public SearchResultsHeaderPanel(String id, IModel<QueryFacetsSelection> model, AbstractPageableView<SolrDocument> resultsView, IDataProvider<SolrDocument> solrDocumentProvider) {
        super(id, model);

        this.solrDocumentProvider = solrDocumentProvider;
        this.resultsView = resultsView;

        add(createSearchInfoLabel("searchInfo"));
        // form to select number of results per page
        add(createResultPageSizeForm("resultPageSizeForm", resultsView));

        //For Ajax updating of search results
        setOutputMarkupId(true);
    }

    private Label createSearchInfoLabel(String id) {
        return new Label(id, new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                final QueryFacetsSelection selection = getModel().getObject();
                if ((selection.getQuery() == null || selection.getQuery().isEmpty())
                        && (selection.getSelection() == null || selection.getSelection().isEmpty())) {
                    return String.format("Showing all %d records", solrDocumentProvider.size());
                } else {
                    final long firstShown = 1 + resultsView.getCurrentPage() * resultsView.getItemsPerPage();
                    final long lastShown = Math.min(resultsView.getItemCount(), firstShown + resultsView.getItemsPerPage() - 1);
                    final String query = selection.getQuery();
                    return String.format("Showing %s %d results%s",
                            (resultsView.getPageCount() <= 1) ? "" : String.format("%d to %d of ", firstShown, lastShown),
                            solrDocumentProvider.size(),
                            query == null ? "" : String.format(" for '%s'", query)
                    );
                }
            }
        });
    }

    private Form createResultPageSizeForm(String id, final IPageableItems resultsView) {
        final Form resultPageSizeForm = new Form(id);

        final DropDownChoice<Long> pageSizeDropDown
                = new DropDownChoice<Long>("resultPageSize",
                        // bind to items per page property of pageable
                        new PropertyModel<Long>(resultsView, "itemsPerPage"),
                        ITEMS_PER_PAGE_OPTIONS);
        pageSizeDropDown.add(new AjaxFormComponentUpdatingBehavior("change") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                onChange(target);
            }
        });
        resultPageSizeForm.add(pageSizeDropDown);

        return resultPageSizeForm;
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        solrDocumentProvider.detach();
    }

    protected void onChange(AjaxRequestTarget target) {
        //noop - may be overridden
    }

}
