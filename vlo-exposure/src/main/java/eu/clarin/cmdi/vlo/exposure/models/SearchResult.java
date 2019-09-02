package eu.clarin.cmdi.vlo.exposure.models;

import java.sql.Timestamp;

public class SearchResult {
    private String recordId;
    private int position;
    private int page;

    /**
     * Instantiates a new SearchResult instance.
     *
     * @param recordId the record id
     * @param position the position
     * @param page     the page
     */
    public SearchResult(String recordId, int position, int page) {
        this.recordId = recordId;
        this.position = position;
        this.page = page;
    }

    /**
     * Gets the recordId.
     *
     * @return the recordId
     */
    public String getRecordId() {
        return recordId;
    }

    /**
     * Sets the record id.
     *
     * @param recordId the new record id
     */
    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    /**
     * Gets the position.
     *
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Sets the position.
     *
     * @param position the new position
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Gets the page.
     *
     * @return the page
     */
    public int getPage() {
        return page;
    }

    /**
     * Sets the page.
     *
     * @param page the new page
     */
    public void setPage(int page) {
        this.page = page;
    }
}
