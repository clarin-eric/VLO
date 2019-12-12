package eu.clarin.cmdi.vlo.exposure.frontend;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;

import eu.clarin.cmdi.vlo.exposure.frontend.panels.Filter;
import eu.clarin.cmdi.vlo.exposure.frontend.panels.FilterForm;
import eu.clarin.cmdi.vlo.exposure.frontend.panels.FilterFormRecord;
import eu.clarin.cmdi.vlo.exposure.frontend.service.FrontEndDataProvider;
import eu.clarin.cmdi.vlo.exposure.frontend.service.RecordStatistics;
import eu.clarin.cmdi.vlo.exposure.postgresql.QueryParameters;
import eu.clarin.cmdi.vlo.exposure.postgresql.impl.SearchResultHandlerImpl;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.clarin.cmdi.vlo.exposure.models.PageView;
import eu.clarin.cmdi.vlo.exposure.postgresql.impl.PageViewsHandlerImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;

public class Record extends WebPage {
    private final Logger logger = LoggerFactory.getLogger(Record.class);
    private RecordStatistics record;
    private QueryParameters queryParameters;
    private String FILTER_FORM_WICKET_ID = "FILTER_FORM";

    public Record(PageParameters parameters) {
        super(parameters);
        String recordId = "";
        try {
            getParameters(parameters);
            record = WicketApplication.get().getFrontendDataProvider().getRecordStatistics(queryParameters);
            addFilterForm();
            loadRecordStats();
            loadKeyWords();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            // redirect to main page
            getRequestCycle().setResponsePage(HomePage.class);
        }
    }

    private void addFilterForm(){
        add(new FilterFormRecord(FILTER_FORM_WICKET_ID, new IModel<Filter>() {
            @Override
            public Filter getObject() {
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                Date sDate = new Date(queryParameters.getStartDate().getTime());
                Date eDate = new Date(queryParameters.getEndDate().getTime());
                return new Filter(formatter.format(sDate), formatter.format(eDate), queryParameters.getRecordIdWC());
            }
        }));
    }

    private void getParameters(PageParameters parameters){
        String recordId = parameters.get("id").toString();
        Object sDate = parameters.get("startDate");
        Object eDate = parameters.get("endDate");

        long DAY_IN_MS = 1000 * 60 * 60 * 24;

        Timestamp endDate = new Timestamp(System.currentTimeMillis());
        Timestamp startDate = new Timestamp(System.currentTimeMillis()- (14 * DAY_IN_MS));

        if(eDate != null){
            try{
                Date date = new SimpleDateFormat("MM/dd/yyyy").parse(eDate.toString());
                endDate = new Timestamp(date.getTime() + 1000 * 59 * 60 * 24 );
            }catch (Exception e){
                endDate = new Timestamp(System.currentTimeMillis());
            }
        }
        if(sDate != null){
            try {
                Date date = new SimpleDateFormat("MM/dd/yyyy").parse(sDate.toString());
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
        queryParameters = new QueryParameters(startDate, endDate, recordId);
    }

    private void loadRecordStats() {
        try {
            add(new Label("recordId", record.getRecordId()));
            add(new Label("views", record.getViews()));
            add(new Label("lastVisit", record.getLastVisit()));
            add(new Label("queries", record.getNumOfQueries()));
            add(new Label("position", record.getAveragePosition()));
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    private void loadKeyWords() {
        HashMap<String, Integer> keyWords = record.getKeyWords();
        try {
            add(new ListView<String>("keywords",new ArrayList<String>(keyWords.keySet())){
                @Override
                protected void populateItem(ListItem<String> item) {
                    String day = item.getModelObject();
                    Integer freq = keyWords.get(day);
                    if(day == null)
                        day = "Home page";
                    item.add(new Label("keyword",day));
                    item.add(new Label("freq", freq));
                }
            });
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        FrontEndDataProvider frontEndDataProvider = WicketApplication.get().getFrontendDataProvider();
        frontEndDataProvider.setQueryParameters(queryParameters);
        String chartData = frontEndDataProvider.getPageViewsChartData();

        response.render(JavaScriptContentHeaderItem.forScript("chartData = " + chartData,"chart-data"));
    }
}
