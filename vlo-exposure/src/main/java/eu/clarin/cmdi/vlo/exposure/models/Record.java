package eu.clarin.cmdi.vlo.exposure.models;

import java.io.Serializable;

public class Record implements Serializable {
    private String recordId;
    private int freq;
    private String internalUrl;
    private String externalUrl;

    public Record(String recordId, int freq) {
        this.recordId = recordId;
        this.freq = freq;
        internalUrl = "record?id="+recordId;
        externalUrl = "http://vlo.clarin.eu/record?docId=" + recordId;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public String getInternalUrl() {
        return internalUrl;
    }

    public void setInternalUrl(String internalUrl) {
        this.internalUrl = internalUrl;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }
}
