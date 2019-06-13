package eu.clarin.cmdi.vlo.exposure.models;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Timestamp;
import eu.clarin.cmdi.vlo.exposure.postgresql.impl.SearchQueryHandlerImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.exposure.postgresql.VloExposureException;

public class SearchQuery {
	private final  Logger logger = LoggerFactory.getLogger(SearchQuery.class);

	private String searchTerm;
	private String Filter;
	private List<SearchResult> results;
	private Timestamp timeStamp;
	private String ip;
	private String url;
	
	public SearchQuery(String searchTerm, String filter, List<SearchResult> results, String ip, String url) {
		super();
		this.searchTerm = searchTerm;
		this.Filter = filter;
		this.results = results;
		this.ip = ip;
		this.url = url;
		this.timeStamp = new Timestamp(System.currentTimeMillis());

	}

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
	
	public boolean save(VloConfig vloConfig) throws VloExposureException {
		SearchQueryHandlerImpl sqh= new SearchQueryHandlerImpl();
		boolean saved = false;
		try {
			if(sqh.addSearchQuery(vloConfig, this)) {
				System.out.print("add to DB");
				saved = true;
			}
				
		}catch(Exception ex) {
			logger.error(ex.getMessage());
            System.out.println(ex.getMessage());
		}
		return saved;
	}
	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public String getFilter() {
		return Filter;
	}

	public void setFilter(String filter) {
		Filter = filter;
	}

	public List<SearchResult> getResults() {
		return results;
	}

	public void setResults(List<SearchResult> results) {
		this.results = results;
	}

	public Timestamp getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	
}
