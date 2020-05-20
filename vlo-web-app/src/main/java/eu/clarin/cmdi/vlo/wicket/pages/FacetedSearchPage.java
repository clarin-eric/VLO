package eu.clarin.cmdi.vlo.wicket.pages;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.JavaScriptResources;
import eu.clarin.cmdi.vlo.PiwikEventConstants;
import eu.clarin.cmdi.vlo.VloWebSession;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.PiwikConfig;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.FieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.ExposureTracker;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionPair;
import eu.clarin.cmdi.vlo.wicket.AjaxPiwikTrackingBehavior;
import eu.clarin.cmdi.vlo.wicket.JsonLdHeaderBehavior;
import eu.clarin.cmdi.vlo.wicket.SitelinkSearchboxHeaderBehavior;
import eu.clarin.cmdi.vlo.wicket.historyapi.HistoryApiAware;
import eu.clarin.cmdi.vlo.wicket.model.BooleanOptionsModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldsModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetNamesModel;
import eu.clarin.cmdi.vlo.wicket.model.JsonLdModel;
import eu.clarin.cmdi.vlo.wicket.model.PermaLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.RecordCountModel;
import eu.clarin.cmdi.vlo.wicket.panels.BreadCrumbPanel;
import eu.clarin.cmdi.vlo.wicket.panels.CopyPageLinkPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.AdvancedSearchOptionsPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.AvailabilityFacetPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.FacetsPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.SearchFormPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.SearchResultsHeaderPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.SearchResultsPanel;
import eu.clarin.cmdi.vlo.wicket.provider.SolrDocumentExpansionPairProvider;
import eu.clarin.cmdi.vlo.wicket.provider.SolrDocumentProviderAdapter;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.AbstractPageableView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.util.comparator.Comparators;

/**
 * The main search page showing a search form, facets, and search results
 *
 * @author twagoo
 */
public class FacetedSearchPage extends VloBasePage<QueryFacetsSelection> implements HistoryApiAware {

    private static final long serialVersionUID = 1L;
    //private final static List<String> ADDITIONAL_FACETS = ImmutableList.of(FacetConstants.FIELD_LICENSE_TYPE);
    private final static FieldKey ADDITIONAL_FACETS = FieldKey.LICENSE_TYPE;

    @SpringBean
    private FacetFieldsService facetFieldsService;
    @SpringBean
    private ExposureTracker exposureTracker;
    @SpringBean
    private VloConfig vloConfig;
    @SpringBean
    private PiwikConfig piwikConfig;
    @SpringBean(name = "queryParametersConverter")
    private PageParametersConverter<QueryFacetsSelection> paramsConverter;
    @SpringBean
    private FieldNameService fieldNameService;

    /**
     * Provider of search results including 'expansion' of collapsed (very
     * similar) records
     */
    private IDataProvider<SolrDocumentExpansionPair> documentsProvider;

    /**
     * Provider of search results without expansion of collapsed records
     */
    private IDataProvider<SolrDocument> solrDocumentsProvider;

    private MarkupContainer searchContainer;
    private SearchResultsPanel searchResultsPanel;
    private Component facetsPanel;
    private Component navigation;
    private Component searchForm;
    private Component optionsPanel;
    private Component availabilityFacetPanel;
    private Component resultsHeader;
    private MarkupContainer selections;

    private IModel<List<String>> facetNamesModel;
    private FacetFieldsModel fieldsModel;
    private IModel<FacetSelectionType> facetSelectionTypeModeModel;
    private IModel<Boolean> simpleModeModel;
    private IModel<Long> recordCountModel;
    private IModel<HashMap<String, FieldValuesFilter>> facetValuesFiltersModel;

    private IModel<String> searchResultsTitleModel;

    public FacetedSearchPage(IModel<QueryFacetsSelection> queryModel) {
        this(queryModel, Model.of(false));
    }

    public FacetedSearchPage(PageParameters parameters) {
        this(parameters, Model.of(false));
    }

    public FacetedSearchPage(IModel<QueryFacetsSelection> queryModel, IModel<Boolean> simpleModeModel) {
        super(queryModel);

        createModels();
        this.simpleModeModel = simpleModeModel;

        addComponents();
    }

