package eu.clarin.cmdi.vlo.pages;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.VloWebApplication.ThemedSession;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class DocumentLinkPanel extends Panel {

    private static final long serialVersionUID = 1L;

    public DocumentLinkPanel(String id, IModel<SolrDocument> model, SearchPageQuery query) {
        super(id, model);
        SolrDocument doc = model.getObject();
        BookmarkablePageLink<ShowResultPage> docLink = ShowResultPage.createBookMarkableLink("docLink", query, doc.getFieldValue(
                FacetConstants.FIELD_ID).toString(), (ThemedSession)getSession());
        add(docLink);
        Object nameValue = doc.getFirstValue(FacetConstants.FIELD_NAME);
        String name = "<no name>";
        if (nameValue != null) {
            name = nameValue.toString();
        }
        docLink.add(new Label("docLabel", name));
    }

}
