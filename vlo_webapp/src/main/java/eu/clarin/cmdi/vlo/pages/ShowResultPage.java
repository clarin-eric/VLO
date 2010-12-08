package eu.clarin.cmdi.vlo.pages;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WicketURLEncoder;

import eu.clarin.cmdi.vlo.Configuration;
import eu.clarin.cmdi.vlo.StringUtils;
import eu.clarin.cmdi.vlo.dao.DaoLocator;
import eu.clarin.cmdi.vlo.dao.FacetConstants;

public class ShowResultPage extends BasePage {

    public static final String PARAM_DOC_ID = "docId";

    public ShowResultPage(final PageParameters parameters) {
        super(parameters);
        String docId = getPageParameters().getString(PARAM_DOC_ID, null);
        SearchPageQuery query = new SearchPageQuery(parameters);
        BookmarkablePageLink backLink = new BookmarkablePageLink("backLink", FacetedSearchPage.class, query.getPageParameters());
        add(backLink);
        String handle = docId.substring("test-".length());
        add(new ExternalLink("openBrowserLink", Configuration.getInstance().getIMDIBrowserUrl(handle)));
        addPrevNextLabels(docId, query);
        addAttributesTable(docId, query);
    }

    private void addAttributesTable(final String docId, SearchPageQuery query) {
        DocumentAttributesDataProvider attributeProvider = new DocumentAttributesDataProvider(docId);
        DataTable table = new DataTable("attributesTable", createAttributesColumns(), attributeProvider, 250);
        table.setTableBodyCss("attributesTbody");
        table.addTopToolbar(new HeadersToolbar(table, null));
        add(table);
    }

    @SuppressWarnings("serial")
    private IColumn[] createAttributesColumns() {
        IColumn[] columns = new IColumn[2];

        columns[0] = new PropertyColumn(new Model<String>("Field"), "field") {

            @Override
            public String getCssClass() {
                return "attribute";
            }
        };
        columns[1] = new AbstractColumn<DocumentAttribute>(new Model<String>("Value")) {

            @Override
            public void populateItem(Item<ICellPopulator<DocumentAttribute>> cellItem, String componentId,
                    IModel<DocumentAttribute> rowModel) {
                DocumentAttribute attribute = rowModel.getObject();
                cellItem.add(new MultiLineLabel(componentId, attribute.getValue()) {

                    @Override
                    protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
                        CharSequence body = StringUtils.toMultiLineHtml(getDefaultModelObjectAsString());
                        replaceComponentTagBody(markupStream, openTag, body);
                    }
                });
            }
        };
        return columns;
    }

    private void addPrevNextLabels(String docId, SearchPageQuery query) {
        int index = -1;
        SolrDocumentList docIdList = DaoLocator.getSearchResultsDao().getDocIdList(query.getSolrQuery().getCopy());
        for (int i = 0; i < docIdList.size(); i++) {
            SolrDocument doc = docIdList.get(i);
            if (doc.getFieldValue(FacetConstants.FIELD_ID).equals(docId)) {
                index = i;
                break;
            }
        }
        if (index > 0) {
            String prevDocId = docIdList.get(index - 1).getFieldValue(FacetConstants.FIELD_ID).toString();
            BookmarkablePageLink<ShowResultPage> prev = createBookMarkableLink("prev", query, prevDocId);
            add(prev);
        } else {
            add(new Label("prev", "prev"));
        }
        if (index < (docIdList.size() - 1) && index >= 0) {
            String prevDocId = docIdList.get(index + 1).getFieldValue(FacetConstants.FIELD_ID).toString();
            BookmarkablePageLink<ShowResultPage> next = createBookMarkableLink("next", query, prevDocId);
            add(next);
        } else {
            add(new Label("next", "next"));
        }
    }

    public static BookmarkablePageLink<ShowResultPage> createBookMarkableLink(String linkId, SearchPageQuery query, String docId) {
        PageParameters pageParameters = query.getPageParameters();
        pageParameters.put(ShowResultPage.PARAM_DOC_ID, WicketURLEncoder.QUERY_INSTANCE.encode(docId));
        BookmarkablePageLink<ShowResultPage> docLink = new BookmarkablePageLink<ShowResultPage>(linkId, ShowResultPage.class,
                pageParameters);
        return docLink;
    }
}
