package eu.clarin.cmdi.vlo.pages;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
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
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.protocol.http.WicketURLDecoder;
import org.apache.wicket.protocol.http.WicketURLEncoder;
import org.apache.wicket.resource.ContextRelativeResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.Resources;
import eu.clarin.cmdi.vlo.StringUtils;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.dao.DaoLocator;

public class ShowResultPage extends BasePage {

    private final static Logger LOG = LoggerFactory.getLogger(ShowResultPage.class);
    public static final String PARAM_DOC_ID = "docId";
    public static final String feedbackfromURL = "http://www.clarin.eu/node/3502?url=";
    
    private final static ImageResource FEEDBACK_IMAGE = new ImageResource(new ContextRelativeResource("Images/feedback.png"), "Report an Error");
    private final URL xslFile = getClass().getResource("/cmdi2xhtml.xsl");

    @SuppressWarnings("serial")
    public ShowResultPage(final PageParameters parameters) {
        super(parameters);
        final String docId = WicketURLDecoder.QUERY_INSTANCE.decode(getPageParameters().getString(PARAM_DOC_ID, null));
        SolrDocument solrDocument = DaoLocator.getSearchResultsDao().getSolrDocument(docId);
        if (solrDocument != null) {
            final SearchPageQuery query = new SearchPageQuery(parameters);
            BookmarkablePageLink<String> backLink = new BookmarkablePageLink<String>("backLink", FacetedSearchPage.class, query.getPageParameters());
            add(backLink);
            String href = getHref(docId);
            if (href != null) {
                add(new ExternalLink("openBrowserLink", href, new ResourceModel(Resources.OPEN_IN_ORIGINAL_CONTEXT).getObject()));
            } else {
                add(new Label("openBrowserLink", new ResourceModel(Resources.ORIGINAL_CONTEXT_NOT_AVAILABLE).getObject()));
            }
            addAttributesTable(solrDocument);
            addResourceLinks(solrDocument);
            addSearchServiceForm(solrDocument);
            addCompleteCmdiView(solrDocument);
            
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
        } else {
            setResponsePage(new ResultNotFoundPage(parameters));
        }

        // add feedback link
        addFeedbackLink(parameters);
    }

