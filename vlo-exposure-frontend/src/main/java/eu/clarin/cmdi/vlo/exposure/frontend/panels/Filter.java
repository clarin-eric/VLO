package eu.clarin.cmdi.vlo.exposure.frontend.panels;
import java.io.Serializable;
import java.sql.Timestamp;

public class Filter implements Serializable {
    private String startDate;
    private String endDate;
    private String recordIdWildCard;
    //private int page;

    public Filter(String startDate, String endDate, String recordIdWildCard) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.recordIdWildCard = recordIdWildCard;
    }


    public String getRecordIdWildCard() {
        return recordIdWildCard;
    }

    public void setRecordIdWildCard(String recordIdWildCard) {
        this.recordIdWildCard = recordIdWildCard;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
