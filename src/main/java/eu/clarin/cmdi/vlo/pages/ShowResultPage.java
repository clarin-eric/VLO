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
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.vlo.StringUtils;

public class ShowResultPage extends WebPage {

    public static final String PARAM_DOC_ID = "docId";

    public ShowResultPage(final PageParameters parameters) {
        super(parameters);
        String docId = getPageParameters().getString(PARAM_DOC_ID, null);
        PageParameters params = new PageParameters();
        params.put(FacetedSearchPage.PARAM_QUERY, parameters.get(FacetedSearchPage.PARAM_QUERY));
        BookmarkablePageLink docLink = new BookmarkablePageLink("backLink", FacetedSearchPage.class, params);
        add(docLink);
        //TODO PD configure browser url http://corpus1.mpi.nl/ds/imdi_browser or http://catalog.clarin.eu/ds/imdi_browser/????? 
        //TODO PD strip test from handle
        add(new ExternalLink("openBrowserLink", "http://corpus1.mpi.nl/ds/imdi_browser?openpath=" + docId));
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
