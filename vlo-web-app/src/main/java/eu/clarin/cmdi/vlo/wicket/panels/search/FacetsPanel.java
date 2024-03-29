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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.MapModel;

import eu.clarin.cmdi.vlo.JavaScriptResources;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.FieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.NameAndCountFieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.FacetConditionEvaluationService;
import eu.clarin.cmdi.vlo.config.FacetConfigurationService;
import eu.clarin.cmdi.vlo.wicket.BooleanVisibilityBehavior;
import eu.clarin.cmdi.vlo.wicket.model.BooleanOptionsModel;
import eu.clarin.cmdi.vlo.wicket.model.ComputeMapValueModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetExpansionStateModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldsModel;
import java.util.Collection;
import java.util.Optional;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * A panel representing a group of facets.
 *
 * For each facet present in the provided list model, a {@link FacetPanel} is
 * added to the a list view.
 *
 * @author twagoo
 */
public abstract class FacetsPanel extends GenericPanel<List<String>> {

    @SpringBean
    private VloConfig vloConfig;
    @SpringBean
    private FacetConfigurationService facetsConfig;
    @SpringBean
    private FacetConditionEvaluationService facetConditionService;
    @SpringBean
    private JavaScriptResources javaScriptResources;

    private MapModel<String, ExpansionState> expansionModel;
    private IModel<Boolean> allFacetsShown = Model.of(false);

    /**
     *
     * @param id component id
     * @param facetNamesModel model that provides the list of names of facets to
     * show in this panel
     * @param fieldsModel fields model to be passed to individual facet field
     * models
     * @param facetValuesFiltersModel
     * @param selectionModel model representing the current query/value
     * selection state
     * @param selectionTypeModeModel
     */
    public FacetsPanel(final String id, final IModel<List<String>> facetNamesModel, final FacetFieldsModel fieldsModel, IModel<HashMap<String, FieldValuesFilter>> facetValuesFiltersModel, final IModel<QueryFacetsSelection> selectionModel, final IModel<FacetSelectionType> selectionTypeModeModel) {
        super(id, facetNamesModel);

        final Map<String, ExpansionState> expansionStateMap = new HashMap<>();
        expansionModel = new MapModel<>(expansionStateMap);
        final IModel<Boolean> conditionalFacetDisplayModel = () -> {
            //only enable show/hide secondary facets functionality if there are enoug (too many) available facets
            final int numberFacetsShown = getNumberFacetsShown(facetNamesModel.getObject(), fieldsModel.getObject());
            return numberFacetsShown >= vloConfig.getHideSecondaryFacetsLimit(); //configurable threshold
        };

        final MarkupContainer container = new WebMarkupContainer("container");
        add(container
                .setOutputMarkupId(true)
                .add(new AttributeAppender("class", new BooleanOptionsModel<>(conditionalFacetDisplayModel, Model.of("show-conditionally"), new Model<>()), " "))
                .add(new AttributeAppender("class", new BooleanOptionsModel<>(allFacetsShown, Model.of("show-all"), Model.of("show-primary")), " "))
        );

        final ListView<String> facetsView = new ListView<String>("facets", facetNamesModel) {

            @Override
            protected void populateItem(final ListItem<String> item) {
                final ComputeMapValueModel<String, FieldValuesFilter> filterModel = new ComputeMapValueModel<String, FieldValuesFilter>(facetValuesFiltersModel, item.getModel()) {
                    @Override
                    protected FieldValuesFilter computeObject(IModel<String> keyModel) {
                        return new NameAndCountFieldValuesFilter();
                    }
                };

                // Create a facet field model which does a lookup by name,
                // making it dynamic in case the selection and therefore
                // set of available values changes
                item.add(new FacetPanel("facet",
                        item.getModel(),
                        new FacetFieldModel(item.getModel(), fieldsModel),
                        selectionModel,
                        selectionTypeModeModel,
                        new FacetExpansionStateModel(item.getModel(), expansionModel), filterModel) {

                    @Override
                    protected void selectionChanged(Optional<AjaxRequestTarget> target) {
                        FacetsPanel.this.selectionChanged(target);
                    }

                    @Override
                    protected boolean meetsDisplayConditions() {
                        return facetConditionService.shouldShow(item.getModelObject(), selectionModel.getObject(), fieldsModel.getObject());
                    }

                }.add(new AttributeAppender("class", new IModel<String>() {
                    //class appender that differentiates between primary and secondary facets (based on configuration)
                    @Override
                    public String getObject() {
                        final Collection<String> primaryFacetFields = facetsConfig.getPrimaryFacetFieldNames();
                        if (primaryFacetFields == null || primaryFacetFields.isEmpty()) {
                            //no primary facets configured, don't set a class
                            return null;
                        } else if (primaryFacetFields.contains(item.getModelObject())) {
                            return "primary-facet";
                        } else {
                            return "secondary-facet";
                        }
                    }
                }, " "))
                );
            }
        };
        // facet list is not dynamic, so reuse items
        facetsView.setReuseItems(true);
        container.add(facetsView);

        //toggler for showing/hiding secondary facets
        container.add(new AjaxFallbackLink<Void>("more") {
            @Override
            public void onClick(Optional<AjaxRequestTarget> t) {
                allFacetsShown.setObject(Boolean.TRUE);
                t.ifPresent(target -> {
                    target.add(container);
                    target.appendJavaScript("$('[data-toggle=\"tooltip\"]').tooltip();");
                });
            }

        }.add(BooleanVisibilityBehavior.visibleOnFalse(allFacetsShown)));

        container.add(new AjaxFallbackLink<Void>("fewer") {
            @Override
            public void onClick(Optional<AjaxRequestTarget> t) {
                allFacetsShown.setObject(Boolean.FALSE);
                t.ifPresent(target -> {
                    target.add(container);
                });
            }
        }.add(BooleanVisibilityBehavior.visibleOnTrue(allFacetsShown)));
    }

    /**
     *
     * @param visibleFacets superset of facet fields that may be shown
     * @param actualFacetFields facet fields currently available
     * @return
     */
    private int getNumberFacetsShown(final List<String> visibleFacets, final List<FacetField> actualFacetFields) {
        final Predicate<FacetField> shownFieldHasValues = new Predicate<FacetField>() {
            @Override
            public boolean apply(FacetField input) {
                return input.getValueCount() > 0 && visibleFacets.contains(input.getName());
            }
        };
        return Iterables.size(Iterables.filter(actualFacetFields, shownFieldHasValues));
    }

    @Override
    public void detachModels() {
        super.detachModels();
        expansionModel.detach();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        // JQuery UI for tooltips
        response.render(JavaScriptHeaderItem.forReference(javaScriptResources.getSyntaxHelpJS()));
    }

    protected abstract void selectionChanged(Optional<AjaxRequestTarget> target);
}
