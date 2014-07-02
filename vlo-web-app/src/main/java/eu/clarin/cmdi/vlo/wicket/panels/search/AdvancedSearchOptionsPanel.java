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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.FacetSelectionModel;
import eu.clarin.cmdi.vlo.wicket.model.ToggleModel;
import eu.clarin.cmdi.vlo.wicket.panels.ExpandablePanel;
import eu.clarin.cmdi.vlo.wicket.panels.VirtualCollectionFormPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

/**
 * A panel showing advanced search options:
 * <ul>
 * <li>selection of records that support FCS</li>
 * <li>selection of records that are based on either CMDI or OLAC</li>
 * </ul>
 *
 * @author twagoo
 */
public abstract class AdvancedSearchOptionsPanel extends ExpandablePanel<QueryFacetsSelection> {

    public AdvancedSearchOptionsPanel(String id, IModel<QueryFacetsSelection> model) {
        super(id, model);

        // create a model for the selection state for the FCS facet
        final IModel<FacetSelection> fcsFacetModel = new FacetSelectionModel(model, FacetConstants.FIELD_SEARCH_SERVICE);
        // wrap in a toggle model that allows switching between a null selection and a 'not empty' selection
        final ToggleModel<FacetSelection> toggleModel = new ToggleModel<FacetSelection>(fcsFacetModel, null, new FacetSelection(FacetSelectionType.NOT_EMPTY));

        final Form options = new Form("options");
        final CheckBox fcsCheck = new CheckBox("fcs", toggleModel);
        fcsCheck.add(new OnChangeAjaxBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                selectionChanged(target);
            }
        });
        options.add(fcsCheck);
        add(options);
        
        //TODO: lazy rendering of form, it can be large. I.e. provide a link
        //that makes form 'visible', and then tries to auto submit
        add(new VirtualCollectionFormPanel("vcrForm", model));

        // should initially be epxanded if one of the options was selected
        if (toggleModel.getObject()) {
            getExpansionModel().setObject(ExpansionState.EXPANDED);
        }
    }

    @Override
    protected Label createTitleLabel(String id) {
        return new Label(id, "Search options");
    }

    protected abstract void selectionChanged(AjaxRequestTarget target);

}
