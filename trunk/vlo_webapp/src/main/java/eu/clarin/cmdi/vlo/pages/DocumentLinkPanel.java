package eu.clarin.cmdi.vlo.pages;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WicketURLEncoder;

public class DocumentLinkPanel extends Panel {

    private static final long serialVersionUID = 1L;

    public DocumentLinkPanel(String id, IModel<SolrDocument> model, SearchPageQuery query) {
        super(id, model);
        SolrDocument doc = model.getObject();
        PageParameters pageParameters = query.getPageParameters();
        pageParameters.put(ShowResultPage.PARAM_DOC_ID, WicketURLEncoder.QUERY_INSTANCE.encode(doc.getFieldValue("id").toString()));
        BookmarkablePageLink<ShowResultPage> docLink = new BookmarkablePageLink<ShowResultPage>("docLink", ShowResultPage.class,
                pageParameters);
        add(docLink);
        docLink.add(new Label("docLabel", doc.getFirstValue("name").toString()));
    }

}
