package eu.clarin.cmdi.vlo.pages;

import java.util.Iterator;
import java.util.Map;

import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;

public class ShowAllFacetValuesPage extends BasePage {

    public static final String SELECTED_FACET_PARAM = "selectedFacet";

    public ShowAllFacetValuesPage(PageParameters parameters) {
        super(parameters);
        final SearchPageQuery query = new SearchPageQuery(parameters);

        Map<String, String> filterQueries = query.getFilterQueryMap();
        RepeatingView filteredFacets = new RepeatingView("filteredFacets");
        if (filterQueries != null) {
            WebMarkupContainer wmc = new WebMarkupContainer(filteredFacets.newChildId());
            wmc.add(new Label("filteredFacet", "Selected categories:"));
            filteredFacets.add(wmc);
            for (String fq : filterQueries.keySet()) {
                wmc = new WebMarkupContainer(filteredFacets.newChildId());
                wmc.add(new Label("filteredFacet", fq + " = " + filterQueries.get(fq)));
                filteredFacets.add(wmc);
            }
        }
        add(filteredFacets); //TODO PD make panel and clean up can be used in all the pages probably.

        String selectedFacet = parameters.getString(SELECTED_FACET_PARAM);
        add(new Label("category", selectedFacet));

        SolrFacetFieldDataProvider data = new SolrFacetFieldDataProvider(selectedFacet, query);
        int size = data.size();
        int half = size / 2;
        RepeatingView left = new RepeatingView("leftColumn");
        Iterator<? extends Count> leftIter = data.iterator(0, size - half);
        while (leftIter.hasNext()) {
            Count count = leftIter.next();
            left.add(new FacetLinkPanel(left.newChildId(), new Model<Count>(count), query));
        }
        add(left);
        RepeatingView right = new RepeatingView("rightColumn");
        Iterator<? extends Count> rightIter = data.iterator(size - half, half);
        while (rightIter.hasNext()) {
            Count count = rightIter.next();
            right.add(new FacetLinkPanel(right.newChildId(), new Model<Count>(count), query));
        }
        add(right);
    }

}
