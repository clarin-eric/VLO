package eu.clarin.cmdi.vlo.exposure.postgresql.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

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
			source.setServerName(config.getPgHost());
			source.setDatabaseName(config.getPgDbName());
			source.setPortNumber(Integer.parseInt(config.getPgPort()));
			source.setUser(config.getPgUsername());
			source.setPassword(config.getPgPassword());
			logger.info("PostgreSQL connections pool is created.");
		}catch(Exception e) {
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
		if(null == PgConnection.config) {
			setConfig(config);
		}
		try {
			connection = source.getConnection();
		}catch(SQLException e) {
			logger.warn("Something went wrong, PostgreSQL DB is not connected.");
			logger.error(e.getMessage());
		}
		return connection;
	}
}
