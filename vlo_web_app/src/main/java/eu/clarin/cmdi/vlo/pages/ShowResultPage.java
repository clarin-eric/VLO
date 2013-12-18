package eu.clarin.cmdi.vlo.pages;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.Resources;
import eu.clarin.cmdi.vlo.StringUtils;
import eu.clarin.cmdi.vlo.VloWebApplication;
import eu.clarin.cmdi.vlo.VloSession;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.dao.DaoLocator;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.behavior.AbstractBehavior;
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
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.protocol.http.WicketURLDecoder;
import org.apache.wicket.protocol.http.WicketURLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page showing VLO search results
 * 
 * @author keeloo, for the addLandingPage links method and annotations
 */
public class ShowResultPage extends BasePage {

    private final static Logger LOG = LoggerFactory.getLogger(ShowResultPage.class);
    public static final String PARAM_DOC_ID = "docId";
    public static final String feedbackfromURL = VloConfig.getFeedbackFromUrl();
    
    private final URL xslFile = getClass().getResource("/cmdi2xhtml.xsl");
    
    @SuppressWarnings("serial")
    public ShowResultPage(final PageParameters currentParam) {
        
        super(currentParam);
        final String docId = WicketURLDecoder.QUERY_INSTANCE.decode(getPageParameters().getString(PARAM_DOC_ID, null));
        SolrDocument solrDocument = DaoLocator.getSearchResultsDao().getSolrDocument(docId);
        if (solrDocument != null) {
            final SearchPageQuery query = new SearchPageQuery(currentParam);
            
            // now the persistent parameters are not in the query parameters
            PageParameters newParam = new PageParameters ();
            // add the new query parameters to this map
            newParam.putAll(query.getPageParameters());
            // add the persistent parameters to this map
            //newParam = webApp.reflectPersistentParameters(newParam);
            
            newParam = ((VloWebApplication.ThemedSession)getSession()).reflectPersistentParameters(newParam);
            
            BookmarkablePageLink<String> backLink = new BookmarkablePageLink<String>("backLink", FacetedSearchPage.class, newParam);
            add(backLink);
            String href = getHref(docId);
            if (href != null) {
                add(new ExternalLink("openBrowserLink", href, new ResourceModel(Resources.OPEN_IN_ORIGINAL_CONTEXT).getObject()));
            } else {
                add(new Label("openBrowserLink", new ResourceModel(Resources.ORIGINAL_CONTEXT_NOT_AVAILABLE).getObject()));
            }
            addAttributesTable(solrDocument);
            
            /* If there are any, add the link or links to landing pages 
             * contained in the solr document.
             */
            addLandingPageLinks(solrDocument);
            
            // also, if there are any, add the link or links to search pages 
            addSearchPageLinks(solrDocument);

            // add the rest of the resource links to the result page
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
            setResponsePage(new ResultNotFoundPage(currentParam));
        }

        // add the feedback link to the result page
        addFeedbackLink(currentParam);
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

    /*
     * Based on the solr document, create a table of meta data attribute and value pairs
     */
    private void addAttributesTable(final SolrDocument solrDocument) {
        // because of FIELD_LANGUAGE_LINK, remove the FIELD_LANDGUAGE facet
        solrDocument.remove(FacetConstants.FIELD_LANGUAGE);
        /* Use the data provider from the solrDocument object as a provider
         * for the table to be instantiated here.
         */
        DocumentAttributesDataProvider attributeProvider = new DocumentAttributesDataProvider(solrDocument);

        DataTable table;
        /* Create table: use the provider, and pass a method to create the 
         * columns.
         */
        table = new DataTable("attributesTable", createAttributesColumns(), attributeProvider, 250);
        // associate css with table
        table.setTableBodyCss("attributesTbody");
        table.addTopToolbar(new HeadersToolbar(table, null));  
        // add table to page
        add(table);
    }

    /**
     * Create the columns for the table. 
     * 
     * Create one column for the attributes and one column for their values.
     * 
     * @param
     */
    private IColumn[] createAttributesColumns() {
        IColumn[] columns = new IColumn[2];

        // create the column for the attribute names
        columns[0] = new PropertyColumn<Object>(new ResourceModel(Resources.FIELD), "field") {

            @Override
            public String getCssClass() {
                return "attribute";
            }
        };

        // create the column for the values of the attributes
        columns[1] = new AbstractColumn<DocumentAttribute>(new ResourceModel(Resources.VALUE)) {
            @Override
            public void populateItem(Item<ICellPopulator<DocumentAttribute>> cellItem,
                    String componentId, IModel<DocumentAttribute> rowModel) {

                /*
                 * While in the data for the table, the values (for the 
                 * description) attribute are structured. Creating a single 
                 * attribute, these values are collapsed. Refer to the 
                 * 
                 * DocumentAttribute 
                 * 
                 * class.
                 */
                DocumentAttribute attribute = rowModel.getObject();

                if (attribute.getField().equals(FacetConstants.FIELD_LANGUAGES)) {
                    cellItem.add(new SmartLinkMultiLineLabel(componentId, attribute.getValue()) {
                        @Override
                        protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
                            setEscapeModelStrings(false);
                            CharSequence body = getDefaultModelObjectAsString();
                            replaceComponentTagBody(markupStream, openTag, body);
                        }
                    });
                } else if (attribute.getField().equals(FacetConstants.FIELD_COMPLETE_METADATA)) {
                    cellItem.add(new SmartLinkMultiLineLabel(componentId, attribute.getValue()) {
                        @Override
                        protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
                            setEscapeModelStrings(false);
                            CharSequence body = getDefaultModelObjectAsString();
                            replaceComponentTagBody(markupStream, openTag, "<a href=\"" + body + "\">" + body + "</a>");
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
    
    /**
     * Add landing page links to the result page.
     *
     * @param solrDocument the document to get the links from
     */
    private void addLandingPageLinks(SolrDocument solrDocument) {
        
        Label oneLandingPageText;
        oneLandingPageText = new Label("oneLandingPage",
                new ResourceModel(Resources.LANDING_PAGE).getObject() + ":");
        this.add(oneLandingPageText);

        Label moreLandingPagesText;
        moreLandingPagesText = new Label("moreLandingPages",
                new ResourceModel(Resources.LANDING_PAGES).getObject() + ":");
        this.add(moreLandingPagesText);

        RepeatingView repeatingView = new RepeatingView("landingPageList");
        add(repeatingView);

        /*
         * Depending on the number of links to be shown, at most one of the 
         * labels in the accompanying HTML page that is subject to Wicket is 
         * made visible.
         */
        if (!solrDocument.containsKey(FacetConstants.FIELD_LANDINGPAGE)) {
            
            /* Since there are no links to be shown, make both labels defined in
             * the page invisible
             */
            oneLandingPageText.setVisible(false);
            moreLandingPagesText.setVisible(false);
        } else {
            //  make one of the two labels invisible

            Collection<Object> landingPages = 
                    solrDocument.getFieldValues(FacetConstants.FIELD_LANDINGPAGE);
            if (landingPages.size() > 1) {
                
                // the list will contain more than one landing page link
                oneLandingPageText.setVisible(false);
                moreLandingPagesText.setVisible(true);
            } else {
                // the list will contain exactly one landing page link.
                oneLandingPageText.setVisible(true);
                moreLandingPagesText.setVisible(false);
            }
            
            // generate the list of links
            for (Iterator<Object> it = landingPages.iterator(); it.hasNext();) {
                final Object landingPage;
                landingPage = it.next();

                // add a link to the list
                repeatingView.add(
                        new AjaxLazyLoadPanel(repeatingView.newChildId()) {
                            @Override
                            public Component getLazyLoadComponent(String markupId) {
                                String landingPageLink;
                                landingPageLink = landingPage.toString();

                                // create a panel for the link
                                return new LandingPageLinkPanel(markupId,
                                        landingPage.toString());
                            }
                        });
            }
        }
    }
    
    /**
     * Add search page links to the result page.
     *
     * @param solrDocument the document to get the links from
     */
    private void addSearchPageLinks(SolrDocument solrDocument) {
        
        Label oneSearchPageText;
        oneSearchPageText = new Label("oneSearchPage",
                new ResourceModel(Resources.SEARCH_PAGE).getObject() + ":");
        this.add(oneSearchPageText);

        Label moreSearchPagesText;
        moreSearchPagesText = new Label("moreSearchPages",
                new ResourceModel(Resources.SEARCH_PAGES).getObject() + ":");
        this.add(moreSearchPagesText);

        RepeatingView repeatingView = new RepeatingView("searchPageList");
        add(repeatingView);

        /*
         * Depending on the number of links to be shown, at most one of the 
         * labels in the accompanying HTML page that is subject to Wicket is 
         * made visible.
         */
        if (!solrDocument.containsKey(FacetConstants.FIELD_SEARCHPAGE)) {
            
            /* Since there are no links to be shown, make both labels defined in
             * the page invisible
             */
            oneSearchPageText.setVisible(false);
            moreSearchPagesText.setVisible(false);
        } else {
            //  make one of the two labels invisible

            Collection<Object> searchPages = 
                    solrDocument.getFieldValues(FacetConstants.FIELD_SEARCHPAGE);
            if (searchPages.size() > 1) {
                
                // the list will contain more than one landing page link
                oneSearchPageText.setVisible(false);
                moreSearchPagesText.setVisible(true);
            } else {
                // the list will contain exactly one landing page link.
                oneSearchPageText.setVisible(true);
                moreSearchPagesText.setVisible(false);
            }
            
            // generate the list of links
            for (Iterator<Object> it = searchPages.iterator(); it.hasNext();) {
                final Object searchPage;
                searchPage = it.next();

                // add a link to the list
                repeatingView.add(
                        new AjaxLazyLoadPanel(repeatingView.newChildId()) {
                            @Override
                            public Component getLazyLoadComponent(String markupId) {
                                String searchPageLink;
                                searchPageLink = searchPage.toString();

                                // create a panel for the link 
                                return new SearchPageLinkPanel(markupId,
                                        searchPage.toString());
                            }
                        });
            }
        }
    }

    /**
     * Add links to resources other than search or landing pages to the result
     * page.
     *
     * @param solrDocument the document to get the links from
     */
    private void addResourceLinks(SolrDocument solrDocument) {
        RepeatingView repeatingView = new RepeatingView("resourceList");
        add(repeatingView);
        if (solrDocument.containsKey(FacetConstants.FIELD_RESOURCE)) {     
            Collection<Object> resources = solrDocument.getFieldValues(FacetConstants.FIELD_RESOURCE);
            if (resources.size() > 1) {
                repeatingView.add(new Label(repeatingView.newChildId(), new ResourceModel(Resources.RESOURCE_PL)));
            } else {
                repeatingView.add(new Label(repeatingView.newChildId(), new ResourceModel(Resources.RESOURCE)));
            }
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
    	
        // Image resourceImg = new Image("feedbackImage", FEEDBACK_IMAGE.getResource());
        // String title = "Report an error";
        // resourceImg.add(new SimpleAttributeModifier("title", title));
        // resourceImg.add(new SimpleAttributeModifier("alt", title));
        String href = getHref(feedbackfromURL+thisURL);
        String name = feedbackfromURL+thisURL;
        ExternalLink link = new ExternalLink("feedbackLink", href, "found an error?");
        // link.add(resourceImg);
        // add(new Label("feedbackLabel", "Found an error?"));
        add(link);
    }

    public static BookmarkablePageLink<ShowResultPage> createBookMarkableLink(String linkId, SearchPageQuery query, String docId, VloSession session) {
        PageParameters pageParameters = query.getPageParameters();
        pageParameters.put(ShowResultPage.PARAM_DOC_ID, WicketURLEncoder.QUERY_INSTANCE.encode(docId));
        
        // webApp.reflectPersistentParameters(pageParameters);
        // instead of this: pass page parameters back to the session
        session.reflectPersistentParameters(pageParameters);

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
				Label contentSearchLabel = new Label("contentSearchForm", HtmlFormCreator.getContentSearchForm(aggregrationContextMap, "Plain text search via Federated Content Search"));
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
                out.setOutputProperty(Serializer.Property.ENCODING, "UTF-8");
                out.setOutputWriter(strWriter);
                final XsltTransformer trans = exp.load();

                trans.setInitialContextNode(source);
                trans.setDestination(out);
                trans.transform();
        } catch (Exception e) {
                LOG.error("Couldn't create CMDI metadata: " + e.getMessage());
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