    public FacetedSearchPage(PageParameters parameters, IModel<Boolean> simpleModeModel) {
        super(parameters);

        final QueryFacetsSelection selection = paramsConverter.fromParameters(parameters);
        final IModel<QueryFacetsSelection> queryModel = new Model<>(selection);

        setModel(queryModel);

        createModels();
        this.simpleModeModel = simpleModeModel;

        addComponents();

        // add Piwik tracking behavior
        if (piwikConfig.isEnabled()) {
            resultsHeader.add(AjaxPiwikTrackingBehavior.newPageViewTrackingBehavior(PiwikEventConstants.PIWIK_PAGEVIEW_SEARCH));
        }

        add(new SitelinkSearchboxHeaderBehavior());

        // add schema.org DataCatalog metadata (header)
        final IModel<JsonLdModel.JsonLdObject> dataCatalogMetadata = Model.of(new DataCatalog(vloConfig.getHomeUrl()));
        add(new JsonLdHeaderBehavior(new JsonLdModel(dataCatalogMetadata)));

    }

    private void createModels() {
        final List<String> facetFields = vloConfig.getFacetFieldNames();
        final List<String> allFields = ImmutableList.copyOf(Iterables.concat(facetFields, ImmutableList.of(fieldNameService.getFieldName(ADDITIONAL_FACETS))));

        facetNamesModel = new FacetNamesModel(facetFields);
        recordCountModel = new RecordCountModel(getModel());
        searchResultsTitleModel = new StringResourceModel("pageTitle.searchResults", FacetedSearchPage.this, super.getTitleModel());

        facetValuesFiltersModel = new Model<>(new HashMap<>());
        final IModel<Integer> facetValueLimitModel = () -> {
            // are there any facet value filters?
            if (facetValuesFiltersModel.getObject().values().stream()
                    .anyMatch(Predicates.not(map -> map.isEmpty()))) {
                return -1; //no limit, because filter has to be applied to all
            } else {
                //increase by number of selected values to make sure we always get the number of remaining values
                final OptionalInt maxSelection
                        = Optional.ofNullable(getModelObject().getSelection())
                                .map(s
                                        -> s.values().stream()
                                        .mapToInt(fs -> fs.getValues().size())
                                        .max()
                                ).orElse(OptionalInt.empty());

                return vloConfig.getMaxNumberOfFacetsToShow() + maxSelection.orElse(0) + 1;
            }
        };
        fieldsModel = new FacetFieldsModel(facetFieldsService, allFields, getModel(), facetValueLimitModel);

        final FacetSelectionType initialSelectionType = getFacetSelectionTypeModeFromSessionOrDefault();
        facetSelectionTypeModeModel = new Model<FacetSelectionType>(initialSelectionType) {
            @Override
            public void setObject(FacetSelectionType object) {
                super.setObject(object);
                //persist in session
                VloWebSession.get().setFacetSelectionTypeMode(object);
            }

        };
    }

    private FacetSelectionType getFacetSelectionTypeModeFromSessionOrDefault() {
        final FacetSelectionType sessionValue = VloWebSession.get().getFacetSelectionTypeMode();
        if (sessionValue == null) {
            return FacetSelectionType.OR;
        } else {
            return (FacetSelectionType) sessionValue;
        }
    }

