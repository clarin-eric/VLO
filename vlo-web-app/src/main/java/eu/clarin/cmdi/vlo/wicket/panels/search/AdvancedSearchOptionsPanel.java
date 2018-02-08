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
import com.google.common.collect.Lists;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.BooleanOptionsModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetSelectionModel;
import eu.clarin.cmdi.vlo.wicket.model.ToggleModel;
import eu.clarin.cmdi.vlo.wicket.pages.VirtualCollectionSubmissionPage;
import eu.clarin.cmdi.vlo.wicket.panels.ExpandablePanel;
import java.util.Collection;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
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
public abstract class AdvancedSearchOptionsPanel extends ExpandablePanel<QueryFacetsSelection> implements IAjaxIndicatorAware {

    @SpringBean
    private VloConfig config;
    @SpringBean
    private FieldNameService fieldNameService;

    private final IDataProvider<SolrDocument> documentProvider;

    private final Form optionsForm;

    private final AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender();

    /**
     * The fields that this panel provides options for
     */
    public final static Collection<FieldKey> OPTIONS_FIELDS = ImmutableSet.of(
            FieldKey.HAS_PART_COUNT,
            FieldKey.SEARCH_SERVICE);

    public AdvancedSearchOptionsPanel(String id, final IModel<QueryFacetsSelection> model, IModel<FacetSelectionType> selectionTypeModeModel, final IDataProvider<SolrDocument> documentProvider) {
        super(id, model);
        this.documentProvider = documentProvider;

        optionsForm = new Form("options");

        final DropDownChoice<FacetSelectionType> selectionType = new DropDownChoice<>("selectionType", selectionTypeModeModel, Lists.newArrayList(FacetSelectionType.AND, FacetSelectionType.OR));
        selectionType.add(new OnChangeAjaxBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                selectionChanged(target);
            }
        });
        optionsForm.add(selectionType);

        final CheckBox fcsCheck = createFieldNotEmptyOption("fcs", fieldNameService.getFieldName(FieldKey.SEARCH_SERVICE));
        optionsForm.add(fcsCheck);

        final MarkupContainer collectionsSection = new WebMarkupContainer("collectionsSection");
        final CheckBox collectionCheck = createFieldNotEmptyOption("collection", fieldNameService.getFieldName(FieldKey.HAS_PART_COUNT));
        collectionsSection.add(collectionCheck);
        collectionsSection.setVisible(config.isProcessHierarchies());
        optionsForm.add(collectionsSection);

        optionsForm.add(indicatorAppender);

        add(optionsForm);

        add(createVcrSubmitTrigger("vcrSubmitTrigger"));
    }

    private Component createVcrSubmitTrigger(String id) {

        final IModel<Boolean> vcrSubmitEnabledModel = new LoadableDetachableModel<Boolean>() {
            @Override
            protected Boolean load() {
                final long documentCount = documentProvider.size();
                return (documentCount > 0 && documentCount <= config.getVcrMaximumItemsCount());
            }
        };

        final Link submitLink = new Link(id, vcrSubmitEnabledModel) {
            @Override
            public void onClick() {
                setResponsePage(new VirtualCollectionSubmissionPage(AdvancedSearchOptionsPanel.this.getModel(), documentProvider));
            }

            @Override
            public boolean isEnabled() {
                return vcrSubmitEnabledModel.getObject();
            }
        };

        return submitLink
                .add(new AttributeAppender("class",
                        new BooleanOptionsModel(vcrSubmitEnabledModel,
                                new Model<String>(null), //enabled
                                Model.of("disabled")), " "))
                .add(new AttributeModifier("target",
                        new BooleanOptionsModel(vcrSubmitEnabledModel,
                                Model.of("_blank"),
                                new Model<String>(null))))
                .add(new AttributeModifier("title",
                        new BooleanOptionsModel(vcrSubmitEnabledModel,
                                Model.of("Create a new collection in the Virtual Collection Registry containing the records included in the current search result"),
                                Model.of(String.format("Only available for search results containing %s items or less", config.getVcrMaximumItemsCount())))));
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

    @Override
    public void detachModels() {
        super.detachModels();
        documentProvider.detach();
    }

    protected abstract void selectionChanged(AjaxRequestTarget target);

}
