package eu.clarin.cmdi.vlo.wicket.pages;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.JavaScriptResources;
import eu.clarin.cmdi.vlo.PiwikEventConstants;
import eu.clarin.cmdi.vlo.VloWebSession;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.PiwikConfig;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.wicket.historyapi.HistoryApiAware;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.wicket.AjaxPiwikTrackingBehavior;
import eu.clarin.cmdi.vlo.wicket.model.BooleanOptionsModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldsModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetNamesModel;
import eu.clarin.cmdi.vlo.wicket.model.PermaLinkModel;
import eu.clarin.cmdi.vlo.wicket.panels.BreadCrumbPanel;
import eu.clarin.cmdi.vlo.wicket.panels.TopLinksPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.AvailabilityFacetPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.AdvancedSearchOptionsPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.FacetsPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.SearchFormPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.SearchResultsHeaderPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.SearchResultsPanel;
import eu.clarin.cmdi.vlo.wicket.provider.SolrDocumentProvider;
import java.util.Map;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.AbstractPageableView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

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
    private VloConfig vloConfig;
    @SpringBean
    private PiwikConfig piwikConfig;
    @SpringBean(name = "queryParametersConverter")
    private PageParametersConverter<QueryFacetsSelection> paramsConverter;
    @SpringBean
    private FieldNameService fieldNameService;

    private IDataProvider<SolrDocument> documentsProvider;

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
    }

    private void createModels() {
        final List<String> facetFields = vloConfig.getFacetFieldNames();
        final List<String> allFields = ImmutableList.copyOf(Iterables.concat(facetFields, ImmutableList.of(fieldNameService.getFieldName(ADDITIONAL_FACETS))));
        facetNamesModel = new FacetNamesModel(facetFields);
        fieldsModel = new FacetFieldsModel(facetFieldsService, allFields, getModel(), -1);

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
        documentsProvider = new SolrDocumentProvider(getModel(), fieldNameService);

        searchContainer = new WebMarkupContainer("searchContainer");
        searchContainer.add(new AttributeModifier("class", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return simpleModeModel.getObject() ? "simple" : "";
            }

        }));
        searchContainer.setOutputMarkupId(true);
        add(searchContainer);

        final IDataProvider<SolrDocument> solrDocumentProvider = new SolrDocumentProvider(getModel(), fieldNameService);

        navigation = createNavigation("navigation");
        searchContainer.add(navigation);

        searchForm = createSearchForm("search");
        searchContainer.add(searchForm);

        //selections panel (facets and options)
        selections = new WebMarkupContainer("selections") {

            @Override
            protected void onConfigure() {
                final Map<String, FacetSelection> facetSelection = FacetedSearchPage.this.getModel().getObject().getSelection();
                setVisible(solrDocumentProvider.size() > 0
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
        searchContainer.add(new IndicatingAjaxFallbackLink("toggleSelections") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                selectionsExpandedModel.setObject(!selectionsExpandedModel.getObject());
                if (target != null) {
                    target.add(selections);
                    target.add(this);
                }
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
        searchResultsPanel = new SearchResultsPanel("searchResults", getModel(), solrDocumentProvider) {

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(solrDocumentProvider.size() > 0);
            }

            @Override
            protected void onAjaxSearchPagination(AjaxRequestTarget target) {
                super.onAjaxSearchPagination(target);
                if (target != null) {
                    //updating record offset in search result header
                    target.add(resultsHeader);
                }
            }

        };
        searchContainer.add(searchResultsPanel.setOutputMarkupPlaceholderTag(true));

        final AbstractPageableView<SolrDocument> resultsView = searchResultsPanel.getResultsView();

        resultsHeader = createResultsHeader("searchresultsheader", getModel(), resultsView, solrDocumentProvider);
        searchContainer.add(resultsHeader.setOutputMarkupId(true));
    }

    private SearchResultsHeaderPanel createResultsHeader(String id, IModel<QueryFacetsSelection> model, AbstractPageableView<SolrDocument> resultsView, IDataProvider<SolrDocument> solrDocumentProvider) {
        return new SearchResultsHeaderPanel(id, model, resultsView, solrDocumentProvider) {
            @Override
            protected void onChange(AjaxRequestTarget target) {
                updateSelection(target);
            }

            @Override
            protected void onSelectionChanged(QueryFacetsSelection selection, AjaxRequestTarget target) {
                setModelObject(selection);
                updateSelection(target);
            }

        };
    }

    private WebMarkupContainer createNavigation(String id) {
        final WebMarkupContainer container = new WebMarkupContainer(id);
        container.setOutputMarkupId(true);
        container.add(new BreadCrumbPanel("breadcrumbs", getModel()));
        container.add(new TopLinksPanel("permalink", new PermaLinkModel(getPageClass(), getModel()), getTitleModel()) {

            @Override
            protected void onChange(AjaxRequestTarget target) {
                if (target != null) {
                    target.add(container);
                }
            }

        });
        return container;
    }

    private Panel createOptionsPanel(String id) {
        final Panel panel = new AdvancedSearchOptionsPanel(id, getModel(), facetSelectionTypeModeModel, documentsProvider) {

            @Override
            protected void selectionChanged(AjaxRequestTarget target) {
                updateSelection(target);
            }
        };
        panel.setOutputMarkupId(true);
        return panel;
    }

    private Panel createAvailabilityPanel(String id) {
        final Panel availabilityPanel = new AvailabilityFacetPanel(id, getModel(), fieldsModel) {

            @Override
            protected void selectionChanged(AjaxRequestTarget target) {
                updateSelection(target);
            }
        };
        availabilityPanel.setOutputMarkupId(true);
        return availabilityPanel;
    }

    private Panel createSearchForm(String id) {
        final SearchFormPanel form = new SearchFormPanel(id, getModel()) {

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                // reset expansion state of search results
                searchResultsPanel.resetExpansion();

                //transition from simple
                simpleModeModel.setObject(false);
                if (target != null) {
                    target.prependJavaScript("cb|transitionFromSimple(cb);");
                    target.add(searchContainer); //update everything within container
                }

                updateSelection(target);
            }

        };
        form.setOutputMarkupId(true);
        return form;
    }

    private Panel createFacetsPanel(final String id) {

        final FacetsPanel panel = new FacetsPanel(id, facetNamesModel, fieldsModel, getModel(), facetSelectionTypeModeModel) {

            @Override
            protected void selectionChanged(AjaxRequestTarget target) {
                updateSelection(target);
            }

        };
        panel.setOutputMarkupId(true);
        return panel;
    }

    private void updateSelection(AjaxRequestTarget target) {
        //detach facetFieldsModel when selection is changed
        fieldsModel.detach();

        // selection changed, update facets and search results
        if (target != null) { // null if JavaScript disabled
            target.add(navigation);
            target.add(searchForm);
            target.add(resultsHeader);
            target.add(searchResultsPanel);
            target.add(selections);

            //reapply js for nice tooltips
            target.appendJavaScript("applyFacetTooltips();");
        }
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
        response.render(JavaScriptHeaderItem.forReference(JavaScriptResources.getBootstrapTour()));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(FacetedSearchPage.class, "vlo-tour.js")));
        response.render(JavaScriptHeaderItem.forScript("initTourSearchPage();", "initTourSearchPage"));
    }

}
