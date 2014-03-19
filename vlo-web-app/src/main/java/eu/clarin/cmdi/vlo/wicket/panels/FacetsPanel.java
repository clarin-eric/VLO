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
package eu.clarin.cmdi.vlo.wicket.panels;

import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.wicket.model.FacetExpansionStateModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetSelectionModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.MapModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * A panel representing a group of facets.
 *
 * For each facet present in the provided list model, a {@link FacetPanel} is
 * added to the a list view.
 *
 * @author twagoo
 */
public abstract class FacetsPanel extends Panel {

    @SpringBean
    private FacetFieldsService facetFieldsService;

    /**
     *
     * @param id component id
     * @param facetsModel model that provides the list of facets to show in this
     * panel
     * @param selectionModel model representing the current query/value
     * selection state
     */
    public FacetsPanel(final String id, final IModel<List<FacetField>> facetsModel, final IModel<QueryFacetsSelection> selectionModel) {
        super(id);

        final Map<String, ExpansionState> expansionStateMap = new HashMap<String, ExpansionState>();
        final MapModel<String, ExpansionState> expansionModel = new MapModel<String, ExpansionState>(expansionStateMap);

        final ListView<FacetField> facetsView = new ListView<FacetField>("facets", facetsModel) {

            @Override
            protected void populateItem(ListItem<FacetField> item) {
                // Create a facet field model which does a lookup by name,
                // making it dynamic in case the selection and therefore
                // set of available values changes
                final FacetFieldModel facetFieldModel = new FacetFieldModel(facetFieldsService, item.getModelObject(), selectionModel);
                item.add(
                        new FacetPanel("facet",
                                new FacetSelectionModel(facetFieldModel, selectionModel),
                                new FacetExpansionStateModel(facetFieldModel, expansionModel)) {

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
    }

    protected abstract void selectionChanged(AjaxRequestTarget target);
}
