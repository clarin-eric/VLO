package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.wicket.components.FacetsPanel;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.components.SearchForm;
import eu.clarin.cmdi.vlo.wicket.components.SearchResultsPanel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;

/**
 * The main search page showing a search form, facets, and search results
 *
 * @author twagoo
 */
public class FacetedSearchPage extends WebPage {

    private static final long serialVersionUID = 1L;

    public FacetedSearchPage(final PageParameters parameters) {
        super(parameters);

        final QueryFacetsSelection selection = new QueryFacetsSelection(
                new HashMap<String, Collection<String>>() {
                    {
//                        put("language", Collections.singleton("Dutch"));
                        put("continent", Collections.<String>emptyList());
                    }
                });
        final Model<QueryFacetsSelection> queryModel = new Model<QueryFacetsSelection>(selection);

        add(new FacetsPanel("facets", queryModel));
        
        add(new SearchForm("search", queryModel));
        
        add(new SearchResultsPanel("searchResults", queryModel));
    }
}