    private String getHref(String linkToOriginalContext) {
        String result = linkToOriginalContext;
        if (linkToOriginalContext != null) {
            if (linkToOriginalContext.startsWith(FacetConstants.TEST_HANDLE_MPI_PREFIX)) {
                linkToOriginalContext = linkToOriginalContext.replace(FacetConstants.TEST_HANDLE_MPI_PREFIX, FacetConstants.HANDLE_MPI_PREFIX);
            }
            if (linkToOriginalContext.startsWith(FacetConstants.HANDLE_MPI_PREFIX)) {
                result = VloConfig.getIMDIBrowserUrl(linkToOriginalContext);
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
		solrDocument.remove(FacetConstants.FIELD_LANGUAGE);	// ignore language entry, because of FIELD_LANGUAGE_LINK
        DocumentAttributesDataProvider attributeProvider = new DocumentAttributesDataProvider(solrDocument);
        @SuppressWarnings("unchecked")
		DataTable table = new DataTable("attributesTable", createAttributesColumns(), attributeProvider, 250);
        table.setTableBodyCss("attributesTbody");
        table.addTopToolbar(new HeadersToolbar(table, null));
        add(table);
    }

    @SuppressWarnings({ "serial" })
    private IColumn[] createAttributesColumns() {
        IColumn[] columns = new IColumn[2];

        columns[0] = new PropertyColumn<Object>(new ResourceModel(Resources.FIELD), "field") {

            @Override
            public String getCssClass() {
                return "attribute";
            }
        };

        columns[1] = new AbstractColumn<DocumentAttribute>(new ResourceModel(Resources.VALUE)) {
            @Override
            public void populateItem(Item<ICellPopulator<DocumentAttribute>> cellItem, String componentId, IModel<DocumentAttribute> rowModel) {
                DocumentAttribute attribute = rowModel.getObject();

                if (attribute.getField().equals(FacetConstants.FIELD_LANGUAGE)) {
                    cellItem.add(new SmartLinkMultiLineLabel(componentId, attribute.getValue()) {

                        @Override
                        protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
                            CharSequence body = StringUtils.toMultiLineHtml(getDefaultModelObjectAsString());
                            replaceComponentTagBody(markupStream, openTag, getSmartLink(body));
                        }
                    });
                } else if(attribute.getField().equals(FacetConstants.FIELD_LANGUAGES)) {
                    cellItem.add(new SmartLinkMultiLineLabel(componentId, attribute.getValue()) {

                        @Override
                        protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
                        	setEscapeModelStrings(false);
                            CharSequence body = getDefaultModelObjectAsString();
                            replaceComponentTagBody(markupStream, openTag, body);
                        }
                    });
                } else {
                    cellItem.add(new SmartLinkMultiLineLabel(componentId, attribute.getValue()) {

                        @Override
                        protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
                            CharSequence body = StringUtils.toMultiLineHtml(getDefaultModelObjectAsString());
                            replaceComponentTagBody(markupStream, openTag, getSmartLink(body));
                        }
                    });
                }
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
    
    private void addFeedbackLink(final PageParameters parameters) {
        String thisURL = RequestUtils.toAbsolutePath(RequestCycle.get().urlFor(ShowResultPage.class, parameters).toString());
        try {
            thisURL = URLEncoder.encode(thisURL,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.toString());
        }
    	
        Image resourceImg = new Image("feedbackImage", FEEDBACK_IMAGE.getResource());
        String title = "Report an error";
        resourceImg.add(new SimpleAttributeModifier("title", title));
        resourceImg.add(new SimpleAttributeModifier("alt", title));
        String href = getHref(feedbackfromURL+thisURL);
        String name = feedbackfromURL+thisURL;
        ExternalLink link = new ExternalLink("feedbackLink", href);
        link.add(resourceImg);
        add(new Label("feedbackLabel", "Found an error?"));
        add(link);
    }

    public static BookmarkablePageLink<ShowResultPage> createBookMarkableLink(String linkId, SearchPageQuery query, String docId) {
        PageParameters pageParameters = query.getPageParameters();
        pageParameters.put(ShowResultPage.PARAM_DOC_ID, WicketURLEncoder.QUERY_INSTANCE.encode(docId));
        BookmarkablePageLink<ShowResultPage> docLink = new BookmarkablePageLink<ShowResultPage>(linkId, ShowResultPage.class,
                pageParameters);
        return docLink;
    }
    
	/**
	 * Add contentSearch form (FCS)
	 * @param solrDocument
	 */
	private void addSearchServiceForm(final SolrDocument solrDocument) {
		final WebMarkupContainer contentSearchContainer = new WebMarkupContainer("contentSearch");
		add(contentSearchContainer);
		
		if (solrDocument.containsKey(FacetConstants.FIELD_SEARCH_SERVICE)) {
			try {
				// building map (CQL endpoint -> List with resource ID)
				HashMap<String, List<String>> aggregrationContextMap = new HashMap<String, List<String>>();
				List<String> idList = new ArrayList<String>();
				idList.add(solrDocument.getFirstValue(FacetConstants.FIELD_ID).toString());
				aggregrationContextMap.put(solrDocument.getFirstValue(FacetConstants.FIELD_SEARCH_SERVICE).toString(), idList);
				Label contentSearchLabel = new Label("contentSearchForm", HtmlFormCreator.getContentSearchForm(aggregrationContextMap));
				contentSearchLabel.setEscapeModelStrings(false);				
				contentSearchContainer.add(contentSearchLabel);
			} catch (UnsupportedEncodingException uee) {
				contentSearchContainer.setVisible(false);
			}
		} else {
			contentSearchContainer.setVisible(false);
		}
	}
	
	/**
	 * Add complete CMDI view
	 * @param solrDocument
	 */
	private void addCompleteCmdiView(final SolrDocument solrDocument) {
		StringWriter strWriter = new StringWriter();

        final Processor proc = new Processor(false);
        final XsltCompiler comp = proc.newXsltCompiler();

        try {
                final XsltExecutable exp = comp.compile(new StreamSource(xslFile.getFile()));
                final XdmNode source = proc.newDocumentBuilder().build(
                                new StreamSource(new InputStreamReader(new URL(solrDocument.getFirstValue(FacetConstants.FIELD_COMPLETE_METADATA).toString()).openStream())));
                final Serializer out = new Serializer();
                out.setOutputProperty(Serializer.Property.METHOD, "html");
                out.setOutputProperty(Serializer.Property.INDENT, "yes");
                out.setOutputWriter(strWriter);
                final XsltTransformer trans = exp.load();

                trans.setInitialContextNode(source);
                trans.setDestination(out);
                trans.transform();
        } catch (Exception e) {
                LOG.error("Couldn't create CMDI metadata: "+e.getMessage());
                strWriter = new StringWriter().append("<b>Could not load complete CMDI metadata</b>");
        }		
		
        Label completeCmdiLabel = new Label("completeCmdi", strWriter.toString());
		completeCmdiLabel.setEscapeModelStrings(false);
		add(completeCmdiLabel);
		
		// remove complete CMDI view on page load
		add(new AbstractBehavior() {
			private static final long serialVersionUID = 1865219352602175954L;

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.renderOnLoadJavascript("toogleDiv('completeCmdi', 'toogleLink')");
			}
		});
	}
}
