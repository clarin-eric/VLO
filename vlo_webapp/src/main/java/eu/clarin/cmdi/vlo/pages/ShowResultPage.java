package eu.clarin.cmdi.vlo.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.vlo.Configuration;
import eu.clarin.cmdi.vlo.StringUtils;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;

public class ShowResultPage extends BasePage {

    public static final String PARAM_DOC_ID = "docId";
    SearchPageQuery query;
    DataTable table;
    DocumentAttributesDataProvider attributeProvider;
    AjaxFallbackDefaultDataTable t;

    public ShowResultPage(final PageParameters parameters) {
        super(parameters);
        String docId = getPageParameters().getString(PARAM_DOC_ID, null);
        query = new SearchPageQuery(parameters);
        BookmarkablePageLink backLink = new BookmarkablePageLink("backLink", FacetedSearchPage.class, query.getPageParameters());
        add(backLink);
        String handle = docId.substring("test-".length());
        add(new ExternalLink("openBrowserLink", Configuration.getInstance().getIMDIBrowserUrl(handle)));
        addSearchResults(docId);
        addAttributesTable(docId, query);
    }

    private void addAttributesTable(final String docId, SearchPageQuery query) {
        attributeProvider = new DocumentAttributesDataProvider(docId);
        t = new AjaxFallbackDefaultDataTable("attributesTable", createAttributesColumns(), attributeProvider, 25);
        t.setTableBodyCss("attributesTbody");
        add(t);
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

    @SuppressWarnings("serial")
    private void addSearchResults(String docId) {
        List<IColumn<SolrDocument>> columns = new ArrayList<IColumn<SolrDocument>>();
        columns.add(new AbstractColumn<SolrDocument>(new Model<String>("Results")) {

            @Override
            public void populateItem(Item<ICellPopulator<SolrDocument>> cellItem, String componentId, IModel<SolrDocument> rowModel) {

                cellItem.add(new DocumentLinkPanel(componentId, rowModel, query));
            }
        });
        AjaxFallbackDefaultDataTable t = new AjaxFallbackDefaultDataTable("test", columns, new SolrDocumentDataProvider(query.getSolrQuery().getCopy()), 1);
        add(t);
        add(new PreviousNextPagingNavigator("nav", t, this, query));
    }

    public void setCurrentPage(int pagenumber) {
        SolrDocumentDataProvider dataProvider = new SolrDocumentDataProvider(query.getSolrQuery().getCopy());
        Iterator it = dataProvider.iterator(pagenumber, 1);
        if (it.hasNext()) {
            SolrDocument doc = (SolrDocument) it.next();
            attributeProvider = new DocumentAttributesDataProvider(doc.getFieldValue("id").toString());
            this.remove("attributesTable");
            table = new DataTable("attributesTable", createAttributesColumns(), attributeProvider, 25);
            table.setTableBodyCss("attributesTbody");
            table.addTopToolbar(new HeadersToolbar(table, null));
            add(table);
        }
    }
}