    private void addComponents() {
        documentsProvider = new SolrDocumentExpansionPairProvider(getModel(), fieldNameService, exposureTracker);
        solrDocumentsProvider = new SolrDocumentProviderAdapter(documentsProvider, fieldNameService);
        searchContainer = new WebMarkupContainer("searchContainer");
        searchContainer.add(new AttributeModifier("class", new IModel<>() {
            @Override
            public String getObject() {
                return simpleModeModel.getObject() ? "simple" : "";
            }

        }));
        searchContainer.setOutputMarkupId(true);
        add(searchContainer);

        navigation = createNavigation("navigation");
        searchContainer.add(navigation);

        searchContainer.add(new BookmarkablePageLink<Void>("searchpage.greeting.description.link", FacetedSearchPage.class, new PageParameters()));

        searchForm = createSearchForm("search");
        searchContainer.add(searchForm);

        //selections panel (facets and options)
        selections = new WebMarkupContainer("selections") {

            @Override
            protected void onConfigure() {
                super.onConfigure();
                final Map<String, FacetSelection> facetSelection = FacetedSearchPage.this.getModel().getObject().getSelection();
                setVisible(documentsProvider.size() > 0
                        || (facetSelection != null && !facetSelection.isEmpty()));
            }
        };
        facetsPanel = createFacetsPanel("facets");
        availabilityFacetPanel = createAvailabilityPanel("availability");
        optionsPanel = createOptionsPanel("options");

        searchContainer.add(selections
                .add(facetsPanel)
                .add(availabilityFacetPanel)
                .add(optionsPanel)
                .setOutputMarkupPlaceholderTag(true)
        );

        // make "selections" panel collapsable on smaller screens
        final IModel<Boolean> selectionsExpandedModel = Model.of(false);
        searchContainer.add(new IndicatingAjaxFallbackLink<Void>("toggleSelections") {
            @Override
            public void onClick(Optional<AjaxRequestTarget> target) {
                selectionsExpandedModel.setObject(!selectionsExpandedModel.getObject());
                target.ifPresent(t -> {
                    t.add(selections);
                    t.add(this);
                });
            }
        }
                .add(new Label("toggleSelectionsLabel", // dynamic button label 
                        new BooleanOptionsModel<>(selectionsExpandedModel,
                                Model.of("Hide facets and search options"),
                                Model.of("Show facets and search options"))))
                .setOutputMarkupId(true)
        );
        selections.add(new AttributeAppender("class", // set style for collapsed
                new BooleanOptionsModel<>(selectionsExpandedModel,
                        Model.of(""),
                        Model.of("collapsed-xs-sm")),
                " "));

        //search results panel and header
        searchResultsPanel = new SearchResultsPanel("searchResults", getModel(), documentsProvider) {

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(documentsProvider.size() > 0);
            }

            @Override
            protected void onAjaxSearchPagination(AjaxRequestTarget target) {
                super.onAjaxSearchPagination(target);
                //updating record offset in search result header
                target.add(resultsHeader);
            }

        };
        searchContainer.add(searchResultsPanel.setOutputMarkupPlaceholderTag(true));

        final AbstractPageableView<SolrDocumentExpansionPair> resultsView = searchResultsPanel.getResultsView();

