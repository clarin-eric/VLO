package eu.clarin.cmdi.vlo.pages;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.Resources;
import eu.clarin.cmdi.vlo.VloWebApplication.ThemedSession;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.dao.AutoCompleteDao;
import eu.clarin.cmdi.vlo.importer.FacetConceptMapping.FacetConcept;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import fiftyfive.wicket.basic.TruncatedLabel;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.RequestUtils;

public class FacetedSearchPage extends BasePage {
    private static final long serialVersionUID = 1L;

    private final SearchPageQuery query;
    private final static AutoCompleteDao autoCompleteDao = new AutoCompleteDao();
    private final static String facetConceptsFile = VloConfig.getFacetConceptsFile();
    private final static Map<String, FacetConcept> facetNameMap = VLOMarshaller.getFacetConceptMapping(facetConceptsFile).getFacetConceptMap();
    
    /**
     * @param parameters Page parameters
     * @throws SolrServerException
     */
    public FacetedSearchPage(final PageParameters parameters) {
        super(parameters);
        query = new SearchPageQuery(parameters);
        addSearchBox();
        addFacetColumns();
        addSearchResults();
        addSearchServiceForm();
    }

    @SuppressWarnings("serial")
    private class SearchBoxForm extends Form<SearchPageQuery> {
        private final AutoCompleteTextField<String> searchBox;
        
        /*
         * Add multiline list of selected facet values
         */
        private void addFacetOverview() {

            // get a map of the facets currently selected
            Map<String, String> selectedFacets;
            selectedFacets = query.getFilterQueryMap();

            // create an interator for walking over the facets
            Iterator<Map.Entry<String, String>> entries = 
                    selectedFacets.entrySet().iterator();

            /*
             * Wicket label to be used to show the list of facets that have been 
             * selected.
             */
            MultiLineLabel facetOverview;

            // walk over the facets
            if (!entries.hasNext()) {
                // not a single facet has been selected
                facetOverview = new MultiLineLabel("facetOverview", 
                        "No facets values selected");
            } else {
                // at least one facet has been selected

                String string = "Selected facet values:  ";

                // start building the the multiline label here
                String[] facetFields;
                int i = 0, lineLength = 0,
                        maxLineLength = VloConfig.getFacetOverviewLength();
                Boolean hasPrevious = false;

                /* 
                 * Get the facet fields. We need them in order to display
                 * the values in the overview in the same order as their
                 * respective facets are listed in the boxes. Store multiple
                 * lines in one string.
                 */
                facetFields = VloConfig.getFacetFields();

                while (i < facetFields.length) {

                    // check if facet field is in selected facets map
                    if (selectedFacets.containsKey(facetFields[i])) {
                        String value = selectedFacets.get(facetFields[i]);
                        lineLength = lineLength + value.length();

                        if (hasPrevious) {
                            string = string.concat(", ");
                        }

                        if (lineLength > maxLineLength) {
                            string = string.concat("\n");
                            lineLength = 0;
                            hasPrevious = false;
                        }

                        string = string.concat(value);
                        hasPrevious = true;
                    }
                    i++;
                }

                // create a new wicket multi line label
                facetOverview = new MultiLineLabel("facetOverview", string);
            }

            // finally, add the label to the form
            this.add(facetOverview);
        }

