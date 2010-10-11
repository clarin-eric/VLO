package eu.clarin.cmdi.vlo.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
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
        addAttributesTable(docId);
    }

    private void addAttributesTable(final String docId) {
        DataTable table = new DataTable("attributesTable", createAttributesColumns(), new DocumentAttributesDataProvider(docId), 25);
        table.setTableBodyCss("attributesTbody");
        table.addTopToolbar(new HeadersToolbar(table, null));
        add(table);
    }

    @SuppressWarnings("serial")
    private IColumn[] createAttributesColumns() {
        IColumn[] columns = new IColumn[2];

        columns[0] = new PropertyColumn(new Model<String>("Attribute"), "field") {
            @Override
            public String getCssClass() {
                return "attribute";
            }

        };
        columns[1] = new AbstractColumn<DocumentAttribute>(new Model<String>("")) {

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

}
