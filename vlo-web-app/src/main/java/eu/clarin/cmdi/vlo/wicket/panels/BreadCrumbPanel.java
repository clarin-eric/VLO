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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;
import eu.clarin.cmdi.vlo.wicket.pages.FacetedSearchPage;
import eu.clarin.cmdi.vlo.wicket.provider.FacetSelectionProvider;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.apache.wicket.AttributeModifier;
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
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

/**
 * A panel representing the action trail that has lead to the current page in
 * its current state with links to revert to a previous state (not completely
 * linear given the nature of faceted browsing)
 *
 * @author twagoo
 */
public class BreadCrumbPanel extends GenericPanel<QueryFacetsSelection> {

    @SpringBean(name="queryParametersConverter")
    private PageParametersConverter<QueryFacetsSelection> paramsConverter;

    private final WebMarkupContainer query;
    private final WebMarkupContainer facets;

    public BreadCrumbPanel(String id, IModel<QueryFacetsSelection> model) {
        super(id, model);
        add(new BookmarkablePageLink("mainpage", FacetedSearchPage.class));
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

    private WebMarkupContainer createFacets(final IModel<QueryFacetsSelection> model, String id) {
        final WebMarkupContainer facetsContainer = new WebMarkupContainer(id);
        facetsContainer.add(new Link("leaveselection") {

            @Override
            public void onClick() {
                setResponsePage(FacetedSearchPage.class, paramsConverter.toParameters(model.getObject()));
            }
        });

        // create a provider that lists the facet name -> values entries
        final FacetSelectionProvider facetSelectionProvider = new FacetSelectionProvider(model);
        facetsContainer.add(new DataView<Map.Entry<String, FacetSelection>>("facet", facetSelectionProvider) {

            @Override
            protected void populateItem(final Item<Map.Entry<String, FacetSelection>> item) {
                final IModel<Map.Entry<String, FacetSelection>> selectionModel = item.getModel();
                // add a label for the selected facet value(s)
                final Label valueLabel = new Label("value", new PropertyModel(selectionModel, "value")) {

                    @Override
                    public <C> IConverter<C> getConverter(Class<C> type) {
                        // converter to render the value(s) nicely
                        return (IConverter<C>) new SelectionConverter(item.getModelObject().getKey());
                    }

                };
                // add facet name as title attribute so that it becomes available through a tooltip
                valueLabel.add(new AttributeModifier("title",
                        new SolrFieldNameModel(new PropertyModel(selectionModel, "key"))));
                item.add(valueLabel);

                // add a link for removal of the facet value selection
                item.add(new Link("removal") {

                    @Override
                    public void onClick() {
                        // get a copy of the current selection
                        final QueryFacetsSelection newSelection = model.getObject().getCopy();
                        final String facet = selectionModel.getObject().getKey();
                        // unselect this facet
                        newSelection.selectValues(facet, null);
                        setResponsePage(FacetedSearchPage.class, paramsConverter.toParameters(newSelection));
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
        final Map<String, FacetSelection> selection = getModelObject().getSelection();

        query.setVisible(queryString != null && !queryString.isEmpty());
        facets.setVisible(selection != null && !selection.isEmpty());
    }

    /**
     * Converter for string collections, rendering depends on items in
     * collection (if singleton, show its value; if multiple, comma separated)
     */
    private class SelectionConverter implements IConverter<FacetSelection> {

        private final String facet;

        public SelectionConverter(String facet) {
            this.facet = facet;
        }

        @Override
        public FacetSelection convertToObject(String value, Locale locale) throws ConversionException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String convertToString(FacetSelection selection, Locale locale) {
            switch (selection.getSelectionType()) {
                case AND:
                    return getCollectionString(selection, " and ");
                case OR:
                    return getCollectionString(selection, " or ");
                case NOT:
                    return "not [" + getCollectionString(selection, " or ") + "]";
                case NOT_EMPTY:
                    return getAnyValueString();
                default:
                    return facet;
            }

        }

        private String getAnyValueString() {
            if (FacetConstants.FIELD_SEARCH_SERVICE.equals(facet)) {
                return "Content searchable";
            }
            return "any " + facet;
        }

        public String getCollectionString(FacetSelection selection, String valueSeparator) {
            final Collection<String> value = selection.getValues();
            //TODO: include selection type
            if (value.isEmpty()) {
                return "";
            } else if (value.size() == 1) {
                return value.iterator().next();
            } else {
                final Iterator<String> iterator = value.iterator();
                final StringBuilder sb = new StringBuilder(iterator.next());
                while (iterator.hasNext()) {
                    sb.append(valueSeparator).append(iterator.next());
                }
                return sb.toString();
            }
        }

    };
}
