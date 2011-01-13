package eu.clarin.cmdi.vlo.pages;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.basic.SmartLinkMultiLineLabel;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.WicketURLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.Configuration;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.Resources;
import eu.clarin.cmdi.vlo.StringUtils;
import eu.clarin.cmdi.vlo.dao.DaoLocator;

public class ShowResultPage extends BasePage {

    private final static Logger LOG = LoggerFactory.getLogger(ShowResultPage.class);

    public static final String PARAM_DOC_ID = "docId";

    public ShowResultPage(final PageParameters parameters) {
        super(parameters);
        String docId = getPageParameters().getString(PARAM_DOC_ID, null);
        SearchPageQuery query = new SearchPageQuery(parameters);
        BookmarkablePageLink backLink = new BookmarkablePageLink("backLink", FacetedSearchPage.class, query.getPageParameters());
        add(backLink);
        String href = getHref(docId);
        if (href != null) {
            add(new ExternalLink("openBrowserLink", href, new ResourceModel(Resources.OPEN_IN_ORIGINAL_CONTEXT).getObject()));
        } else {
            add(new Label("openBrowserLink", new ResourceModel(Resources.ORIGINAL_CONTEXT_NOT_AVAILABLE).getObject()));
        }
        addPrevNextLabels(docId, query);
        SolrDocument solrDocument = DaoLocator.getSearchResultsDao().getSolrDocument(docId);
        addAttributesTable(solrDocument);
        addResourceLinks(solrDocument);
    }

    private String getHref(String linkToOriginalContext) {
        String result = linkToOriginalContext;
        if (linkToOriginalContext != null) {
            if (linkToOriginalContext.startsWith(FacetConstants.TEST_HANDLE_PREFIX)) {
                linkToOriginalContext = linkToOriginalContext.replace(FacetConstants.TEST_HANDLE_PREFIX, FacetConstants.HANDLE_PREFIX);
            }
            if (linkToOriginalContext.startsWith(FacetConstants.HANDLE_PREFIX)) {
                result = Configuration.getInstance().getIMDIBrowserUrl(linkToOriginalContext);
            } else {
                try {
                    new URL(linkToOriginalContext);
                } catch (MalformedURLException e) {
                    LOG.debug("Link to original context is incorrect:", e);
                    result = null;
                }
            }
        }
        return result;
    }

    private void addAttributesTable(final SolrDocument solrDocument) {
        DocumentAttributesDataProvider attributeProvider = new DocumentAttributesDataProvider(solrDocument);
        DataTable table = new DataTable("attributesTable", createAttributesColumns(), attributeProvider, 250);
        table.setTableBodyCss("attributesTbody");
        table.addTopToolbar(new HeadersToolbar(table, null));
        add(table);
    }

    @SuppressWarnings("serial")
    private IColumn[] createAttributesColumns() {
        IColumn[] columns = new IColumn[2];

        columns[0] = new PropertyColumn(new ResourceModel(Resources.FIELD), "field") {

            @Override
            public String getCssClass() {
                return "attribute";
            }
        };
        columns[1] = new AbstractColumn<DocumentAttribute>(new ResourceModel(Resources.VALUE)) {

            @Override
            public void populateItem(Item<ICellPopulator<DocumentAttribute>> cellItem, String componentId,
                    IModel<DocumentAttribute> rowModel) {
                DocumentAttribute attribute = rowModel.getObject();
                cellItem.add(new SmartLinkMultiLineLabel(componentId, attribute.getValue()) {

                    @Override
                    protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
                        CharSequence body = StringUtils.toMultiLineHtml(getDefaultModelObjectAsString());
                        replaceComponentTagBody(markupStream, openTag, getSmartLink(body));
                    }
                });
            }
            
            @Override
            public String getCssClass() {
                return "attributeValue";
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
            add(new WebMarkupContainer("prev"));
        }
        if (index < (docIdList.size() - 1) && index >= 0) {
            String prevDocId = docIdList.get(index + 1).getFieldValue(FacetConstants.FIELD_ID).toString();
            BookmarkablePageLink<ShowResultPage> next = createBookMarkableLink("next", query, prevDocId);
            add(next);
        } else {
            add(new WebMarkupContainer("next"));
        }
    }

    private void addResourceLinks(SolrDocument solrDocument) {
        RepeatingView repeatingView = new RepeatingView("resourceList");
        add(repeatingView);
        if (solrDocument.containsKey(FacetConstants.FIELD_RESOURCE)) {
            Collection<Object> resources = solrDocument.getFieldValues(FacetConstants.FIELD_RESOURCE);
            for (Object resource : resources) {
                String[] split = resource.toString().split(Pattern.quote(FacetConstants.FIELD_RESOURCE_SPLIT_CHAR), 2);
                String mimeType = split[0];
                String resourceLink = split[1];
                repeatingView.add(new ResourceLinkPanel(repeatingView.newChildId(), mimeType, resourceLink));
            }
        } else {
            repeatingView.add(new Label(repeatingView.newChildId(), new ResourceModel(Resources.NO_RESOURCE_FOUND)));
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
