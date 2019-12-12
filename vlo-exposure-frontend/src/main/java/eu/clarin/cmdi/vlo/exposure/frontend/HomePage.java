package eu.clarin.cmdi.vlo.exposure.frontend;

import eu.clarin.cmdi.vlo.exposure.frontend.panels.*;
import eu.clarin.cmdi.vlo.exposure.frontend.service.FrontEndDataProvider;
import eu.clarin.cmdi.vlo.exposure.models.Record;
import eu.clarin.cmdi.vlo.exposure.postgresql.QueryParameters;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.markup.html.WebPage;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class HomePage extends WebPage {
    private static final long serialVersionUID = 1L;
    private QueryParameters queryParameters;
    private final String KEYWORDS_PANEL_WICKET_ID = "KEYWORDS_PANEL";
    private final String PAGE_VIEWS_PANEL_WICKET_ID = "PAGE_VIEWS_PANEL";
    private final String ALL_SEARCH_RESULTS_PANEL_WICKET_ID = "ALL_SEARCH_RESULTS_PANEL";
    private final String SEARCH_RESULTS_PANEL_WICKET_ID = "SEARCH_RESULTS_PANEL_PANEL";
    private final String FILTER_FORM_WICKET_ID = "FILTER_FORM";


    public HomePage(final PageParameters parameters) {
        super(parameters);
        getParameters(parameters);
        loadPanels();
    }

    private void loadPanels() {
        add(new FilterForm(FILTER_FORM_WICKET_ID, new IModel<Filter> () {
            @Override
            public Filter getObject() {
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                Date sDate = new Date(queryParameters.getStartDate().getTime());
                Date eDate = new Date(queryParameters.getEndDate().getTime());
                return new Filter(formatter.format(sDate), formatter.format(eDate), queryParameters.getRecordIdWC());
            }
        }));
        FrontEndDataProvider frontEndDataProvider =  WicketApplication.get().getFrontendDataProvider();
        frontEndDataProvider.setQueryParameters(queryParameters);
        createPageViewPanel(frontEndDataProvider); // Panel 1
        createKeywordsPanel(frontEndDataProvider); // Panel 3
        createSearchResultsPanel(frontEndDataProvider); // Panels 4 + 5
    }

    private void getParameters(PageParameters parameters){
        String sDate = parameters.get("startDate").toString();
        String eDate = parameters.get("endDate").toString();
        String recordIdWC = parameters.get("recordIdWC").toString();
        long DAY_IN_MS = 1000 * 60 * 60 * 24;

        Timestamp endDate = new Timestamp(System.currentTimeMillis());
        Timestamp startDate = new Timestamp(System.currentTimeMillis()- (14 * DAY_IN_MS));

        if(recordIdWC== null || recordIdWC.equals(""))
            recordIdWC = "%%";

        if(eDate != null){
            try{
                Date date = new SimpleDateFormat("MM/dd/yyyy").parse(eDate);
                endDate = new Timestamp(date.getTime() + 1000 * 59 * 60 * 24 );
            }catch (Exception e){
                endDate = new Timestamp(System.currentTimeMillis());
            }
        }
        if(sDate != null){
            try {
                Date date = new SimpleDateFormat("MM/dd/yyyy").parse(sDate);
                startDate = new Timestamp(date.getTime());
            }catch (Exception e){
                startDate = new Timestamp(System.currentTimeMillis()- (14 * DAY_IN_MS));
            }
        }

        if(startDate.getTime() > endDate.getTime()) { // exchange start and end
            Timestamp temp = startDate;
            startDate = endDate;
            endDate = temp;
        }
        queryParameters = new QueryParameters(startDate, endDate, recordIdWC);
    }

    // PANEL 1
    private void createPageViewPanel(FrontEndDataProvider frontEndDataProvider) {

        final List<Record> records = frontEndDataProvider.getPageViewsStatistics();
        add(new RecordsDataViewPanel(PAGE_VIEWS_PANEL_WICKET_ID,records, "Most Visited Records", new String[] {"Show","Record Id","Visits"}, queryParameters ));
    }


    // PANEL 3 Keywords
    private void createKeywordsPanel(FrontEndDataProvider frontEndDataProvider) {
        final HashMap<String,Integer> keyWords = frontEndDataProvider.getKeywordsStatistics();
        add(new KeywordsPanel(KEYWORDS_PANEL_WICKET_ID, keyWords));
    }

    // PANEL 4 + 5
    private void createSearchResultsPanel(FrontEndDataProvider frontEndDataProvider) {
        final List<Record> searchResults = frontEndDataProvider.getSearchResultsStatistics(false);
        final List<Record>  searchResultsAll = frontEndDataProvider.getSearchResultsStatistics(true);
        add(new RecordsDataViewPanel(SEARCH_RESULTS_PANEL_WICKET_ID,searchResults, "Appearance in Search Results including Frontpage Results", new String[] {"Show","Record Id","# Queries"}, queryParameters ));
        add(new RecordsDataViewPanel(ALL_SEARCH_RESULTS_PANEL_WICKET_ID,searchResultsAll, "Appearance in Search Results", new String[] {"Show","Record Id","# Queries"} , queryParameters));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        FrontEndDataProvider frontEndDataProvider = WicketApplication.get().getFrontendDataProvider();
        frontEndDataProvider.setQueryParameters(queryParameters);
        String chartData = frontEndDataProvider.getKeywordsStatisticsChartData();

        response.render(JavaScriptContentHeaderItem.forScript("chartData = " + chartData,"chart-data"));
    }

}