        public SearchBoxForm(String id, SearchPageQuery query) {
            super(id, new CompoundPropertyModel<SearchPageQuery>(query));
            add(new ExternalLink("vloHomeLink", VloConfig.getHomeUrl()));

            searchBox = new AutoCompleteTextField<String>("searchQuery") {
                @Override
                protected Iterator<String> getChoices(String input) {
                    return autoCompleteDao.getChoices(input).iterator();
                }
            };
            
            add(searchBox);
            Button submit = new Button("searchSubmit");
            add(submit);
            
            // add link to help menu page 
            String helpUrl = VloConfig.getHelpUrl();            
            ExternalLink helpLink = new ExternalLink("helpLink", helpUrl, "help");
            add(helpLink);
            
            String thisURL = RequestUtils.toAbsolutePath(RequestCycle.get().urlFor(ShowResultPage.class, query.getPageParameters()).toString());
            try {
            	thisURL = URLEncoder.encode(thisURL,"UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
            
            String feedbackFormUrl = VloConfig.getFeedbackFromUrl() + thisURL;
            ExternalLink feedbackLink = new ExternalLink("feedbackLink", feedbackFormUrl, "found an error?");
            add(feedbackLink);
            
            addFacetOverview();
        }

        @Override
        protected void onSubmit() {
            SearchPageQuery query = getModelObject();
            PageParameters pageParameters = query.getPageParameters();

            // pageParameters = webApp.reflectPersistentParameters(pageParameters);
            pageParameters = ((ThemedSession)getSession()).reflectPersistentParameters(pageParameters);
            
            setResponsePage(FacetedSearchPage.class, pageParameters);
        }
    }

    private void addSearchBox() {
        add(new SearchBoxForm("searchForm", query));
    }
    
    @SuppressWarnings("serial")
    private void addFacetColumns() {
        GridView<FacetField> facetColumns = new GridView<FacetField>("facetColumns", new SolrFacetDataProvider(query.getSolrQuery()
                .getCopy())) {
            @Override
            protected void populateItem(Item<FacetField> item) {
            	String facetName = ((FacetField)item.getDefaultModelObject()).getName();
            	String descriptionTooltip = "";
            	if(facetNameMap.containsKey(facetName))
            		descriptionTooltip = facetNameMap.get(facetName).getDescription();
            	item.add(new FacetBoxPanel("facetBox", item.getModel(), descriptionTooltip).create(query));
            }

            @Override
            protected void populateEmptyItem(Item<FacetField> item) {
                item.add(new Label("facetBox", ""));
            }
        };
        facetColumns.setColumns(2);
        add(facetColumns);
    }

    @SuppressWarnings("serial")
    private void addSearchResults() {
        List<IColumn<SolrDocument>> columns = new ArrayList<IColumn<SolrDocument>>();
        columns.add(new AbstractColumn<SolrDocument>(new ResourceModel(Resources.NAME)) {
            
            @Override
            public void populateItem(Item<ICellPopulator<SolrDocument>> cellItem, String componentId, IModel<SolrDocument> rowModel) {
                cellItem.add(new DocumentLinkPanel(componentId, rowModel, query));
            }
        });
        columns.add(new AbstractColumn<SolrDocument>(new ResourceModel(Resources.DESCRIPTION)) {

            @Override
            public void populateItem(Item<ICellPopulator<SolrDocument>> cellItem, String componentId, IModel<SolrDocument> rowModel) {
        	String descriptionText = (String) rowModel.getObject().getFirstValue(FacetConstants.FIELD_DESCRIPTION);
                cellItem.add(new TruncatedLabel(componentId, 100, descriptionText));
                
            }
        });
        AjaxFallbackDefaultDataTable<SolrDocument> searchResultList = new AjaxFallbackDefaultDataTable<SolrDocument>("searchResults", columns,
                new SolrDocumentDataProvider(query.getSolrQuery().getCopy()), 30);

        add(searchResultList);
    }
    
    /**
     * Add contentSearch form (FCS)
     *
     * @param solrDocument
     */
    private void addSearchServiceForm() {
        
        BookmarkablePageLink link;
        link = new BookmarkablePageLink ("link", FacetedSearchPage.class);
        link.add (new Label ("naar deze pagina"));
        
        // get values for cql endpoint substitution
        String cqlEndpointFilter = VloConfig.getCqlEndpointFilter();
        String cqlEndpointAlternative = VloConfig.getCqlEndpointAlternative();

        final WebMarkupContainer contentSearchContainer = new WebMarkupContainer("contentSearch");
        add(contentSearchContainer);

        // get all documents with an entry for the FCS
        SearchServiceDataProvider dataProvider = new SearchServiceDataProvider(query.getSolrQuery());
        if (dataProvider.size() > 0 && dataProvider.size() <= 200) {	// at least one and not more than x records with FCS endpoint in result set?
            try {
                // building map (CQL endpoint -> List of resource IDs)
                HashMap<String, List<String>> aggregationContextMap = new HashMap<String, List<String>>();

                int offset = 0;
                int fetchSize = 1000;
                int totalResults = dataProvider.size();
                while (offset < totalResults) {
                    Iterator<SolrDocument> iter = dataProvider.iterator(offset, fetchSize);
                    while (iter.hasNext()) {
                        SolrDocument document = iter.next();
                        String id = document.getFirstValue(FacetConstants.FIELD_ID).toString();
                        String fcsEndpoint = document.getFirstValue(FacetConstants.FIELD_SEARCH_SERVICE).toString();
                        if (aggregationContextMap.containsKey(fcsEndpoint)) {
                            aggregationContextMap.get(fcsEndpoint).add(id);
                        } else {
                            List<String> idArray = new ArrayList<String>();
                            idArray.add(id);
                            
                            // substitute endpoint
                            if (cqlEndpointFilter.length() == 0){
                                // no substitution
                            } else {
                                // check for the need to substitute
                            }
                                
                            if (cqlEndpointFilter.equals(cqlEndpointFilter)){
                                // no substitution, take the value from the record
                                aggregationContextMap.put(fcsEndpoint, idArray);
                            } else {
                                // substitution, take the alternative url
                                aggregationContextMap.put(cqlEndpointAlternative, idArray);
                            }
                        }
                    }

                    offset += fetchSize;
                }

                // add HTML form to container
                String labelString;
                if (totalResults == 1) {
                    labelString = "Plain text search via Federated Content Search (supported by one resource in this result set)";
                } else {
                    labelString = "Plain text search via Federated Content Search (supported by " + totalResults
                            + " resources in this result set)";
                }
                Label contentSearchLabel = new Label("contentSearchForm", HtmlFormCreator.getContentSearchForm(
                        aggregationContextMap, labelString));
                contentSearchLabel.setEscapeModelStrings(false);
                contentSearchContainer.add(contentSearchLabel);
                // contentSearchContainer.add(link);
            } catch (UnsupportedEncodingException uee) {
                contentSearchContainer.setVisible(false);
            }
        } else {
            contentSearchContainer.setVisible(false);
        }
    }
}
