package eu.clarin.cmdi.vlo.exposure.postgresql.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.sym.Name;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.print.attribute.standard.MediaSize.NA;

import org.postgresql.ds.PGConnectionPoolDataSource;
import eu.clarin.cmdi.vlo.config.VloConfig;

public class PgConnection {
    private final static Logger logger = LoggerFactory.getLogger(PgConnection.class);

    private static VloConfig config = null;
    private static PGConnectionPoolDataSource source = null;

    /**
     * Sets the config.
     *
     * @param config the new vlo config
     */
    public static void setConfig(VloConfig config) {
        try {
            PgConnection.config = config;
            source = new PGConnectionPoolDataSource();
            source.setServerName(config.getVloExposureHost());
            source.setDatabaseName(config.getVloExposureDbName());
            source.setPortNumber(Integer.parseInt(config.getVloExposurePort()));
            source.setUser(config.getVloExposureUsername());
            source.setPassword(config.getVloExposurePassword());
            logger.info("PostgreSQL connections pool is created.");
        } catch (Exception e) {
            logger.warn("Something went wrong, PostgreSQL connections pool counldn't be created");
            logger.error(e.getMessage());
        }
    }

    /**
     * Sets the config.
     *
     * @param config the new vlo config
     */
    public static void setConfig(String host, String dbName, String user, String pass, String url) {
        try {
            source = new PGConnectionPoolDataSource();
            source.setServerName(host);
            source.setDatabaseName(dbName);
            source.setUser(user);
            source.setPassword(pass);
            source.setUrl(url);
            logger.info("PostgreSQL connections pool is created.");
        } catch (Exception e) {
            logger.warn("Something went wrong, PostgreSQL connections pool counldn't be created");
            logger.error(e.getMessage());
        }
    }

    /**
     * Gets the connection.
     *
     * @return the connection
     */
    public static Connection getConnection(VloConfig config) {
        Connection connection = null;
        if (null == PgConnection.source) {
            setConfig(config);
        }
        try {
            connection = source.getConnection();
        } catch (SQLException e) {
            logger.warn("Something went wrong, PostgreSQL DB is not connected.");
            logger.error(e.getMessage());
        }
        return connection;
    }

    /**
     * Gets the connection.
     *
     * @return the connection
     */
    public static Connection getConnection() {
        Connection connection = null;
        if (null != PgConnection.source) {
            try {
                connection = source.getConnection();
            } catch (SQLException e) {
                logger.warn("Something went wrong, PostgreSQL DB is not connected.");
                logger.error(e.getMessage());
            }
        }
        return connection;
    }
}
