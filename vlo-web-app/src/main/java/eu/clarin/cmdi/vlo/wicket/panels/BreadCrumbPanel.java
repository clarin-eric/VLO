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

import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.components.SingleValueSolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.model.PermaLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;
import eu.clarin.cmdi.vlo.wicket.pages.FacetedSearchPage;
import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import java.util.Map;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import eu.clarin.cmdi.vlo.FieldKey;

/**
 * A panel representing the action trail that has lead to the current page in
 * its current state with links to revert to a previous state (not completely
 * linear given the nature of faceted browsing)
 *
 * @author twagoo
 */
public class BreadCrumbPanel extends GenericPanel<QueryFacetsSelection> {
    @SpringBean
    private FieldNameService fieldNameService;

    private final IModel<SolrDocument> documentModel;
    private final IModel<String> facetModel;

    public BreadCrumbPanel(String id, IModel<QueryFacetsSelection> model) {
        this(id, model, new Model<SolrDocument>(null));
    }

    public BreadCrumbPanel(String id, IModel<QueryFacetsSelection> selectionModel, String facet) {
        super(id, selectionModel);
        this.facetModel = Model.of(facet);
        this.documentModel = new Model<>(null);
    }

    public BreadCrumbPanel(String id, IModel<QueryFacetsSelection> selectionModel, IModel<SolrDocument> documentModel) {
        super(id, selectionModel);
        this.documentModel = documentModel;
        this.facetModel = new Model<>(null);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new BookmarkablePageLink("home", getApplication().getHomePage()));
        add(new BookmarkablePageLink("searchPage", FacetedSearchPage.class));

        // add 'search results' item
        final WebMarkupContainer results = new WebMarkupContainer("results") {
            @Override
            protected void onConfigure() {
                final String queryString = getModelObject().getQuery();
                final Map<String, FacetSelection> selection = getModelObject().getSelection();

                setVisible((queryString != null && !queryString.isEmpty())
                        || (selection != null && !selection.isEmpty()));
            }

        };
        results.add(new ExternalLink("resultsLink", new PermaLinkModel(FacetedSearchPage.class, getModel())));
        add(results);

        // add document item
        final WebMarkupContainer document = new WebMarkupContainer("document") {
            @Override
            protected void onConfigure() {
                setVisible(BreadCrumbPanel.this.documentModel.getObject() != null);
            }

        };
        document.add(new ExternalLink("documentLink", new PermaLinkModel(RecordPage.class, getModel(), documentModel))
                .add(new SingleValueSolrFieldLabel("documentTitle", documentModel, fieldNameService.getFieldName(FieldKey.NAME), getString("breadcrumbs.unnamedrecord"))));
        add(document);

        final WebMarkupContainer facet = new WebMarkupContainer("facet") {
            @Override
            protected void onConfigure() {
                setVisible(BreadCrumbPanel.this.facetModel.getObject() != null);
            }
        };
        facet.add(new Label("facetName", new SolrFieldNameModel(facetModel)));
        add(facet);
    }

    @Override
    public void detachModels() {
        super.detachModels();
        if (documentModel != null) {
            documentModel.detach();
        }
    }

}
