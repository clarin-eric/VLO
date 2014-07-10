package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.wicket.panels.SingleFacetPanel;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.wicket.panels.search.FacetsPanel;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.wicket.panels.search.FacetPanel;
import eu.clarin.cmdi.vlo.wicket.components.SearchForm;
import eu.clarin.cmdi.vlo.wicket.panels.search.SearchResultsPanel;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldsModel;
import eu.clarin.cmdi.vlo.wicket.panels.BreadCrumbPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.FacetValuesPanel;
import eu.clarin.cmdi.vlo.wicket.panels.TopLinksPanel;
import eu.clarin.cmdi.vlo.wicket.panels.search.AdvancedSearchOptionsPanel;
import eu.clarin.cmdi.vlo.wicket.provider.SolrDocumentProvider;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * The main search page showing a search form, facets, and search results
 *
 * @author twagoo
 */
public class FacetedSearchPage extends VloBasePage<QueryFacetsSelection> {

    private static final long serialVersionUID = 1L;

    @SpringBean
    private FacetFieldsService facetFieldsService;
    @SpringBean
    private VloConfig vloConfig;
    @SpringBean(name = "queryParametersConverter")
    private PageParametersConverter<QueryFacetsSelection> paramsConverter;

    private IDataProvider<SolrDocument> documentsProvider;
    private SearchResultsPanel searchResultsPanel;
    private Component facetsPanel;
    private Component collectionsPanel;
    private Component navigation;
    private Component optionsPanel;

    public FacetedSearchPage(IModel<QueryFacetsSelection> queryModel) {
        super(queryModel);
        addComponents();
    }

    public FacetedSearchPage(PageParameters parameters) {
        super(parameters);

        final QueryFacetsSelection selection = paramsConverter.fromParameters(parameters);
        final IModel<QueryFacetsSelection> queryModel = new Model<QueryFacetsSelection>(selection);
        setModel(queryModel);
        addComponents();
    }

    private void addComponents() {
        documentsProvider = new SolrDocumentProvider(getModel());

        navigation = createNavigation("navigation");
        add(navigation);

        add(createSearchForm("search"));

        collectionsPanel = createCollectionsPanel("collections");
        add(collectionsPanel);

        facetsPanel = createFacetsPanel("facets");
        add(facetsPanel);

        optionsPanel = createOptionsPanel("options");
        add(optionsPanel);

        searchResultsPanel = new SearchResultsPanel("searchResults", getModel(), documentsProvider);
        add(searchResultsPanel);
    }

    private WebMarkupContainer createNavigation(String id) {
        final WebMarkupContainer container = new WebMarkupContainer(id);
        container.setOutputMarkupId(true);
        container.add(new BreadCrumbPanel("breadcrumbs", getModel()) {

            @Override
            protected void onSelectionChanged(QueryFacetsSelection selection, AjaxRequestTarget target) {
                setModelObject(selection);
                updateSelection(target);
            }

        });
        container.add(new TopLinksPanel("permalink", getModel()) {

            @Override
            protected void onChange(AjaxRequestTarget target) {
                if (target != null) {
                    target.add(container);
                }
            }

        });
        return container;
    }

    private Component createOptionsPanel(String id) {
        final Panel panel = new AdvancedSearchOptionsPanel(id, getModel(), documentsProvider) {

            @Override
            protected void selectionChanged(AjaxRequestTarget target) {
                updateSelection(target);
            }
        };
        return panel.setOutputMarkupId(true);
    }

    private SearchForm createSearchForm(String id) {
        final SearchForm searchForm = new SearchForm(id, getModel()) {

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                // reset expansion state of search results
                searchResultsPanel.resetExpansion();
                updateSelection(target);
            }

        };
        return searchForm;
    }

    private Component createCollectionsPanel(final String id) {
        // collection facet is optional...
        final WebMarkupContainer enclosure = new WebMarkupContainer(id);
        enclosure.setOutputMarkupId(true);
        if (vloConfig.getCollectionFacet() != null) {
            final FacetPanel panel = new SingleFacetPanel("collectionsFacet", getModel(), vloConfig.getCollectionFacet(), facetFieldsService, 3) {

                @Override
                protected void selectionChanged(AjaxRequestTarget target) {
                    updateSelection(target);
                }

            };
            enclosure.add(panel);
        } else {
            // no collection facet, do not add the panel
            final WebMarkupContainer placeholder = new WebMarkupContainer("collectionsFacet");
            placeholder.setVisible(false);
            enclosure.add(placeholder);
        }
        return enclosure;
    }

    private Panel createFacetsPanel(final String id) {
        final IModel<QueryFacetsSelection> queryModel = getModel();
        final IModel<List<FacetField>> facetFieldsModel = new FacetFieldsModel(facetFieldsService, vloConfig.getFacetFields(), queryModel, FacetValuesPanel.MAX_NUMBER_OF_FACETS_TO_SHOW);
        final FacetsPanel panel = new FacetsPanel(id, facetFieldsModel, queryModel) {

            @Override
            protected void selectionChanged(AjaxRequestTarget target) {
                updateSelection(target);
            }
        };
        panel.setOutputMarkupId(true);
        return panel;
    }

    private void updateSelection(AjaxRequestTarget target) {
        // selection changed, update facets and search results
        if (target != null) { // null if JavaScript disabled
            target.add(navigation);
            target.add(searchResultsPanel);
            target.add(facetsPanel);
            target.add(collectionsPanel);
            target.add(optionsPanel);
        }
    }
}
