package eu.clarin.cmdi.vlo.pages;

import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class FacetLinkPanel extends Panel {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("serial")
    public FacetLinkPanel(String id, IModel<Count> model, final SearchPageQuery query) {
        super(id, model);
        Link<Count> facetLink = new Link<Count>("facetLink", model) {

            @Override
            public void onClick() {
                Count count = getModelObject();
                query.setFilterQuery(count);
                PageParameters pageParameters = new PageParameters();
                pageParameters.put(FacetedSearchPage.PARAM_QUERY, query.getSolrQuery().toString());
                setResponsePage(FacetedSearchPage.class, pageParameters);
            }

        };
        facetLink.add(new Label("facetLinkLabel", model.getObject().getName()));
        add(facetLink);
        add(new Label("facetCount", "" + model.getObject().getCount()));
    }

}
