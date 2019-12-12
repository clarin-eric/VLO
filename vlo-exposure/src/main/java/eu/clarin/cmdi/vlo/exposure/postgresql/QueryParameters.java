package eu.clarin.cmdi.vlo.exposure.postgresql;

import java.io.Serializable;
import java.sql.Timestamp;

public class QueryParameters implements Serializable {
    private Timestamp startDate;
    private Timestamp endDate;
    private String recordIdWC;

    public QueryParameters(Timestamp startDate, Timestamp endDate, String recordIdWC ) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.recordIdWC = recordIdWC;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }


    public String getRecordIdWC() {
        return recordIdWC;
    }

    public void setRecordIdWC(String recordIdWC) {
        this.recordIdWC = recordIdWC;
    }
}
