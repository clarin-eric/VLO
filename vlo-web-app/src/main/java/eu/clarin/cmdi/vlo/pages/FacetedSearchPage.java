package eu.clarin.cmdi.vlo.pages;

import eu.clarin.cmdi.vlo.components.FacetsPanel;
import java.util.Arrays;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.util.ListModel;

public class FacetedSearchPage extends WebPage {

    private static final long serialVersionUID = 1L;

    public FacetedSearchPage(final PageParameters parameters) {
        super(parameters);
        
        add(new FacetsPanel("facets", new ListModel<FacetField>(Arrays.asList(
                new FacetField("Language"),
                new FacetField("Resource type")))));
    }
}
