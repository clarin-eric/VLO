package eu.clarin.cmdi.vlo.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class FacetHeaderPanel extends Panel {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("serial")
    public FacetHeaderPanel(String id, IModel<FacetModel> model, final SearchPageQuery query) {
        super(id, model);
        add(new Link<FacetModel>("allLink", model) {

            @Override
            public void onClick() {
                query.removeFilterQuery(getModelObject().getFacetField());
                PageParameters pageParameters = new PageParameters();
                pageParameters.put(FacetedSearchPage.PARAM_QUERY, query.getSolrQuery().toString());
                setResponsePage(FacetedSearchPage.class, pageParameters);
            }

        });
        add(new Label("headerLabelSelect", model.getObject().getSelectedValue()));
    }

}
