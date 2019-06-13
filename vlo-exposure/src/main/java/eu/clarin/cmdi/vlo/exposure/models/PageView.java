package eu.clarin.cmdi.vlo.exposure.models;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.exposure.postgresql.impl.PageViewsHandlerImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.exposure.postgresql.VloExposureException;

public class PageView{
	private final static Logger logger = LoggerFactory.getLogger(PageView.class);

	private int id;
	private String recordId;
	private String ip;
	private String url;
	private String httpReferer;
	private Timestamp timeStamp;
	
	
	public PageView(String recordId, String ip, String url, String httpReferer, Timestamp ts) {
		this.recordId = recordId;
		this.ip = ip;
		this.url = url;
		this.httpReferer = httpReferer;
		this.timeStamp = ts;
	}
	
	public PageView(String recordId, String ip, String url, String httpReferer) {
		this.recordId = recordId;
		this.ip = ip;
		this.url = url;
		this.httpReferer = httpReferer;
		this.timeStamp = new Timestamp(System.currentTimeMillis());

	}
	
	public boolean save(VloConfig vloConfig) throws VloExposureException{
		PageViewsHandlerImpl pvh= new PageViewsHandlerImpl();
		boolean saved = false;
		try {
			if(pvh.addPageView(vloConfig, this)) {
				System.out.print("add to DB");
				saved = true;
			}
				
		}catch(VloExposureException ex) {
			logger.error(ex.getMessage());
		}
		return saved;
	}
	
	public String getRecordId() {
		return this.recordId;
	}
	
	public String getIp() {
		return this.ip;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public String getHttpReferer() {
		return this.httpReferer;
	}
	
	public Timestamp getTimeStamp() {
		return this.timeStamp;
	}
}
