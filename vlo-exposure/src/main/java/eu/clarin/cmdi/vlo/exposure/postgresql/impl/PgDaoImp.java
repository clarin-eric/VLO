package eu.clarin.cmdi.vlo.exposure.postgresql.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import eu.clarin.cmdi.vlo.config.VloConfig;

public class PgDaoImp {
	private final Logger logger = LoggerFactory.getLogger(PgDaoImp.class);
	private VloConfig config;
	private String dbName;
	private String port;
	private String url;
	private String host;
	private String user;
	private String password;
	private Connection connection = null;

	/**
	 * Instantiates a new PgDaoImp.
	 *
	 * @param config the VLO configuration
	 */
	public PgDaoImp(VloConfig config) {
		this.config = config;
		this.dbName = config.getPgDbName();
		this.port = config.getPgPort();
		this.host = config.getPgHost();
		this.user = config.getPgUsername();
		this.password = config.getPgPassword();
		this.url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
	}

	/**
	 * Establish DB connection.
	 *
	 * @return the connection
	 */
	public Connection connect() {
		try {
			Class.forName("org.postgresql.Driver");
			Properties props = new Properties();
			props.setProperty("user", user);
			props.setProperty("password", password);

			connection = DriverManager.getConnection(url, props);
			logger.info("Connected to the PostgreSQL server successfully.");
		} catch (SQLException e) {
			connection = null;
			logger.warn("Something went wrong, PostgreSQL DB is not connected.");
			logger.error(e.getMessage());

		} catch (Exception e) {
			connection = null;
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
	public Connection getConnection() {
		return this.connection;
	}
}
