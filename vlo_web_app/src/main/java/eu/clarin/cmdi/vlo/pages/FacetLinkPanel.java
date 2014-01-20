package eu.clarin.cmdi.vlo.pages;

import eu.clarin.cmdi.vlo.VloSession;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class FacetLinkPanel extends BasePanel {

    private static final long serialVersionUID = 1L;

    public FacetLinkPanel(String id, IModel<Count> model, final SearchPageQuery query) {
        super(id, model);

        Count count = model.getObject();
        SearchPageQuery q = query.getShallowCopy();
        q.setFilterQuery(count);
        PageParameters param = q.getPageParameters();
        param.mergeWith(VloSession.get().getVloSessionPageParameters());
        
        Link<Count> facetLink = new BookmarkablePageLink("facetLink", FacetedSearchPage.class, param);
        facetLink.add(new Label("facetLinkLabel", model.getObject().getName()));
        add(facetLink);
        add(new Label("facetCount", "" + model.getObject().getCount()));
    }

}
