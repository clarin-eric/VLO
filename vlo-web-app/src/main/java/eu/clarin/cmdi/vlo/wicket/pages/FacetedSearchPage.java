package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.wicket.components.FacetsPanel;
import eu.clarin.cmdi.vlo.pojo.Facet;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import java.util.Arrays;
import java.util.Collections;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;

public class FacetedSearchPage extends WebPage {

    private static final long serialVersionUID = 1L;

    public FacetedSearchPage(final PageParameters parameters) {
        super(parameters);

        final FacetSelection languageSelection = new FacetSelection(new Facet("language"), Collections.singleton("Dutch"));

        final QueryFacetsSelection selection = new QueryFacetsSelection(Arrays.asList(languageSelection));

        add(new FacetsPanel("facets", new Model<QueryFacetsSelection>(selection)));
    }
}
