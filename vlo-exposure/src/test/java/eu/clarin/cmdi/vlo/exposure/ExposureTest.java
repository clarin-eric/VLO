package eu.clarin.cmdi.vlo.exposure;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import com.opentable.db.postgres.embedded.FlywayPreparer;

import eu.clarin.cmdi.vlo.exposure.postgresql.impl.*;
import eu.clarin.cmdi.vlo.exposure.models.PageView;
import eu.clarin.cmdi.vlo.exposure.models.SearchQuery;
import eu.clarin.cmdi.vlo.exposure.models.SearchResult;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

import eu.clarin.cmdi.vlo.config.VloConfig;
import com.opentable.db.postgres.junit.EmbeddedPostgresRules;
import com.opentable.db.postgres.junit.PreparedDbRule;
import org.junit.Ignore;

@Ignore
public class ExposureTest {
    private final static Logger logger = LoggerFactory.getLogger(ExposureTest.class);
    private EmbeddedPostgres pg = null;
    @Rule
    public PreparedDbRule db = EmbeddedPostgresRules.preparedDatabase(FlywayPreparer.forClasspathLocation("db"));

    @Before
    public void initializeEmbeddedPg() {
        try {
            // default creditials by OpenTable Embedded PostgreSQL Component
            String username = "postgres";
            String password = "postgres";
            String host = "localhost";
            String dbName = "postgres";
            String url = db.getTestDatabase().getConnection().getMetaData().getURL().toString();
            PgConnection.setConfig(host, dbName, username, password, url);
            Connection conn = PgConnection.getConnection();
            assertNotNull(conn);
        } catch (Exception e) {
            System.out.print(e);
            fail();
        }
    }

    @Test
    public void testPgConnection() throws Exception {
        try {
            Connection conn = PgConnection.getConnection(); // db.getTestDatabase().getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = s
                    .executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='public'");
            assertTrue(rs.next());
            assertEquals("flyway_schema_history", rs.getString(1));
            assertTrue(rs.next());
            assertEquals("PageViews", rs.getString(1));
            assertTrue(rs.next());
            assertEquals("SearchQueries", rs.getString(1));
            assertTrue(rs.next());
            assertEquals("SearchResults", rs.getString(1));
            assertFalse(rs.next());
        } catch (Exception e) {
            System.out.print(e);
            fail();
        }
    }

    @Test
    public void testAddSearchQuery() {
        VloConfig vloConfig = new VloConfig();
        // create SearchResults List
        List<SearchResult> results = new ArrayList<SearchResult>();
        results.add(new SearchResult("RecordId_1", 1, 1));
        results.add(new SearchResult("RecordId_2", 2, 1));
        results.add(new SearchResult("RecordId_3", 3, 1));
        // Create SearchQuery Object
        SearchQuery sq = new SearchQuery("TestTerm", "TestFilter", results, new Timestamp(System.currentTimeMillis()),
                "0.0.0.0", "www.test.test");
        // Save it to the database
        assertTrue(sq.save(vloConfig));
        try {
            Connection conn = PgConnection.getConnection();
            Statement s = conn.createStatement();
            // get number of entries from SearchQueries table and be sure there is only one
            // entry
            ResultSet rs = s.executeQuery("SELECT count(*) FROM \"public\".\"SearchQueries\"");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
            // get number of entries from SeachResults table and be sure they are exactly 3
            // entries
            rs = s.executeQuery("SELECT count(*) FROM \"public\".\"SearchResults\"");
            assertTrue(rs.next());
            assertEquals(3, rs.getInt(1));
        } catch (Exception e) {
            logger.error(e.getMessage());
            fail();
        }
    }

    @Test
    public void testAddPageView() {
        VloConfig vloConfig = new VloConfig();
        String recordId = "testId";
        String ip = "testIp";
        String url = "www.test.test";
        String httpReferer = "http://test";
        // create PageView Object
        PageView pv = new PageView(recordId, ip, url, httpReferer);
        // save it to the database and assert
        assertTrue(pv.save(vloConfig));
        try {
            Connection conn = PgConnection.getConnection();
            Statement s = conn.createStatement();
            // get number of entries from PageViews table and be sure there is only one
            // entry
            ResultSet rs = s.executeQuery("SELECT count(*) FROM \"public\".\"PageViews\"");
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        } catch (Exception e) {
            logger.error(e.getMessage());
            fail();
        }
    }

}
