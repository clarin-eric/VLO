package eu.clarin.cmdi.vlo.exposure.postgresql.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import eu.clarin.cmdi.vlo.exposure.models.Record;
import eu.clarin.cmdi.vlo.exposure.postgresql.QueryParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.exposure.models.PageView;
import eu.clarin.cmdi.vlo.exposure.postgresql.PageViewsHandler;

public class PageViewsHandlerImpl implements PageViewsHandler {

    private final static Logger logger = LoggerFactory.getLogger(PageViewsHandlerImpl.class);
    private final String table = "\"PageViews\"";

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
        String query = "INSERT INTO " + table + "(record_id, ip, url, http_referer, \"timeSt\") "
                + "VALUES(?,?,?,?,?)";
        long id = 0;
        int affectedRows = -1;
        if (null != conn) {
            try {
                PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
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
    
    public List<Record> getStats(VloConfig vloConfig, QueryParameters qp){
        String query = "SELECT record_id, count(*) as views FROM "+ table +
                " where record_id like ? and \"timeSt\" between ? and ? " +
                "group by record_id order by views DESC "; //LIMIT ? OFFSET ?
        List<Record> records= new ArrayList<>();
        try {
            Connection conn = PgConnection.getConnection(vloConfig);
            PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, qp.getRecordIdWC());
            pstmt.setTimestamp(2, qp.getStartDate());
            pstmt.setTimestamp(3, qp.getEndDate());

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {   
                String recordId = rs.getString("record_id");
                int views = rs.getInt("views");
                records.add(new Record(recordId, views));
            }
            conn.close();
        }catch(SQLException ex){
            logger.error(ex.getMessage());
        }
        
        return records;
    } 

    // get statistics by record_id
    public HashMap<String,String> getStatByRecordId(VloConfig vloConfig, QueryParameters qp){
        HashMap<String,String> returnValues = new LinkedHashMap<String, String>();
        String timeStampField = "\"timeSt\"";
        String query = "SELECT " + table + ".\"record_id\", count(*) as views, "
                + "min(" + table + ".\"timeSt\") as lastVisited FROM "+ table + 
                "WHERE record_id=? " +
                " and " + timeStampField + " between ? and ? " +
                " group by "+ table +".\"record_id\"";
        try {
            Connection conn = PgConnection.getConnection(vloConfig);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, qp.getRecordIdWC());
            pstmt.setTimestamp(2, qp.getStartDate());
            pstmt.setTimestamp(3, qp.getEndDate());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                int views = rs.getInt("views");
                Timestamp lastVisited = rs.getTimestamp("lastVisited");
                returnValues.put("views", String.valueOf(views));
                returnValues.put("lastVisited", lastVisited.toString());
            }
            conn.close();
        }catch(Exception ex){
            logger.error(ex.getMessage());
        }
        return returnValues;
    }

    // get statistics by record_id
    public HashMap<String,Integer> getKeyWordsByRecordId(VloConfig vloConfig, QueryParameters qp){
        String timeStampField = "\"timeSt\"";
        String queriesTable = "\"SearchQueries\"";
        String resultsTable = "\"SearchResults\"";
        String query = "SELECT "+queriesTable+".\"searchTerm\", count(*) as views FROM " + queriesTable
                + " WHERE "+ queriesTable + ".id in ( "
                + " select distinct(query_id) from " + resultsTable +" WHERE record_id =?"
                +")  and " + timeStampField + " between ? and ? " +
                " group by \"searchTerm\";";
        HashMap<String,Integer> map= new LinkedHashMap<>();
        try {
            Connection conn = PgConnection.getConnection(vloConfig);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, qp.getRecordIdWC());
            pstmt.setTimestamp(2, qp.getStartDate());
            pstmt.setTimestamp(3, qp.getEndDate());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                String searchTerm = rs.getString("searchTerm");
                int freq = rs.getInt("views");
                map.put(searchTerm, freq);
            }
        }catch(Exception ex){
            logger.error(ex.getMessage());
        }
        return map;
    }
}
