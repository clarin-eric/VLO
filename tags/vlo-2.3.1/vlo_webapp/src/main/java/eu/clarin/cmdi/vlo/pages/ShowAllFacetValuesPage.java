package eu.clarin.cmdi.vlo.pages;

import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;

public class ShowAllFacetValuesPage extends BasePage {

    public static final String SELECTED_FACET_PARAM = "selectedFacet";

    public ShowAllFacetValuesPage(PageParameters parameters) {
        super(parameters);
        final SearchPageQuery query = new SearchPageQuery(parameters);

        Map<String, String> filterQueries = query.getFilterQueryMap();

        RepeatingView filteredFacets = new RepeatingView("filteredFacets");
        if (filterQueries != null && !filterQueries.isEmpty()) {
            WebMarkupContainer wmc = new WebMarkupContainer(filteredFacets.newChildId());
            wmc.add(new Label("filteredFacet", "Selected categories:"));
            filteredFacets.add(wmc);
            for (String fq : filterQueries.keySet()) {
                wmc = new WebMarkupContainer(filteredFacets.newChildId());
                wmc.add(new Label("filteredFacet", fq + " = " + filterQueries.get(fq)));
                filteredFacets.add(wmc);
            }
        }
        add(filteredFacets);
        String selectedFacet = parameters.getString(SELECTED_FACET_PARAM);
        add(new Label("category", selectedFacet));
        SolrFacetFieldDataProvider data = new SolrFacetFieldDataProvider(selectedFacet, query);
        add(new AlphabeticalPanel("alphaPanel", data, query));
    }
}
