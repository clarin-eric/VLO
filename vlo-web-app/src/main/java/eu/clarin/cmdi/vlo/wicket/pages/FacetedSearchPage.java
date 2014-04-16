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
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.panel.Panel;
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
    @SpringBean(name="queryParametersConverter")
    private PageParametersConverter<QueryFacetsSelection> paramsConverter;

    private SearchResultsPanel searchResultsPanel;
    private Panel facetsPanel;
    private Panel collectionsPanel;
    private WebMarkupContainer navigation;

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
        navigation = new WebMarkupContainer("navigation");
        navigation.setOutputMarkupId(true);
        add(navigation);

        navigation.add(new BreadCrumbPanel("breadcrumbs", getModel()));
        navigation.add(new TopLinksPanel("permalink", getModel()) {

            @Override
            protected void onChange(AjaxRequestTarget target) {
                if (target != null) {
                    target.add(navigation);
                }
            }

        });

        add(createSearchForm("search"));

        collectionsPanel = createCollectionsPanel("collectionsFacet");
        add(collectionsPanel);

        facetsPanel = createFacetsPanel("facets");
        add(facetsPanel);

        Panel optionsPanel = createOptionsPanel("options");
        add(optionsPanel);

        searchResultsPanel = new SearchResultsPanel("searchResults", getModel());
        add(searchResultsPanel);
    }

    public Panel createOptionsPanel(String id) {
        final Panel optionsPanel = new AdvancedSearchOptionsPanel(id, getModel()) {

            @Override
            protected void selectionChanged(AjaxRequestTarget target) {
                updateSelection(target);
            }
        };
        return optionsPanel;
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

    private Panel createCollectionsPanel(final String id) {
        final FacetPanel panel = new SingleFacetPanel(id, getModel(), vloConfig.getCollectionFacet(), facetFieldsService) {

            @Override
            protected void selectionChanged(AjaxRequestTarget target) {
                updateSelection(target);
            }

        };
        panel.setOutputMarkupId(true);
        return panel;
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
        }
    }
}
