/*
 * Copyright (C) 2016 CLARIN
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

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionValueQualifier;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionPair;
import eu.clarin.cmdi.vlo.wicket.BooleanVisibilityBehavior;
import eu.clarin.cmdi.vlo.wicket.model.FormattedStringModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;
import eu.clarin.cmdi.vlo.wicket.pages.FacetedSearchPage;
import static eu.clarin.cmdi.vlo.wicket.panels.search.SearchResultsPanel.ITEMS_PER_PAGE_OPTIONS;
import eu.clarin.cmdi.vlo.wicket.provider.FacetSelectionProvider;
import eu.clarin.cmdi.vlo.wicket.provider.FieldValueConverterProvider;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.navigation.paging.IPageableItems;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.AbstractPageableView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class SearchResultsHeaderPanel extends GenericPanel<QueryFacetsSelection> {

    @SpringBean(name = "queryParametersConverter")
    private PageParametersConverter<QueryFacetsSelection> paramsConverter;
    @SpringBean
    private FieldValueConverterProvider fieldValueConverterProvider;
    @SpringBean
    private FieldNameService fieldNameService;

    private final IDataProvider<SolrDocument> solrDocumentProvider;
    private final AbstractPageableView<SolrDocumentExpansionPair> resultsView;

    public final static String RECORD_COUNT_NUMBER_FORMAT = "%,d";

    public SearchResultsHeaderPanel(String id, IModel<QueryFacetsSelection> model, AbstractPageableView<SolrDocumentExpansionPair> resultsView, IDataProvider<SolrDocument> solrDocumentProvider, IModel<Long> recordCountModel) {
        super(id, model);

        this.solrDocumentProvider = solrDocumentProvider;
        this.resultsView = resultsView;

        final IModel<Boolean> emptyFacetSelectionModelModel = () -> getModelObject().getSelection() == null || getModelObject().getSelection().isEmpty();
        final IModel<Boolean> emptyQueryModel = () -> getModel().getObject().getQuery() == null || getModel().getObject().getQuery().isEmpty();
        final IModel<Boolean> noQueryOrSelectionModel = () -> emptyQueryModel.getObject() && emptyFacetSelectionModelModel.getObject();
        final IModel<Long> resultCountModel = () -> solrDocumentProvider.size();
        final IModel<Boolean> queryWithNoResultsModel = () -> !noQueryOrSelectionModel.getObject() && resultCountModel.getObject() == 0;
        final IModel<Boolean> queryWithResultsModel = () -> !noQueryOrSelectionModel.getObject() && resultCountModel.getObject() > 0;

        //Showing all records (${} results)
        add(new Label("searchInfoAll", new StringResourceModel("searchresults.showingAll", this, resultCountModel))
                .add(BooleanVisibilityBehavior.visibleOnTrue(noQueryOrSelectionModel))
        );
        //No results for
        add(new Label("searchInfoNoResults", new StringResourceModel("searchresults.noResults"))
                .add(BooleanVisibilityBehavior.visibleOnTrue(queryWithNoResultsModel))
        );
        //Showing {} to {} of {} results {} for
        add(new Label("searchInfoResults", new StringResourceModel("searchresults.results.${pageMode}.${singular}.${selectionMode}", this, () -> new ResultPaginationInfo(this.resultsView, resultCountModel, emptyFacetSelectionModelModel)))
                .add(BooleanVisibilityBehavior.visibleOnTrue(queryWithResultsModel))
        );

        add(createQuerySelectionItems("querySelection"));

        // form to select number of results per page
        add(createResultPageSizeForm("resultPageSizeForm", resultsView));

        final IModel<Long> duplicateCountModel = new LoadableDetachableModel<Long>() {
            @Override
            public Long load() {
                return recordCountModel.getObject() - solrDocumentProvider.size();
            }

            @Override
            protected void onDetach() {
                recordCountModel.detach();
            }

        };

        add(new Label("recordCount", new FormattedStringModel<>(RECORD_COUNT_NUMBER_FORMAT, recordCountModel)));
        add(new Label("duplicateCount", new FormattedStringModel<>(RECORD_COUNT_NUMBER_FORMAT, duplicateCountModel)) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(duplicateCountModel.getObject() > 0);
            }

        }
        );

        //For Ajax updating of search results
        setOutputMarkupId(true);
    }

    private Component createQuerySelectionItems(String id) {
        final Component query = createQueryItem("query");
        final Component facets = createFacetItems("facets");

        final WebMarkupContainer container = new WebMarkupContainer(id) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                final String queryString = getModelObject().getQuery();
                final Map<String, FacetSelection> selection = getModelObject().getSelection();
                final boolean hasQuery = queryString != null && !queryString.isEmpty();
                final boolean hasSelection = selection != null && !selection.isEmpty();

                setVisible(hasQuery || hasSelection);
                query.setVisible(hasQuery);
                facets.setVisible(hasSelection);
            }
        };

        return container
                .add(query)
                .add(facets);
    }

    private Component createQueryItem(String id) {
        final Label label = new Label("label", new PropertyModel<>(getModel(), "query"));

        //ajax indicator should go behind label even though behaviour is triggered by remove link...
        final AjaxIndicatorAppender ajaxIndicatorAppender = new AjaxIndicatorAppender();
        label.add(ajaxIndicatorAppender);

        final AjaxFallbackLink<QueryFacetsSelection> removeLink = new IndicatingAjaxFallbackLink<QueryFacetsSelection>("remove", getModel()) {

            @Override
            public void onClick(Optional<AjaxRequestTarget> target) {
                // get a copy of the current selection
                final QueryFacetsSelection newSelection = getModelObject().copy();
                newSelection.setQuery(null);
                onSelectionChanged(newSelection, target);
            }

            @Override
            public String getAjaxIndicatorMarkupId() {
                return ajaxIndicatorAppender.getMarkupId();
            }

        };
        final Component query = new WebMarkupContainer(id)
                .add(label)
                .add(removeLink)
                .add(new AttributeModifier("placeholder", () -> "Search through 100,000 records"));
        return query;
    }

    private Component createFacetItems(String id) {
        final WebMarkupContainer facets = new WebMarkupContainer(id);
        // create a provider that lists the facet name -> values entries
        final FacetSelectionProvider facetSelectionProvider = new FacetSelectionProvider(getModel());
        facets.add(new DataView<Map.Entry<String, FacetSelection>>("facet", facetSelectionProvider) {

            @Override
            protected void populateItem(final Item<Map.Entry<String, FacetSelection>> item) {
                final IModel<Map.Entry<String, FacetSelection>> selectionModel = item.getModel();
                // add a label for the selected facet value(s)
                final Label valueLabel = new Label("label", new PropertyModel(selectionModel, "value")) {

                    @Override
                    public <C> IConverter<C> getConverter(Class<C> type) {
                        final String facet = item.getModelObject().getKey();
                        // converter to render the value(s) nicely
                        return (IConverter<C>) new SelectionConverter(facet, fieldValueConverterProvider.getConverter(facet));
                    }

                };
                // add facet name as title attribute so that it becomes available through a tooltip
                valueLabel.add(new AttributeModifier("title",
                        new SolrFieldNameModel(new PropertyModel<>(selectionModel, "key"))));

                //ajax indicator should go behind label even though behaviour is triggered by remove link...
                final AjaxIndicatorAppender ajaxIndicatorAppender = new AjaxIndicatorAppender();
                valueLabel.add(ajaxIndicatorAppender);

                item.add(valueLabel);

                // add a link for removal of the facet value selection
                item.add(new IndicatingAjaxFallbackLink<QueryFacetsSelection>("remove", getModel()) {

                    @Override
                    public void onClick(Optional<AjaxRequestTarget> target) {
                        // get a copy of the current selection
                        final QueryFacetsSelection newSelection = getModelObject().copy();
                        final String facet = selectionModel.getObject().getKey();
                        // unselect this facet
                        newSelection.selectValues(facet, null);
                        onSelectionChanged(newSelection, target);
                    }

                    @Override
                    public String getAjaxIndicatorMarkupId() {
                        return ajaxIndicatorAppender.getMarkupId();
                    }

                });
            }
        });
        return facets;
    }

    private Form createResultPageSizeForm(String id, final IPageableItems resultsView) {
        final Form resultPageSizeForm = new Form(id);

        final DropDownChoice<Long> pageSizeDropDown
                = new DropDownChoice<>("resultPageSize",
                        // bind to items per page property of pageable
                        new PropertyModel<>(resultsView, "itemsPerPage"),
                        ITEMS_PER_PAGE_OPTIONS);
        pageSizeDropDown.add(new AjaxFormComponentUpdatingBehavior("change") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                onChange(Optional.of(target));
            }
        });
        resultPageSizeForm.add(pageSizeDropDown);

        return resultPageSizeForm;
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        solrDocumentProvider.detach();
    }

    protected void onChange(Optional<AjaxRequestTarget> target) {
        //noop - may be overridden
    }

    /*
     * Gets called if one of the links is clicked and the selection is changed.
     * This implementation sets the response page to {@link FacetedSearchPage}
     * with the new selection as its parameters
     *
     * @param selection new selection
     * @param target AJAX target, may be null
     */
    protected void onSelectionChanged(QueryFacetsSelection selection, Optional<AjaxRequestTarget> target) {
        setResponsePage(FacetedSearchPage.class, paramsConverter.toParameters(selection));
    }

    // Converter for string collections, rendering depends on items in
    // collection (if singleton, show its value; if multiple, comma separated)
    private class SelectionConverter implements IConverter<FacetSelection> {

        private final String facet;
        private final IConverter<String> valueConverter;

        public SelectionConverter(String facet, IConverter<String> valueConverter) {
            this.facet = facet;
            this.valueConverter = valueConverter;
        }

        @Override
        public FacetSelection convertToObject(String value, Locale locale) throws ConversionException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String convertToString(FacetSelection selection, Locale locale) {
            switch (selection.getSelectionType()) {
                case AND:
                    return getCollectionString(selection, " and ", locale);
                case OR:
                    return getCollectionString(selection, " or ", locale);
                case NOT_EMPTY:
                    return getAnyValueString();
                default:
                    return facet;
            }

        }

        private String getAnyValueString() {
            if (null != facet) {

                if (facet.equals(fieldNameService.getFieldName(FieldKey.SEARCH_SERVICE))) {
                    return "Content searchable"; //TODO: make string property
                }
                if (facet.equals(fieldNameService.getFieldName(FieldKey.HAS_PART_COUNT))) {
                    return "Collection records"; //TODO: make string property
                }
                return "any " + facet;

            }
            return "";
        }

        public String getCollectionString(FacetSelection selection, String valueSeparator, Locale locale) {
            final Collection<String> value = selection.getValues();
            //TODO: include selection type
            if (value.isEmpty()) {
                return "";
            } else if (value.size() == 1) {
                return getConvertedValueString(selection, value.iterator().next(), locale).toString();
            } else {
                final Iterator<String> iterator = value.iterator();
                final StringBuilder sb = new StringBuilder(getConvertedValueString(selection, iterator.next(), locale));
                while (iterator.hasNext()) {
                    sb.append(valueSeparator).append(getConvertedValueString(selection, iterator.next(), locale));
                }
                return sb.toString();
            }
        }

        private CharSequence getConvertedValueString(FacetSelection selection, String string, Locale locale) {
            if (selection.getQualifier(string) == FacetSelectionValueQualifier.NOT) {
                return new StringBuilder("NOT ").append(getConvertedValue(string, locale));
            }
            return getConvertedValue(string, locale);
        }

        private String getConvertedValue(String string, Locale locale) {
            if (valueConverter != null) {
                final String converted = valueConverter.convertToString(string, locale);
                if (converted != null) {
                    return converted;
                }
            }
            return string;
        }

    };

    public final class ResultPaginationInfo implements Serializable {

        private final long from;
        private final long to;
        private final long count;
        private final String pageMode;
        private final String selectionMode;
        private final String singular;

        public ResultPaginationInfo(AbstractPageableView<SolrDocumentExpansionPair> resultsView, IModel<Long> resultSize, IModel<Boolean> emptyFacetSelectionModelModel) {
            from = 1 + resultsView.getCurrentPage() * resultsView.getItemsPerPage();
            to = Math.min(resultsView.getItemCount(), from + resultsView.getItemsPerPage() - 1);
            this.count = resultSize.getObject();
            this.pageMode = Optional.ofNullable(resultsView.getPageCount()).orElse(2L) > 1 ? "multipage" : "onepage";
            this.selectionMode = Optional.ofNullable(emptyFacetSelectionModelModel.getObject()).orElse(true) ? "withoutselection" : "withinselection";
            this.singular = count == 1 ? "singular" : "plural";
        }

        public long getFrom() {
            return from;
        }

        public long getTo() {
            return to;
        }

        public long getCount() {
            return count;
        }

        public String getPageMode() {
            return pageMode;
        }

        public String getSelectionMode() {
            return selectionMode;
        }

        public String getSingular() {
            return singular;
        }

    };
}
