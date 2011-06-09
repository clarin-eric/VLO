package eu.clarin.cmdi.vlo.pages;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.extensions.markup.html.basic.SmartLinkMultiLineLabel;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.WicketURLDecoder;
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

    @SuppressWarnings("serial")
    public ShowResultPage(final PageParameters parameters) {
	super(parameters);
	final String docId = WicketURLDecoder.QUERY_INSTANCE.decode(getPageParameters().getString(PARAM_DOC_ID, null));
	SolrDocument solrDocument = DaoLocator.getSearchResultsDao().getSolrDocument(docId);
	if (solrDocument != null) {
	    final SearchPageQuery query = new SearchPageQuery(parameters);
	    BookmarkablePageLink backLink = new BookmarkablePageLink("backLink", FacetedSearchPage.class, query.getPageParameters());
	    add(backLink);
	    String href = getHref(docId);
	    if (href != null) {
		add(new ExternalLink("openBrowserLink", href, new ResourceModel(Resources.OPEN_IN_ORIGINAL_CONTEXT).getObject()));
	    } else {
		add(new Label("openBrowserLink", new ResourceModel(Resources.ORIGINAL_CONTEXT_NOT_AVAILABLE).getObject()));
	    }
	    add(new AjaxLazyLoadPanel("prevNextHeader") {

		@Override
		public Component getLazyLoadComponent(String markupId) {
		    return new PrevNextHeaderPanel(markupId, docId, query);
		}

		@Override
		public Component getLoadingComponent(String markupId) {
		    return new PrevNextHeaderPanel(markupId);
		}
	    });

	    addAttributesTable(solrDocument);
	    addResourceLinks(solrDocument);
	} else {
	    setResponsePage(new ResultNotFoundPage(parameters));
	}

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

        // We also add the metadata field.
        Label metadata = new Label("completeMD",attributeProvider.getMetadata().getValue());
        add(metadata);
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

    @SuppressWarnings("serial")
    private void addResourceLinks(SolrDocument solrDocument) {
	RepeatingView repeatingView = new RepeatingView("resourceList");
	add(repeatingView);
	if (solrDocument.containsKey(FacetConstants.FIELD_RESOURCE)) {
	    Collection<Object> resources = solrDocument.getFieldValues(FacetConstants.FIELD_RESOURCE);
	    for (Object resource : resources) {
		String[] split = resource.toString().split(Pattern.quote(FacetConstants.FIELD_RESOURCE_SPLIT_CHAR), 2);
		final String mimeType = split[0];
		final String resourceLink = split[1];
		repeatingView.add(new AjaxLazyLoadPanel(repeatingView.newChildId()) {
		    @Override
		    public Component getLazyLoadComponent(String markupId) {
			return new ResourceLinkPanel(markupId, mimeType, resourceLink);
		    }

		});
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
