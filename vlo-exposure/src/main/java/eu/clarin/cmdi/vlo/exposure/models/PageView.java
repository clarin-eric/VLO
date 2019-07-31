package eu.clarin.cmdi.vlo.exposure.models;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.exposure.postgresql.impl.PageViewsHandlerImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;

public class PageView{

	private final static Logger logger = LoggerFactory.getLogger(PageView.class);
	private int id;
	private String recordId;
	private String ip;
	private String url;
	private String httpReferer;
	private Timestamp timeStamp;

	/**
	 * Instantiates a new page view.
	 *
	 * @param recordId the record id
	 * @param ip the ip
	 * @param url the url
	 * @param httpReferer the http referer
	 * @param ts the Time Stamp
	 */
	public PageView(String recordId, String ip, String url, String httpReferer, Timestamp ts) {
		this.recordId = recordId;
		this.ip = ip;
		this.url = url;
		this.httpReferer = httpReferer;
		this.timeStamp = ts;
	}

	/**
	 * Instantiates a new page view.
	 *
	 * @param recordId the record id
	 * @param ip the ip
	 * @param url the url
	 * @param httpReferer the http referer
	 */
	public PageView(String recordId, String ip, String url, String httpReferer) {
		this.recordId = recordId;
		this.ip = ip;
		this.url = url;
		this.httpReferer = httpReferer;
		this.timeStamp = new Timestamp(System.currentTimeMillis());
	}

	/**
	 * Save the page view object to the DB.
	 *
	 * @param vloConfig the vlo project configuration 
	 * @return true, if successful
	 */
	public boolean save(VloConfig vloConfig) {
		PageViewsHandlerImpl pvh= new PageViewsHandlerImpl();
		boolean saved = false;
		try {
			saved = pvh.addPageView(vloConfig, this);				
		}catch(Exception ex) {
			logger.error(ex.getMessage());
		}
		return saved;
	}

	/**
	 * Gets the record id.
	 *
	 * @return the record id
	 */
	public String getRecordId() {
		return this.recordId;
	}

	/**
	 * Gets the ip.
	 *
	 * @return the ip
	 */
	public String getIp() {
		return this.ip;
	}

	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getUrl() {
		return this.url;
	}

	/**
	 * Gets the http referer.
	 *
	 * @return the http referer
	 */
	public String getHttpReferer() {
		return this.httpReferer;
	}

	/**
	 * Gets the time stamp.
	 *
	 * @return the time stamp
	 */
	public Timestamp getTimeStamp() {
		return this.timeStamp;
	}
}
