package eu.clarin.cmdi.vlo.pages;

import eu.clarin.cmdi.vlo.VloPageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class FacetHeaderPanel extends BasePanel {

    private static final long serialVersionUID = 1L;

    public FacetHeaderPanel(String id, IModel<FacetModel> model, final SearchPageQuery query) {
        super(id, model);
        SearchPageQuery copy = query.getShallowCopy();
        copy.removeFilterQuery(model.getObject().getFacetField());
        PageParameters param = copy.getPageParameters();
 
        add(new BookmarkablePageLink("allLink", FacetedSearchPage.class, param));
        add(new Label("headerLabelSelect", model.getObject().getSelectedValue()));
    }

}
