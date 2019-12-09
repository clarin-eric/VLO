package eu.clarin.cmdi.vlo.exposure.postgresql.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import eu.clarin.cmdi.vlo.exposure.models.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.clarin.cmdi.vlo.exposure.postgresql.*;
import eu.clarin.cmdi.vlo.exposure.postgresql.SearchResultHandler;
import eu.clarin.cmdi.vlo.exposure.models.SearchResult;
import eu.clarin.cmdi.vlo.config.VloConfig;

public class SearchResultHandlerImpl implements SearchResultHandler {

    private final static Logger logger = LoggerFactory.getLogger(SearchResultHandlerImpl.class);
    private final String table = "\"SearchResults\"";

    @Override
    public boolean addSearchResult(VloConfig vloConfig, long queryId, SearchResult sr) {
        String sqlQuery = "INSERT INTO " + table + "(\"query_id\", record_id,  position, page) " + "VALUES(?,?,?,?)";
        boolean added = false;
        int affectedRows = -1;
        Connection conn = PgConnection.getConnection(vloConfig);
        if (null != conn) {
            try {
                PreparedStatement pstmt = conn.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
                // set query parameters
                pstmt.setLong(1, queryId);
                pstmt.setString(2, sr.getRecordId());
                pstmt.setLong(3, sr.getPosition());
                pstmt.setLong(4, sr.getPage());
                // run the query
                affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    added = true;
                }
                // close connection
                conn.close();
            } catch (SQLException ex) {
                logger.error(ex.getMessage());
            }
        }
        return added;
    }

    public List<Record> getSearchResultsStat(VloConfig vloConfig, QueryParameters qp, boolean withHomepageResults) {
        String timeStampField = "\"timeSt\"";
        String searchQueryTable = "\"SearchQueries\"";
        String condition = "";
        if(!withHomepageResults){
            condition = "  and \"searchTerm\" is not NULL ";
        }
        String query = "SELECT record_id, count(*) as freq FROM " + table +
                " , " + searchQueryTable + " where record_id like ? and "
                + timeStampField + " between ? and ? and "
                + table + ".query_id = " +searchQueryTable+".id " +
                condition
                + "group by record_id order by freq DESC";

        List<Record> records = new ArrayList<>();

        try {
            Connection conn = PgConnection.getConnection(vloConfig);
            PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, qp.getRecordIdWC());
            pstmt.setTimestamp(2, qp.getStartDate());
            pstmt.setTimestamp(3, qp.getEndDate());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String record_id = rs.getString("record_id");
                int freq = rs.getInt("freq");
                records.add(new Record(record_id, freq));
            }
            conn.close();
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        }
        return records;
    }

    public HashMap<String, Double> getSearchResultsStatPerRecordId(VloConfig vloConfig, QueryParameters qp) {

        String timeStampField = "\"timeSt\"";
        String searchQueryTable = "\"SearchQueries\"";

        String query = "SELECT count(*) as freq,  avg(position) as pos FROM " + table + " , " + searchQueryTable + " where "
                + timeStampField + " between ? and ? and "
                + table + ".query_id = " +searchQueryTable+".id and "
                + "record_id = ?"
                + " group by record_id";

        HashMap<String, Double> map = new LinkedHashMap<>();
        try {
            Connection conn = PgConnection.getConnection(vloConfig);
            PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setTimestamp(1, qp.getStartDate());
            pstmt.setTimestamp(2, qp.getEndDate());
            pstmt.setString(3, qp.getRecordIdWC());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {

                Double freq = rs.getDouble("freq");
                Double pos = rs.getDouble("pos");
                map.put("freq", freq);
                map.put("pos", pos);
            }
            conn.close();
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        }
        return map;
    }
}
