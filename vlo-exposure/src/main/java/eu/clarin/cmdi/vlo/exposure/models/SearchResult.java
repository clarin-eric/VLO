package eu.clarin.cmdi.vlo.exposure.models;

import java.sql.Timestamp;

public class SearchResult {

	private String recordId;
	private int position;
	private int page;
	
	public SearchResult(String recordId, int position, int page) {
		this.recordId = recordId;
		this.position = position;
		this.page = page;
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}
	
	
	
}
