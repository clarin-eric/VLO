package eu.clarin.cmdi.vlo.exposure.frontend.panels;
import java.io.Serializable;

public class Filter implements Serializable {
    private String startDate;
    private String endDate;
    private String recordIdWildCard;

    public Filter(String startDate, String endDate, String recordIdWildCard) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.recordIdWildCard = recordIdWildCard;
    }


    String getRecordIdWildCard() {
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
