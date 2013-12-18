package eu.clarin.cmdi.vlo.pages;

import java.util.Map;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.RepeatingView;

public class ShowAllFacetValuesPage extends BasePage {

	public static final String SELECTED_FACET_PARAM = "selectedFacet";
	public static final String FACET_MIN_OCCURS = "facetMinOccurs";
	public static final Integer FACET_MIN_OCCURS_VALUE = 2;	// for parameter FACET_MIN_OCCURS to show only values that occur with this minimal frequency

	public ShowAllFacetValuesPage(PageParameters parameters) {
		super(parameters);
		final SearchPageQuery query = new SearchPageQuery(parameters);

		// show facets that were already chosen
		addFilteredFacets(query);
		
		// filter for minimal frequency of values
		addOccurrencesFilter(parameters);
		
		String selectedFacet = (parameters.get(SELECTED_FACET_PARAM)).toString();
                
                Integer facetMinOccurs = (parameters.get(FACET_MIN_OCCURS)).toInt(1); // take care of 1 as default value
                
		add(new Label("category", selectedFacet));
		SolrFacetFieldDataProvider data = new SolrFacetFieldDataProvider(selectedFacet, query);
		add(new AlphabeticalPanel("alphaPanel", data, query, facetMinOccurs));
	}
	
	/**
	 * Adds a link to allow filtering for minimal occurrences of a value or to show all values again
	 * @param parameters
	 */
	private void addOccurrencesFilter(PageParameters parameters) {
		PageParameters newParameters = (PageParameters) parameters.clone();
		newParameters.remove(FACET_MIN_OCCURS);
		if(!parameters.containsKey(FACET_MIN_OCCURS) || parameters.getAsInteger(FACET_MIN_OCCURS) == 1) {
			newParameters.add(FACET_MIN_OCCURS, FACET_MIN_OCCURS_VALUE.toString());
			add(new BookmarkablePageLink<Void>("filter", ShowAllFacetValuesPage.class, newParameters).add(new Label("filterLabel", "Show only values that occur at least "+FACET_MIN_OCCURS_VALUE+" times")));
		} else {
			add(new BookmarkablePageLink<Void>("filter", ShowAllFacetValuesPage.class, newParameters).add(new Label("filterLabel", "Show all values")));
		}
	}
	
	/**
	 * Add view that shows already selected facets
	 * @param query
	 */
	private void addFilteredFacets(SearchPageQuery query) {
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
	}
}
