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
package eu.clarin.cmdi.vlo.wicket.panels.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.MapModel;

import eu.clarin.cmdi.vlo.JavaScriptResources;
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.FacetExpansionStateModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldsModel;

/**
 * A panel representing a group of facets.
 *
 * For each facet present in the provided list model, a {@link FacetPanel} is
 * added to the a list view.
 *
 * @author twagoo
 */
public abstract class FacetsPanel extends GenericPanel<List<String>> {

    private MapModel<String, ExpansionState> expansionModel;

    /**
     *
     * @param id component id
     * @param facetsModel model that provides the list of facets to show in this
     * panel
     * @param selectionModel model representing the current query/value
     * selection state
     */
    public FacetsPanel(final String id, final IModel<List<String>> facetNamesModel, final FacetFieldsModel fieldsModel, final IModel<QueryFacetsSelection> selectionModel) {
        super(id, facetNamesModel);

        final Map<String, ExpansionState> expansionStateMap = new HashMap<String, ExpansionState>();
        expansionModel = new MapModel<String, ExpansionState>(expansionStateMap);
        
        final ListView<String> facetsView = new ListView<String>("facets", facetNamesModel) {

            @Override
            protected void populateItem(ListItem<String> item) {
            	// Create a facet field model which does a lookup by name,
                // making it dynamic in case the selection and therefore
                // set of available values changes
                item.add(
                        new FacetPanel("facet", 
                        		item.getModel(),
                        		new FacetFieldModel(item.getModelObject(), fieldsModel), 
                                selectionModel,
                                new FacetExpansionStateModel(item.getModel(), expansionModel)) {

                            @Override
                            protected void selectionChanged(AjaxRequestTarget target) {
                                FacetsPanel.this.selectionChanged(target);
                            }
                        }
                );
            }
        };
        // facet list is not dynamic, so reuse items
        facetsView.setReuseItems(true);
        add(facetsView);

        // links to expand, collapse or deselect all facets
        add(createBatchLinks("batchLinks", selectionModel));
    }

    private Component createBatchLinks(String id, final IModel<QueryFacetsSelection> selectionModel) {
        final WebMarkupContainer links = new WebMarkupContainer(id);
        links.add(new IndicatingAjaxFallbackLink("expandAll") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setAllFacetsExpansionState(ExpansionState.EXPANDED);
                selectionChanged(target);
            }
        });
        links.add(new IndicatingAjaxFallbackLink("collapseAll") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setAllFacetsExpansionState(ExpansionState.COLLAPSED);
                selectionChanged(target);
            }
        });
        links.add(new IndicatingAjaxFallbackLink("deselectAll") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                String query = selectionModel.getObject().getQuery();

                selectionModel.setObject(new QueryFacetsSelection(query));
                selectionChanged(target);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                final Map selection = selectionModel.getObject().getSelection();
                setVisible(selection != null && !selection.isEmpty());
            }
        });
        return links;
    }

    private void setAllFacetsExpansionState(final ExpansionState state) {
        final Map<String, ExpansionState> expansionMap = expansionModel.getObject();
        for (String facetName : getModelObject()) {
            expansionMap.put(facetName, state);
        }
    }

    @Override
    public void detachModels() {
        super.detachModels();
        expansionModel.detach();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        // JQuery UI for tooltips
        response.render(CssHeaderItem.forReference(JavaScriptResources.getJQueryUICSS()));
        response.render(JavaScriptHeaderItem.forReference(JavaScriptResources.getJQueryUIJS()));
        response.render(JavaScriptHeaderItem.forReference(JavaScriptResources.getSyntaxHelpJS()));

    }

    protected abstract void selectionChanged(AjaxRequestTarget target);
}
