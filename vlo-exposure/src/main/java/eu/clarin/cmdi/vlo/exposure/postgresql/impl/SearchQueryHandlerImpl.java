package eu.clarin.cmdi.vlo.exposure.postgresql.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.sql.Timestamp;

import eu.clarin.cmdi.vlo.exposure.postgresql.QueryParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.clarin.cmdi.vlo.exposure.models.SearchQuery;
import eu.clarin.cmdi.vlo.exposure.models.SearchResult;
import eu.clarin.cmdi.vlo.exposure.postgresql.SearchQueryHandler;
import eu.clarin.cmdi.vlo.config.VloConfig;

public class SearchQueryHandlerImpl implements SearchQueryHandler {

    private final static Logger logger = LoggerFactory.getLogger(SearchQueryHandlerImpl.class);
    private final String table = "\"SearchQueries\"";

    public boolean addSearchQuery(VloConfig vloConfig, SearchQuery sq) {
        String sqlQuery = "INSERT INTO " + table + "(\"searchTerm\", filter,  url, ip,  \"timeSt\") "
                + "VALUES(?,?,?,?,?)";
        boolean added = false;
        SearchResultHandlerImpl srh = new SearchResultHandlerImpl();
        long id = 0;
        int affectedRows = -1;
        Connection conn = PgConnection.getConnection(vloConfig);
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
                            for (SearchResult searchResult : sq.getResults()) {
                                srh.addSearchResult(vloConfig, id, searchResult);
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

    public HashMap<String,Integer> getKeywordsStat(VloConfig vloConfig, QueryParameters qp) {
        String searchTermField = "\"searchTerm\""; 
        String timeStampField = "\"timeSt\"";
        String query = "SELECT " + searchTermField + ", count(*) as freq FROM "+ table 
                + " where " + timeStampField + " between ? and ? "
                + "and " + searchTermField + " is not null group by " + searchTermField + " order by freq DESC"; // LIMIT ? OFFSET ?
        HashMap<String,Integer> map= new LinkedHashMap<>();
        try {
            Connection conn = PgConnection.getConnection(vloConfig);
            PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            pstmt.setTimestamp(1, qp.getStartDate());
            pstmt.setTimestamp(2, qp.getEndDate());

            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {   
                String searchTerm = rs.getString("searchTerm");
                int freq = rs.getInt("freq");
                map.put(searchTerm, freq);
            }
            conn.close();
        }catch(SQLException ex){
            logger.error(ex.getMessage());
        }
        return map;
    } 
    
    public HashMap<String,Integer> getSearchQueriesPerDay(VloConfig vloConfig, QueryParameters qp) {
        String searchTermField = "\"searchTerm\""; 
        String timeStampField = "\"timeSt\"";
        String query = "SELECT date_trunc('day', " + timeStampField + ") as day, count(*) as freq FROM "+ table 
                + " where " + timeStampField + " between ? and ? "
                + "and " + searchTermField + " is not null group by day order by day "; //limit ? offset ?

        HashMap<String,Integer> chartData= new LinkedHashMap<>();
        try {
            Connection conn = PgConnection.getConnection(vloConfig);
            PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setTimestamp(1, qp.getStartDate());
            pstmt.setTimestamp(2, qp.getEndDate());

            long interval = 1000 * 60 * 60 * 24; // 1 day in millis
            long endTime = qp.getEndDate().getTime();
            long curTime = qp.getStartDate().getTime();
            while (curTime <= endTime) {
                Timestamp d =new Timestamp(curTime);
                String day = d.toString().split(" ")[0];
                chartData.put(day, 0);
                curTime += interval;
            }
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                Timestamp day = rs.getTimestamp("day");
                int freq = rs.getInt("freq");
                String d = day.toString().split(" ")[0];
                chartData.put(d, freq);
            }
            conn.close();
        }catch(SQLException ex){
            logger.error(ex.getMessage());
        }
        return chartData;
    } 
}
