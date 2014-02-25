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

import eu.clarin.cmdi.vlo.config.VloSpringConfig;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.FacetFieldsService;
import eu.clarin.cmdi.vlo.wicket.provider.FacetFieldsDataProvider;
import java.util.Collection;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * A panel representing a group of facets.
 *
 * For each facet present (retrieved from the injected
 * {@link FacetFieldsService}, a panel is added. This is either a
 * {@link FacetValuesPanel}, allowing for selection of facet values, or a
 * {@link SelectedFacetPanel} representing a facet with selected values,
 * allowing for deselection of these values.
 *
 * @author twagoo
 */
public class FacetsPanel extends AbstractFacetsPanel {

    @SpringBean(name = VloSpringConfig.FACETS_PANEL_SERVICE)
    private FacetFieldsService facetFieldsService;

    public FacetsPanel(final String id, IModel<QueryFacetsSelection> model) {
        super(id, model);

        add(new DataView<FacetField>("facets", new FacetFieldsDataProvider(facetFieldsService, model)) {

            @Override
            protected void populateItem(Item<FacetField> item) {
                item.add(createFacetPanel("facet", item.getModel()));
            }
        });
    }

    private Panel createFacetPanel(String id, IModel<FacetField> facetFieldModel) {
        // Is there a selection for this facet?
        final String facetName = facetFieldModel.getObject().getName();
        final Collection<String> selectionValues = model.getObject().getSelectionValues(facetName);
        // Show different panel, depending on selected values
        if (selectionValues == null || selectionValues.isEmpty()) {
            // No values selected, show value selection panel
            return createFacetValuesPanel(id, facetFieldModel);
        } else {
            // Values selected, show selected values panel (with option to remove)
            return createSelectedFacetPanel(id, facetName);
        }
    }
}
