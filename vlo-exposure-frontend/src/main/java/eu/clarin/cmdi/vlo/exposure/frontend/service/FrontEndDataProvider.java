package eu.clarin.cmdi.vlo.exposure.frontend.service;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.exposure.models.Record;
import eu.clarin.cmdi.vlo.exposure.postgresql.QueryParameters;
import eu.clarin.cmdi.vlo.exposure.postgresql.impl.PageViewsHandlerImpl;
import eu.clarin.cmdi.vlo.exposure.postgresql.impl.SearchQueryHandlerImpl;
import eu.clarin.cmdi.vlo.exposure.postgresql.impl.SearchResultHandlerImpl;

import java.util.*;

public class FrontEndDataProvider {
    PageViewsHandlerImpl pageViewHandler;
    SearchResultHandlerImpl searchResultHandler;
    SearchQueryHandlerImpl searchQueryHandler;
    private QueryParameters queryParameters;
    private VloConfig vloConfig;

    public FrontEndDataProvider(VloConfig vloConfig, QueryParameters queryParameters) {
        this(vloConfig);
        this.queryParameters = queryParameters;

    }

    public FrontEndDataProvider(VloConfig vloConfig) {
        this.vloConfig = vloConfig;
        this.pageViewHandler = new PageViewsHandlerImpl();
        this.searchResultHandler = new SearchResultHandlerImpl();
        this.searchQueryHandler= new SearchQueryHandlerImpl();
    }
    public RecordStatistics getRecordStatistics(QueryParameters qp){
        HashMap<String, Integer> keyWords = pageViewHandler.getKeyWordsByRecordId(vloConfig,qp);
        HashMap<String, String> stat = pageViewHandler.getStatByRecordId(vloConfig,qp);
        HashMap<String, Double> srStat = searchResultHandler.getSearchResultsStatPerRecordId(vloConfig,qp);
        int views = 0, numOfQueries = 0;
        double avgPosition =0;
        String lastVisit = "Not visited yet.";
        if(stat.values().size() > 0){
            views = Integer.parseInt(stat.get("views"));
            if(stat.get("lastVisited")!= null){
                lastVisit = stat.get("lastVisited");
            }
        }
        if(srStat.values().size() > 0){
            numOfQueries = srStat.get("freq").intValue();
            avgPosition = srStat.get("pos");
        }

        return new RecordStatistics(qp.getRecordIdWC(), views, numOfQueries, avgPosition, keyWords, lastVisit );
    }
    public List<Record> getPageViewsStatistics(){
        return pageViewHandler.getStats(vloConfig, queryParameters);
    }

    public List<Record>  getSearchResultsStatistics(boolean withHomepageResults ) {
        return searchResultHandler.getSearchResultsStat(vloConfig, queryParameters, withHomepageResults);
    }

    public HashMap<String,Integer> getKeywordsStatistics() {
        return searchQueryHandler.getKeywordsStat(vloConfig, queryParameters);
    }

    public QueryParameters getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(QueryParameters queryParameters) {
        this.queryParameters = queryParameters;
    }

    public String getKeywordsStatisticsChartData() {
        HashMap<String,Integer> searchQueries = searchQueryHandler.getSearchQueriesPerDay(vloConfig, queryParameters);
        StringBuilder sb = new StringBuilder("");
        sb.append("[['Date','Queries'],");
        for (Map.Entry<String, Integer> entry : searchQueries.entrySet()) {
            sb.append("['").append(entry.getKey()).append("',").append(entry.getValue()).append("],");
        }
        sb.append("]");
        return sb.toString();
    }

    public String getPageViewsChartData() {
        HashMap<String,Integer> pageViews = pageViewHandler.getPageViewsPerDay(vloConfig, queryParameters);
        StringBuilder sb = new StringBuilder("");
        sb.append("[['Date','Visits'],");
        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {
            sb.append("['").append(entry.getKey()).append("',").append(entry.getValue()).append("],");
        }
        sb.append("]");
        return sb.toString();
    }

    }
