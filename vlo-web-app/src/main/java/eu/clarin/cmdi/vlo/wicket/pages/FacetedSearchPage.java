package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.config.VloConfig;
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
import org.apache.wicket.behavior.AttributeAppender;
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

    public FacetedSearchPage(final PageParameters parameters) {
        super(parameters);

        final QueryFacetsSelection selection = paramsToQueryFacetSelection(parameters);
        final Model<QueryFacetsSelection> queryModel = new Model<QueryFacetsSelection>(selection);

        add(createCollectionsPanel("collectionsFacet", queryModel));
        add(createFacetsPanel("facets", queryModel));
        
        add(new SearchForm("search", queryModel));
        add(new SearchResultsPanel("searchResults", queryModel));
    }

    private Panel createCollectionsPanel(final String id, final Model<QueryFacetsSelection> queryModel) {
        final FacetFieldModel collectionFacetFieldModel = new FacetFieldModel(facetFieldsService, vloConfig.getCollectionFacet(), queryModel);
        final FacetSelectionModel collectionSelectionModel = new FacetSelectionModel(collectionFacetFieldModel, queryModel);
        final FacetPanel panel = new FacetPanel(id, collectionSelectionModel);
        return panel;
    }

    private Panel createFacetsPanel(final String id, final Model<QueryFacetsSelection> queryModel) {
        final IModel<List<FacetField>> facetFieldsModel = new FacetFieldsModel(facetFieldsService, vloConfig.getFacetFields(), queryModel);
        final FacetsPanel facetsPanel = new FacetsPanel(id, facetFieldsModel, queryModel);
        return facetsPanel;
    }

    private QueryFacetsSelection paramsToQueryFacetSelection(final PageParameters parameters) {
        final String query = parameters.get("q").toOptionalString();

        //TODO: Map parameters to facet selection
        final Map<String, Collection<String>> selection = null;

        return new QueryFacetsSelection(query, selection);
    }
}
