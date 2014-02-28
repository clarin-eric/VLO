package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.wicket.components.FacetsPanel;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.FacetFieldsService;
import eu.clarin.cmdi.vlo.wicket.components.FacetPanel;
import eu.clarin.cmdi.vlo.wicket.components.SearchForm;
import eu.clarin.cmdi.vlo.wicket.components.SearchResultsPanel;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldsModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetSelectionModel;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * The main search page showing a search form, facets, and search results
 *
 * @author twagoo
 */
public class FacetedSearchPage extends WebPage {

    private static final long serialVersionUID = 1L;

    @SpringBean
    private FacetFieldsService facetFieldsService;
    @SpringBean
    private VloConfig vloConfig;

    private final Panel searchResultsPanel;
    private final Panel facetsPanel;
    private final Panel collectionsPanel;

    public FacetedSearchPage(final PageParameters parameters) {
        super(parameters);

        final QueryFacetsSelection selection = paramsToQueryFacetSelection(parameters);
        final Model<QueryFacetsSelection> queryModel = new Model<QueryFacetsSelection>(selection);

        final SearchForm searchForm = new SearchForm("search", queryModel);
        add(searchForm);

        collectionsPanel = createCollectionsPanel("collectionsFacet", queryModel);
        add(collectionsPanel);

        facetsPanel = createFacetsPanel("facets", queryModel);
        add(facetsPanel);

        searchResultsPanel = new SearchResultsPanel("searchResults", queryModel);
        add(searchResultsPanel);
    }

    private Panel createCollectionsPanel(final String id, final IModel<QueryFacetsSelection> queryModel) {
        final FacetFieldModel collectionFacetFieldModel = new FacetFieldModel(facetFieldsService, vloConfig.getCollectionFacet(), queryModel);
        final FacetSelectionModel collectionSelectionModel = new FacetSelectionModel(collectionFacetFieldModel, queryModel);
        final FacetPanel panel = new FacetPanel(id, collectionSelectionModel, new Model<ExpansionState>(ExpansionState.COLLAPSED)) {

            @Override
            protected void selectionChanged(AjaxRequestTarget target) {
                updateSelection(target);
            }
        };
        panel.setOutputMarkupId(true);
        return panel;
    }

    private Panel createFacetsPanel(final String id, final IModel<QueryFacetsSelection> queryModel) {
        final IModel<List<FacetField>> facetFieldsModel = new FacetFieldsModel(facetFieldsService, vloConfig.getFacetFields(), queryModel);
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
        target.add(searchResultsPanel);
        target.add(facetsPanel);
        target.add(collectionsPanel);
    }

    private static QueryFacetsSelection paramsToQueryFacetSelection(final PageParameters parameters) {
        final String query = parameters.get("q").toOptionalString();

        //TODO: Map parameters to facet selection
        final Map<String, Collection<String>> selection = null;

        return new QueryFacetsSelection(query, selection);
    }
}