        resultsHeader = createResultsHeader("searchresultsheader", getModel(), resultsView, solrDocumentsProvider, recordCountModel);
        searchContainer.add(resultsHeader.setOutputMarkupId(true));
    }

    private SearchResultsHeaderPanel createResultsHeader(String id, IModel<QueryFacetsSelection> model, AbstractPageableView<SolrDocumentExpansionPair> resultsView, IDataProvider<SolrDocument> solrDocumentProvider, IModel<Long> recordCountModel) {
        return new SearchResultsHeaderPanel(id, model, resultsView, solrDocumentProvider, recordCountModel) {
            @Override
            protected void onChange(Optional<AjaxRequestTarget> target) {
                updateSelection(target);
            }

            @Override
            protected void onSelectionChanged(QueryFacetsSelection selection, Optional<AjaxRequestTarget> target) {
                setModelObject(selection);
                updateSelection(target);
            }

        };
    }

    private WebMarkupContainer createNavigation(String id) {
        final WebMarkupContainer container = new WebMarkupContainer(id);
        container.setOutputMarkupId(true);
        container.add(new BreadCrumbPanel("breadcrumbs", getModel()));
        container.add(new CopyPageLinkPanel("permalink", new PermaLinkModel(getPageClass(), getModel()), getTitleModel()) {

            @Override
            protected void onChange(Optional<AjaxRequestTarget> target) {
                target.ifPresent(t -> {
                    t.add(container);
                });
            }

        });
        return container;
    }

    private Panel createOptionsPanel(String id) {
        final Panel panel = new AdvancedSearchOptionsPanel(id, getModel(), facetSelectionTypeModeModel, solrDocumentsProvider) {

            @Override
            protected void selectionChanged(Optional<AjaxRequestTarget> target) {
                updateSelection(target);
            }
        };
        panel.setOutputMarkupId(true);
        return panel;
    }

    private Panel createAvailabilityPanel(String id) {
        final Panel availabilityPanel = new AvailabilityFacetPanel(id, getModel(), fieldsModel) {

            @Override
            protected void selectionChanged(Optional<AjaxRequestTarget> target) {
                updateSelection(target);
            }
        };
        availabilityPanel.setOutputMarkupId(true);
        return availabilityPanel;
    }

    private Panel createSearchForm(String id) {
        final SearchFormPanel form = new SearchFormPanel(id, getModel(), recordCountModel) {

            @Override
            protected void onSubmit(Optional<AjaxRequestTarget> target) {
                // reset expansion state of search results
                searchResultsPanel.resetExpansion();

                //transition from simple
                simpleModeModel.setObject(false);
                target.ifPresent(t -> {
                    t.prependJavaScript("cb|transitionFromSimple(cb);");
                    t.add(searchContainer); //update everything within container
                });

                updateSelection(target);
            }

        };
        form.setOutputMarkupId(true);
        return form;
    }

    private Panel createFacetsPanel(final String id) {

        final FacetsPanel panel = new FacetsPanel(id, facetNamesModel, fieldsModel, facetValuesFiltersModel, getModel(), facetSelectionTypeModeModel) {

            @Override
            protected void selectionChanged(Optional<AjaxRequestTarget> target) {
                updateSelection(target);
            }

        };
        panel.setOutputMarkupId(true);
        return panel;
    }

    private void updateSelection(Optional<AjaxRequestTarget> target) {
        //detach facetFieldsModel when selection is changed
        fieldsModel.detach();

        // selection changed, update facets and search results
        target.ifPresent(t -> {
            t.add(navigation);
            t.add(searchForm);
            t.add(resultsHeader);
            t.add(searchResultsPanel);
            t.add(selections);

            //reapply js for nice tooltips
            t.appendJavaScript("applyFacetTooltips();");
            t.appendJavaScript(String.format("document.title='%s'", JavaScriptUtils.escapeQuotes(getTitleModel().getObject())));
        });
    }

    @Override
    public IModel<String> getTitleModel() {
        return new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                final QueryFacetsSelection selection = FacetedSearchPage.this.getModelObject();
                if (selection != null && (selection.getQuery() != null || !selection.getSelection().isEmpty())) {
                    return searchResultsTitleModel.getObject();
                } else {
                    return FacetedSearchPage.super.getTitleModel().getObject();
                }
            }
        };

    }

    @Override
    public IModel<String> getCanonicalUrlModel() {
        return new PermaLinkModel(getPageClass(), getModel());
    }

    @Override
    public PageParameters getHistoryApiPageParameters() {
        return paramsConverter.toParameters(getModelObject());
    }

    @Override
    public void detachModels() {
        super.detachModels();

        searchResultsTitleModel.detach();

        if (facetSelectionTypeModeModel != null) {
            facetSelectionTypeModeModel.detach();
        }
    }

    @Override
    protected void onAfterRender() {
        super.onAfterRender();

        // Once the page has been rendered once, the simple mode can be disabled so that reloads of
        // the *same instance* will be shown as the full versions. New instances of the page
        // that are constructed with the simple model enabled will still render in simple mode
        // (once).
        //
        // see https://github.com/clarin-eric/VLO/issues/95
        simpleModeModel.setObject(false);
    }

    public SearchResultsPanel getSearchResultsPanel() {
        return searchResultsPanel;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(JavaScriptResources.getBootstrapTour(), true));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(FacetedSearchPage.class, "vlo-tour.js"), true));
        response.render(JavaScriptHeaderItem.forScript("initTourSearchPage();", "initTourSearchPage"));
    }

    private static class DataCatalog extends JsonLdModel.JsonLdObject {

        private String url;

        public DataCatalog(String url) {
            super("https://schema.org", "DataCatalog");
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

    }

}
