package eu.clarin.cmdi.vlo.exposure.models;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Timestamp;
import eu.clarin.cmdi.vlo.exposure.postgresql.impl.SearchQueryHandlerImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;

// TODO: Auto-generated Javadoc
public class SearchQuery {
	private final Logger logger = LoggerFactory.getLogger(SearchQuery.class);
	private String searchTerm;
	private String Filter;
	private List<SearchResult> results;
	private Timestamp timeStamp;
	private String ip;
	private String url;

	/**
	 * Instantiates a new search query.
	 *
	 * @param searchTerm the search term
	 * @param filter     the query filter
	 * @param results    the results
	 * @param ip         the ip
	 * @param url        the url
	 */
	public SearchQuery(String searchTerm, String filter, List<SearchResult> results, String ip, String url) {
		super();
		this.searchTerm = searchTerm;
		this.Filter = filter;
		this.results = results;
		this.ip = ip;
		this.url = url;
		this.timeStamp = new Timestamp(System.currentTimeMillis());
	}

	/**
	 * Instantiates a new search query.
	 *
	 * @param searchTerm the search term
	 * @param filter     the query filter
	 * @param results    the results
	 * @param timeStamp  the time stamp
	 * @param ip         the ip
	 * @param url        the url
	 */
	public SearchQuery(String searchTerm, String filter, List<SearchResult> results, Timestamp timeStamp, String ip,
			String url) {
		super();
		this.searchTerm = searchTerm;
		this.Filter = filter;
		this.results = results;
		this.timeStamp = timeStamp;
		this.ip = ip;
		this.url = url;
	}

	/**
	 * Save.
	 *
	 * @param vloConfig the vlo config
	 * @return true, if successful
	 */
	public boolean save(VloConfig vloConfig) {
		SearchQueryHandlerImpl sqh = new SearchQueryHandlerImpl();
		boolean saved = false;
		try {
			saved = sqh.addSearchQuery(vloConfig, this);
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		return saved;
	}

	/**
	 * Gets the search term.
	 *
	 * @return the search term
	 */
	public String getSearchTerm() {
		return searchTerm;
	}

	/**
	 * Sets the search term.
	 *
	 * @param searchTerm the new search term
	 */
	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	/**
	 * Gets the filter.
	 *
	 * @return the filter
	 */
	public String getFilter() {
		return Filter;
	}

	/**
	 * Sets the filter.
	 *
	 * @param filter the new filter
	 */
	public void setFilter(String filter) {
		Filter = filter;
	}

	/**
	 * Gets the results.
	 *
	 * @return the results
	 */
	public List<SearchResult> getResults() {
		return results;
	}

	/**
	 * Sets the results.
	 *
	 * @param results the new results
	 */
	public void setResults(List<SearchResult> results) {
		this.results = results;
	}

	/**
	 * Gets the time stamp.
	 *
	 * @return the time stamp
	 */
	public Timestamp getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Sets the time stamp.
	 *
	 * @param timeStamp the new time stamp
	 */
	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * Gets the ip.
	 *
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Sets the ip.
	 *
	 * @param ip the new ip
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the url.
	 *
	 * @param url the new url
	 */
	public void setUrl(String url) {
		this.url = url;
	}
}
