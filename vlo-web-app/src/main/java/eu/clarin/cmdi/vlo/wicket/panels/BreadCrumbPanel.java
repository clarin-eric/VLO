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

import eu.clarin.cmdi.vlo.wicket.provider.FacetSelectionProvider;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.wicket.pages.FacetedSearchPage;
import java.util.Collection;
import java.util.Map;
import org.apache.wicket.Application;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author twagoo
 */
public abstract class BreadCrumbPanel extends GenericPanel<QueryFacetsSelection> {

    @SpringBean
    private PageParametersConverter<QueryFacetsSelection> paramsConverter;

    private final WebMarkupContainer query;
    private final WebMarkupContainer facets;

    public BreadCrumbPanel(String id, IModel<QueryFacetsSelection> model) {
        super(id, model);
        add(new BookmarkablePageLink("mainpage", Application.get().getHomePage()));
        add(query = createQuery(model, "query"));
        add(facets = createFacets(model, "facets"));

    }

    private WebMarkupContainer createQuery(final IModel<QueryFacetsSelection> selectionModel, String id) {
        final WebMarkupContainer queryContainer = new WebMarkupContainer(id);
        final Link link = new Link("leavequery") {

            @Override
            public void onClick() {
                // make query object without selection
                final QueryFacetsSelection newSelection = new QueryFacetsSelection(selectionModel.getObject().getQuery(), null);
                setResponsePage(FacetedSearchPage.class, paramsConverter.toParameters(newSelection));
            }
        };
        link.add(new Label("content", new PropertyModel(selectionModel, "query")));
        queryContainer.add(link);
        return queryContainer;
    }

    private WebMarkupContainer createFacets(IModel<QueryFacetsSelection> model, String id) {
        final WebMarkupContainer facetsContainer = new WebMarkupContainer(id);

        facetsContainer.add(new DataView<Map.Entry<String, Collection<String>>>("facet", new FacetSelectionProvider(model)) {

            @Override
            protected void populateItem(Item<Map.Entry<String, Collection<String>>> item) {
                final IModel<Map.Entry<String, Collection<String>>> selectionModel = item.getModel();
                item.add(new Label("name", new PropertyModel(selectionModel, "key")));
                item.add(new Label("value", new PropertyModel(selectionModel, "value")));
                item.add(new Link("removal") {

                    @Override
                    public void onClick() {
                        final Map.Entry<String, Collection<String>> selection = selectionModel.getObject();
                        onValuesUnselected(selection.getKey(), selection.getValue(), null);
                    }
                });
            }
        });

        return facetsContainer;
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        final String queryString = getModelObject().getQuery();
        final Map<String, Collection<String>> selection = getModelObject().getSelection();

        query.setVisible(queryString != null && !queryString.isEmpty());
        facets.setVisible(selection != null && !selection.isEmpty());
    }

    /**
     * Callback triggered when values have been removed from this facet
     *
     * @param facet name of the facet this panel represents
     * @param valuesRemoved removed values
     * @param target Ajax target allowing for a partial update. May be null
     * (fallback)!
     */
    protected abstract void onValuesUnselected(String facet, Collection<String> valuesRemoved, AjaxRequestTarget target);
}
