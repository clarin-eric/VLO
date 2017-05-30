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
package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.VloWebSession;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.FacetParameterMapper;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;
import eu.clarin.cmdi.vlo.wicket.panels.BreadCrumbPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.AllFacetValuesPanel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;

/**
 *
 * @author twagoo
 */
public class AllFacetValuesPage extends VloBasePage<FacetField> {

    public final static String SELECTED_FACET_PARAM = "selectedFacet";

    @SpringBean
    private FacetFieldsService facetFieldsService;
    @SpringBean(name = "queryParametersConverter")
    private PageParametersConverter<QueryFacetsSelection> parametersConverter;
    @SpringBean
    private FacetParameterMapper facetParamMapper;
    @SpringBean
    private VloConfig vloConfig;

    private final IModel<QueryFacetsSelection> selectionModel;
    private final IModel<FacetSelectionType> selectionTypeModeModel;

    public AllFacetValuesPage(PageParameters params) {
        super(params);

        this.selectionModel = Model.of(parametersConverter.fromParameters(params));

        final FacetSelectionType sessionSelectionType = VloWebSession.get().getFacetSelectionTypeMode();
        this.selectionTypeModeModel = Model.of(
                sessionSelectionType == null
                        ? FacetSelectionType.OR
                        : sessionSelectionType);
        final StringValue facetValue = params.get(SELECTED_FACET_PARAM);
        if (facetValue.isEmpty()) {
            Session.get().error("No facet provided for all values page");
            throw new RestartResponseException(new FacetedSearchPage(selectionModel));
        }

        final String facet = facetParamMapper.getFacet(facetValue.toString());

        if (vloConfig.getFacetsInSearch().contains(facet)) {
            // create a new model so that all values will be retrieved
            setModel(new FacetFieldModel(facet, facetFieldsService, selectionModel)); // gets all facet values
        }
        if (getModelObject() == null) {
            Session.get().error(String.format("Facet '%s' could not be found", facet));
            ErrorPage.triggerErrorPage(ErrorPage.ErrorType.PAGE_NOT_FOUND, params);
        }

        addComponents();
    }

    public AllFacetValuesPage(IModel<FacetField> fieldModel, final IModel<QueryFacetsSelection> selectionModel, IModel<FacetSelectionType> selectionTypeModeModel) {
        super(fieldModel);
        this.selectionModel = selectionModel;
        this.selectionTypeModeModel = selectionTypeModeModel;
        addComponents();
    }

    private void addComponents() {
        add(new BreadCrumbPanel("breadcrumbs", selectionModel, getModelObject().getName()));

        add(new Label("name", new SolrFieldNameModel(new PropertyModel<String>(getModel(), "name"))));

        add(new AllFacetValuesPanel("values", getModel(), selectionTypeModeModel, selectionModel));

        add(new Form("submit") {
            @Override
            public void onSubmit() {
                // Redirect to search page with updated model
                final FacetedSearchPage searchPage = new FacetedSearchPage(parametersConverter.toParameters(selectionModel.getObject()));
                setResponsePage(searchPage);
            }

        });
    }

    @Override
    public void detachModels() {
        super.detachModels();
        selectionModel.detach();
        selectionTypeModeModel.detach();
    }

}
