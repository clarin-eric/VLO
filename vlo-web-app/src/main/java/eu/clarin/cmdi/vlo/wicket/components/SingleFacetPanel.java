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
import java.util.Collection;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel that shows values or selected value only for collection facet
 *
 * @author twagoo
 */
public class SingleFacetPanel extends AbstractFacetsPanel { // Composition over inheritance!?

    private final static Logger logger = LoggerFactory.getLogger(SingleFacetPanel.class);

    @SpringBean(name = VloSpringConfig.COLLECTION_FACET_SERVICE)
    private FacetFieldsService facetFieldsService;

    private final IModel<QueryFacetsSelection> selectionModel;
    private final IModel<FacetField> facetFieldModel;
    private final SelectedFacetPanel selectedFacetPanel;
    private final FacetValuesPanel facetValuesPanel;

    public SingleFacetPanel(final String id, final IModel<QueryFacetsSelection> selectionModel) {
        super(id, selectionModel);
        
        this.selectionModel = selectionModel;
        this.facetFieldModel = new SingleFacetFieldModel();

        // create both panels, only one is visible at a time (through logic in onConfigure)
        
        // panel showing values for selection
        facetValuesPanel = createFacetValuesPanel("facetValues", facetFieldModel);
        add(facetValuesPanel);

        // panel showing current selection, allowing for deselection
        selectedFacetPanel = createSelectedFacetPanel("facetSelection", facetFieldModel.getObject().getName());
        add(selectedFacetPanel);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        // decide which facet panel to make visible (depending on selection)
        final String facetName = facetFieldModel.getObject().getName();
        final Collection<String> selectionValues = selectionModel.getObject().getSelectionValues(facetName);
        boolean valuesSelected = selectionValues != null && selectionValues.size() > 0;

        facetValuesPanel.setVisible(!valuesSelected);
        selectedFacetPanel.setVisible(valuesSelected);
    }

    private class SingleFacetFieldModel extends AbstractReadOnlyModel<FacetField> {

        @Override
        public FacetField getObject() {
            List<FacetField> facetFields = facetFieldsService.getFacetFields(selectionModel.getObject());
            if (facetFields.isEmpty()) {
                return null;
            } else {
                if (facetFields.size() > 1) {
                    logger.warn("More than one facet returned! Only using first result of {}", facetFields);
                }
                return facetFields.get(0);
            }
        }
    }

}
