package eu.clarin.cmdi.vlo.exposure.postgresql.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.exposure.models.SearchQuery;
import eu.clarin.cmdi.vlo.exposure.models.SearchResult;
import eu.clarin.cmdi.vlo.exposure.postgresql.impl.PgDaoImp;
import eu.clarin.cmdi.vlo.exposure.postgresql.SearchQueryHandler;
import eu.clarin.cmdi.vlo.exposure.postgresql.impl.SearchResultHandlerImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;

public class SearchQueryHandlerImpl implements SearchQueryHandler {

	private final static Logger logger = LoggerFactory.getLogger(SearchQueryHandlerImpl.class);
	private final String table = "\"public\".\"SearchQueries\"";

	public boolean addSearchQuery(VloConfig vloConfig, SearchQuery sq) {
		String sqlQuery = "INSERT INTO " + table + "(\"searchTerm\", filter,  url, ip,  \"timeSt\") "
				+ "VALUES(?,?,?,?,?)";
		boolean added = false;
		SearchResultHandlerImpl srh = new SearchResultHandlerImpl();
		long id = 0;
		int affectedRows = -1;
		PgDaoImp PgConn = new PgDaoImp(vloConfig);
		Connection conn = PgConn.connect();
		if (null != conn) {
			try {
				PreparedStatement pstmt = conn.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
				// set query parameters
				pstmt.setString(1, sq.getSearchTerm());
				pstmt.setString(2, sq.getFilter());
				pstmt.setString(3, sq.getUrl());
				pstmt.setString(4, sq.getIp());
				pstmt.setTimestamp(5, sq.getTimeStamp());
				// run the query
				affectedRows = pstmt.executeUpdate();
				if (affectedRows > 0) {
					// get the ID back
					added = true;

					try (ResultSet rs = pstmt.getGeneratedKeys()) {
						if (rs.next()) {
							id = rs.getLong(1);
							// add Search Results;
							Iterator sr = sq.getResults().iterator();
							while (sr.hasNext()) {
								srh.addSearchResult(vloConfig, id, (SearchResult) sr.next());
							}
						}
					} catch (SQLException ex) {
						logger.error(ex.getMessage());
					}
				}
				conn.close();
			} catch (SQLException ex) {
				logger.error(ex.getMessage());
			}

		}
		return added;
	}
}
