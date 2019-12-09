package eu.clarin.cmdi.vlo.exposure.frontend.service;

import java.util.HashMap;

public class RecordStatistics {
    String recordId;
    int views;
    int numOfQueries;
    HashMap<String, Integer> keyWords;
    double averagePosition;
    String lastVisit;
    public RecordStatistics(String recordId, int views, int numOfQueries, double averagePosition, HashMap<String, Integer> keyWords, String lastVisit ) {
        this.recordId = recordId;
        this.views = views;
        this.numOfQueries = numOfQueries;
        this.averagePosition = averagePosition;
        this.keyWords = keyWords;
        this.lastVisit = lastVisit;
    }

    public String getLastVisit() {
        return lastVisit;
    }

    public void setLastVisit(String lastVisit) {
        this.lastVisit = lastVisit;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getNumOfQueries() {
        return numOfQueries;
    }

    public void setNumOfQueries(int numOfQueries) {
        this.numOfQueries = numOfQueries;
    }

    public HashMap<String, Integer> getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(HashMap<String, Integer> keyWords) {
        this.keyWords = keyWords;
    }

    public double getAveragePosition() {
        return averagePosition;
    }

    public void setAveragePosition(double averagePosition) {
        this.averagePosition = averagePosition;
    }
}
