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

import eu.clarin.cmdi.vlo.JavaScriptResources;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.wicket.model.PermaLinkModel;
import eu.clarin.cmdi.vlo.wicket.panels.search.SearchFormPanel;
import eu.clarin.cmdi.vlo.wicket.panels.SingleFacetPanel;
import eu.clarin.cmdi.vlo.wicket.panels.TopLinksPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.SimpleSearchBrowsePanel;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author twagoo
 */
public class SimpleSearchPage extends VloBasePage<QueryFacetsSelection> {

    @SpringBean
    private VloConfig vloConfig;
    @SpringBean
    private FacetFieldsService facetFieldsService;

    private final Component collectionsPanel;
    private final MarkupContainer navigation;
    private final MarkupContainer browse;

    public SimpleSearchPage(PageParameters parameters) {
        super(parameters);

        // if a query selection is passed in, redirect to search page
        if (!parameters.isEmpty()) {
            throw new RestartResponseException(FacetedSearchPage.class, parameters);
        }

        final Model<QueryFacetsSelection> model = Model.of(new QueryFacetsSelection());
        setModel(model);

        // add an updatable container for breadcrumbs and top links
        navigation = createNavigation("navigation");
        add(navigation);

        // add a persistenet panel for selection of a value for the collection facet
        collectionsPanel = createCollectionsPanel("collectionsFacet");
        collectionsPanel.setOutputMarkupId(true);
        add(collectionsPanel);

        // add a search form (on submit will go to faceted search page)
        add(new SearchFormPanel("search", model) {

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                setResponsePage(new FacetedSearchPage(model));
            }
        });

        // add a panel with browsing options
        browse = new SimpleSearchBrowsePanel("browse", getModel());
        add(browse);
    }

    private WebMarkupContainer createNavigation(String id) {
        final WebMarkupContainer container = new WebMarkupContainer(id);
        container.setOutputMarkupId(true);
        container.add(new BookmarkablePageLink("breadcrumb", getApplication().getHomePage()));
        container.add(new TopLinksPanel("topLinks", new PermaLinkModel(getPageClass(), getModel())) {

            @Override
            protected void onChange(AjaxRequestTarget target) {
                if (target != null) {
                    target.add(container);
                }
            }

        });
        return container;
    }

    private Component createCollectionsPanel(String id) {
        // collection facet is optional...
        if (vloConfig.getCollectionFacet() != null) {
            return new SingleFacetPanel(id, vloConfig.getCollectionFacet(), getModel(), facetFieldsService, 3) {

                @Override
                protected void selectionChanged(AjaxRequestTarget target) {
                    if (target != null) {
                        target.add(navigation);
                        target.add(collectionsPanel);
                        target.add(browse);
                    }
                }
            };
        } else {
            // no collection facet, do not add the panel
            final WebMarkupContainer placeholder = new WebMarkupContainer(id);
            placeholder.setVisible(false);
            return placeholder;
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(JavaScriptResources.getVloFrontJS()));
    }
}
