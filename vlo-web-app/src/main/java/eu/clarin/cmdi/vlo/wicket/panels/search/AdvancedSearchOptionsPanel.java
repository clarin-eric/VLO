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

import com.google.common.collect.ImmutableSet;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.FacetSelectionModel;
import eu.clarin.cmdi.vlo.wicket.model.ToggleModel;
import eu.clarin.cmdi.vlo.wicket.panels.ExpandablePanel;
import java.util.Collection;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * A panel showing advanced search options:
 * <ul>
 * <li>selection of records that support FCS</li>
 * <li>selection of records that are based on either CMDI or OLAC</li>
 * </ul>
 *
 * @author twagoo
 */
public abstract class AdvancedSearchOptionsPanel extends ExpandablePanel<QueryFacetsSelection> implements IAjaxIndicatorAware{

    @SpringBean
    private VloConfig config;

    private final Form optionsForm;

    private final AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender();
    /**
     * The fields that this panel provides options for
     */
    public final static Collection<String> OPTIONS_FIELDS = ImmutableSet.of(
            FacetConstants.FIELD_HAS_PART_COUNT,
            FacetConstants.FIELD_SEARCH_SERVICE);

    public AdvancedSearchOptionsPanel(String id, IModel<QueryFacetsSelection> model) {
        super(id, model);
        optionsForm = new Form("options");

        final CheckBox fcsCheck = createFieldNotEmptyOption("fcs", FacetConstants.FIELD_SEARCH_SERVICE);
        optionsForm.add(fcsCheck);

        final MarkupContainer collectionsSection = new WebMarkupContainer("collectionsSection");
        final CheckBox collectionCheck = createFieldNotEmptyOption("collection", FacetConstants.FIELD_HAS_PART_COUNT);
        collectionsSection.add(collectionCheck);
        collectionsSection.setVisible(config.isProcessHierarchies());
        optionsForm.add(collectionsSection);
        
        optionsForm.add(indicatorAppender);

        add(optionsForm);
    }

    private CheckBox createFieldNotEmptyOption(String id, String fieldName) {
        // create a model for the selection state of the facet
        final IModel<FacetSelection> facetModel = new FacetSelectionModel(getModel(), fieldName);
        // wrap in a toggle model that allows switching between a null selection and a 'not empty' selection
        final ToggleModel<FacetSelection> toggleModel = new ToggleModel<>(facetModel, null, new FacetSelection(FacetSelectionType.NOT_EMPTY));

        final CheckBox checkBox = new CheckBox(id, toggleModel);
        checkBox.add(new OnChangeAjaxBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                selectionChanged(target);
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.getAjaxCallListeners().add(new AjaxCallListener()
                        //disable checkboxes while updating via AJAX
                        .onBeforeSend("$('form#advancedoptions input').prop('disabled', true);")
                        //re-enable checkboxes afterwards
                        .onDone("$('form#advancedoptions input').prop('disabled', false);"));
            }
        });
        // should initially be epxanded if one of the options was selected
        if (toggleModel.getObject()) {
            getExpansionModel().setObject(ExpansionState.EXPANDED);
        }
        return checkBox;
    }

    @Override
    protected Label createTitleLabel(String id) {
        return new Label(id, "Search options");
    }

    @Override
    public String getAjaxIndicatorMarkupId() {
        return indicatorAppender.getMarkupId();
    }

    protected abstract void selectionChanged(AjaxRequestTarget target);

}
