package eu.clarin.cmdi.vlo.pages;

import eu.clarin.cmdi.vlo.components.FacetsPanel;
import eu.clarin.cmdi.vlo.pojo.Facet;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import java.util.Arrays;
import java.util.Collections;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.util.ListModel;

public class FacetedSearchPage extends WebPage {

    private static final long serialVersionUID = 1L;

    public FacetedSearchPage(final PageParameters parameters) {
        super(parameters);

        add(new FacetsPanel("facets", new ListModel<FacetSelection>(Arrays.asList(
                new FacetSelection(new Facet("Language"), Collections.singleton("Dutch")),
                new FacetSelection(new Facet("Resource type"), Collections.singleton("Text"))
        ))));
    }
}
