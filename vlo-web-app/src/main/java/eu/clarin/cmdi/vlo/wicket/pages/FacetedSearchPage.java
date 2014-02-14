package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.wicket.components.FacetsPanel;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import java.util.Collection;
import java.util.Collections;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;

public class FacetedSearchPage extends WebPage {

    private static final long serialVersionUID = 1L;

    public FacetedSearchPage(final PageParameters parameters) {
        super(parameters);

        final QueryFacetsSelection selection = new QueryFacetsSelection(
                Collections.<String, Collection<String>>singletonMap("language", Collections.singleton("Dutch")));

        add(new FacetsPanel("facets", new Model<QueryFacetsSelection>(selection)));
    }
}
