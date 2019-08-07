package eu.clarin.cmdi.vlo.exposure.postgresql.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.exposure.postgresql.PageViewsHandler;
import eu.clarin.cmdi.vlo.exposure.models.PageView;

public class PageViewsHandlerImpl implements PageViewsHandler {

	private final static Logger logger = LoggerFactory.getLogger(PageViewsHandlerImpl.class);
	private final String table = "\"public\".\"PageViews\"";

	/**
	 * Adds the page view.
	 *
	 * @param vloConfig the vlo configuration
	 * @param pv        the PageView Object
	 * @return true, if successful
	 */
	@Override
	public boolean addPageView(VloConfig vloConfig, PageView pv) {
		Connection conn = PgConnection.getConnection(vloConfig);
		boolean added = false;
		String sqlQuery = "INSERT INTO " + table + "(record_id, ip, url, http_referer, \"timeSt\") "
				+ "VALUES(?,?,?,?,?)";
		long id = 0;
		int affectedRows = -1;
		if (null != conn) {
			try {
				PreparedStatement pstmt = conn.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
				// set query parameters
				pstmt.setString(1, pv.getRecordId());
				pstmt.setString(2, pv.getIp());
				pstmt.setString(3, pv.getUrl());
				pstmt.setString(4, pv.getHttpReferer());
				pstmt.setTimestamp(5, pv.getTimeStamp());
				// run the query
				affectedRows = pstmt.executeUpdate();
				if (affectedRows > 0) {
					// get the inserted ID back
					added = true;
					try (ResultSet rs = pstmt.getGeneratedKeys()) {
						if (rs.next()) {
							id = rs.getLong(1);
						}
					} catch (SQLException ex) {
						logger.error(ex.getMessage());
					}
				}
				// close the connection
				conn.close();
			} catch (SQLException ex) {
				logger.error(ex.getMessage());
				added = false;
			}
		}
		return added;
	}
}
