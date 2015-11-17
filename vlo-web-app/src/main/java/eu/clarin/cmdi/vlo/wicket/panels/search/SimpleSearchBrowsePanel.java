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

import java.util.Collection;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.migrate.StringResourceModelMigration;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldsModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldDescriptionModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;
import eu.clarin.cmdi.vlo.wicket.pages.FacetedSearchPage;
import eu.clarin.cmdi.vlo.wicket.pages.SimpleSearchPage;

/**
 * Panel to be shown on {@link SimpleSearchPage} that has a number of links for
 * browsing the records; either all records or by making a value selection in
 * one of a number of predefined facets
 *
 * @author twagoo
 */
public class SimpleSearchBrowsePanel extends GenericPanel<QueryFacetsSelection> {

    @SpringBean
    private SolrDocumentService documentService;

    /**
     *
     * @param id component id
     * @param model model of current selection
     */
    public SimpleSearchBrowsePanel(String id, IModel<QueryFacetsSelection> model) {
        super(id, model);

        final IModel<Long> documentCountModel = new AbstractReadOnlyModel<Long>() {

            @Override
            public Long getObject() {
                return documentService.getDocumentCount(getModel().getObject());
            }
        };

        // add a link to browse all records
        final Link browseAllLink = new Link("browseAll") {

            @Override
            public void onClick() {
                setResponsePage(new FacetedSearchPage(SimpleSearchBrowsePanel.this.getModel()));
            }
        };
        // set label on basis of string defined in resource bundle that takes the count model as a parameter
        browseAllLink.add(new Label("recordCount", StringResourceModelMigration.of("simplesearch.allrecords", documentCountModel, new Object[]{})));
        add(browseAllLink);

        // add selectors for some facets
        add(new FacetSelectorsView("facet", getModel()));

        // make this panel AJAX-updatable
        setOutputMarkupId(true);
    }

    /**
     * List model of links that open up {@link FacetValuesPanel}s for a number
     * of facets. Which facets are included is based on the value returned by
     * {@link VloConfig#getSimpleSearchFacetFields() } in the {@link VloConfig}
     * instance injected into this instance.
     */
    private class FacetSelectorsView extends ListView<FacetField> {

        @SpringBean
        private FacetFieldsService facetFieldsService;
        @SpringBean(name = "queryParametersConverter")
        private PageParametersConverter<QueryFacetsSelection> paramsConverter;
        @SpringBean
        private VloConfig vloConfig;

        /**
         * Model that holds the currently selected facet
         */
        private final IModel<String> selectedFacetModel = new Model<>(null);
        private final IModel<QueryFacetsSelection> selectionModel;

        public FacetSelectorsView(String id, IModel<QueryFacetsSelection> selectionModel) {
            super(id);
            this.selectionModel = selectionModel;
            setModel(new FacetFieldsModel(facetFieldsService, vloConfig.getSimpleSearchFacetFields(), selectionModel, -1));
        }

        @Override
        protected void populateItem(final ListItem<FacetField> item) {
            // add a panel showing the values for selection (constrained by the current model)
            final FacetValuesPanel values = new FacetValuesPanel("values", item.getModel(), selectionModel) {

                @Override
                protected void onValuesSelected(Collection<String> values, AjaxRequestTarget target) {
                    // value selected, make a new selection (in this panel we do not want to change the existing selection)...
                    final QueryFacetsSelection newSelection = selectionModel.getObject().getCopy();
                    newSelection.selectValues(getModelObject().getName(), new FacetSelection(values));
                    // ...then submit to search page
                    setResponsePage(FacetedSearchPage.class, paramsConverter.toParameters(newSelection));
                }

            };
            // wrap in a container that is only visible if this is the selected facet
            final WebMarkupContainer valuesContainer = new WebMarkupContainer("valuesContainer") {

                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setVisible(item.getModelObject().getName().equals(selectedFacetModel.getObject()));
                }
            };
            valuesContainer.add(values);
            item.add(valuesContainer);

            // add a link for selecting this facet
            final AjaxFallbackLink select = new AjaxFallbackLink("select") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    final String facetName = item.getModelObject().getName();
                    if (facetName.equals(selectedFacetModel.getObject())) {
                        // already selected, hide
                        selectedFacetModel.setObject(null);
                    } else {
                        // set this facet as the selected one
                        selectedFacetModel.setObject(facetName);
                    }

                    if (target != null) {
                        // AJAX update
                        target.add(SimpleSearchBrowsePanel.this);
                    }
                }
            };
            
            // add name label (with a description title attribute)
            final PropertyModel facetNameModel = new PropertyModel(item.getModel(), "name");
            // wrap in field name model to get a friendly facet name based on name in FacetField
            final Label name = new Label("name", new SolrFieldNameModel(facetNameModel));
            select.add(name);
            
            // add title attribute to get the facet description in a tooltip
            select.add(new AttributeAppender("title", new SolrFieldDescriptionModel(facetNameModel)));
            
            item.add(select);

            // show a separator except for the last item
            item.add(new WebMarkupContainer("separator") {

                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setVisible(item.getIndex() + 1 < getList().size());
                }
            });
        }
    }

}
