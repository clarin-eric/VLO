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

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentService;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldsModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;
import eu.clarin.cmdi.vlo.wicket.pages.FacetedSearchPage;
import java.util.Collection;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author twagoo
 */
public class SimpleSearchBrowsePanel extends GenericPanel<QueryFacetsSelection> {

    @SpringBean
    private FacetFieldsService facetFieldsService;
    @SpringBean
    private SolrDocumentService documentService;
    @SpringBean
    private VloConfig vloConfig;

    public SimpleSearchBrowsePanel(String id, IModel<QueryFacetsSelection> model) {
        super(id, model);

        final IModel<Long> documentCountModel = new AbstractReadOnlyModel<Long>() {

            @Override
            public Long getObject() {
                return documentService.getDocumentCount(getModel().getObject());
            }
        };

        final BookmarkablePageLink browseAllLink = new BookmarkablePageLink("browseAll", FacetedSearchPage.class);
        browseAllLink.add(new Label("recordCount", new StringResourceModel("simplesearch.allrecords", documentCountModel, new Object[]{})));
        add(browseAllLink);

        add(addFacets("facet"));

        setOutputMarkupId(true);

    }

    private Component addFacets(final String id) {
        //TODO: Filter text box
        final IModel<List<FacetField>> facetFieldsModel = new FacetFieldsModel(facetFieldsService, vloConfig.getSimpleSearchFacetFields(), getModel(), -1);
        final IModel<String> selectedFacetModel = new Model<String>(null);
        return new ListView<FacetField>(id, facetFieldsModel) {

            @Override
            protected void populateItem(final ListItem<FacetField> item) {
                // add a panel showing the values for selection (constrained by the current model)
                final FacetValuesPanel values = new FacetValuesPanel("values", item.getModel(), SimpleSearchBrowsePanel.this.getModel()) {

                    @Override
                    protected void onValuesSelected(String facet, Collection<String> values, AjaxRequestTarget target) {
                        // value selected, add to selection model then submit to search page
                        final IModel<QueryFacetsSelection> selectionModel = SimpleSearchBrowsePanel.this.getModel();
                        selectionModel.getObject().selectValues(facet, values);
                        setResponsePage(new FacetedSearchPage(selectionModel));
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
                select.add(new Label("name",
                        // friendly facet name based on name in FacetField
                        new SolrFieldNameModel(new PropertyModel(item.getModel(), "name"))));
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
        };
    }

}
