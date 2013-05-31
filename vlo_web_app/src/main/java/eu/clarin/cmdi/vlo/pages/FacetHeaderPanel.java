package eu.clarin.cmdi.vlo.pages;

import eu.clarin.cmdi.vlo.VloWebApplication;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class FacetHeaderPanel extends Panel {

    private static final long serialVersionUID = 1L;
    
    // reference to the web application object
    static VloWebApplication webApp;
    
    /**
     * Make sure every web application object sends this message
     * 
     * @param vloWebApplication reference to the web application object
     */
    public static void setWebApp (VloWebApplication vloWebApplication){
        webApp = vloWebApplication;
    }

    public FacetHeaderPanel(String id, IModel<FacetModel> model, final SearchPageQuery query) {
        super(id, model);
        SearchPageQuery copy = query.getShallowCopy();
        copy.removeFilterQuery(model.getObject().getFacetField());
        PageParameters pageParameters = copy.getPageParameters();
        
        pageParameters = webApp.addSessionParameters(pageParameters);
        
        add(new BookmarkablePageLink("allLink", FacetedSearchPage.class, pageParameters));
        add(new Label("headerLabelSelect", model.getObject().getSelectedValue()));
    }

}
