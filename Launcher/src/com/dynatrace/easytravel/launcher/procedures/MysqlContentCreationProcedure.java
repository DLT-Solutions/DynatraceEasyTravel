package com.dynatrace.easytravel.launcher.procedures;

import java.io.IOException;
import java.sql.*;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.database.CreateMysqlContent;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.AbstractProcedure;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopListener;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;


/**
 * Create the MySQL content
 *
 * @author cwat-cchen
 *
 */
public class MysqlContentCreationProcedure extends AbstractProcedure {
	private static final Logger LOGGER = LoggerFactory.make();

	private final AtomicBoolean isRunning = new AtomicBoolean(false);
	public static final String database = BaseConstants.EASYTRAVEL.toLowerCase();
	private static final Object lock = new Object();
	private Connection conn = null;

	public MysqlContentCreationProcedure(ProcedureMapping mapping)
			throws IllegalArgumentException {
		super(mapping);
	}

	@Override
	public Feedback run() {
		final int amount = 1000; // the amount of data

		isRunning.set(true);
		synchronized (lock) {
			try {
				conn = acquireMySQLConnection();
				if (!checkIfdatabaseExists(database)) {
					LOGGER.info(TextUtils.merge("Database ''{0}'' was not found, creating one...", database));
					try {
						try (Statement s = conn.createStatement();) {

							// create easytravel database
							s.executeUpdate("CREATE DATABASE " + database);
							LOGGER.info(TextUtils.merge("Database ''{0}'' has been successfully created.", database));

							// create two tables, guestbook and rating
							String createGuestbookSql = "CREATE TABLE " + database + ".guestbook "
									+ "(no int(11) NOT NULL AUTO_INCREMENT, " + " name varchar(255) NOT NULL, "
									+ " journey_id int(11) NOT NULL, " + " comment varchar(300) NOT NULL, "
									+ " comment_date varchar(30) NOT NULL, " + " PRIMARY KEY (no, name, journey_id))";

							s.executeUpdate(createGuestbookSql);
							LOGGER.info("table 'guestbook' has been successfully created.");

							String createRatingSql = "CREATE TABLE " + database + ".rating "
									+ "( journey_id varchar(20) NOT NULL, " + " number_votes int(11) DEFAULT NULL, "
									+ " total_points int(11) DEFAULT NULL, " + " dec_avg float DEFAULT NULL, "
									+ " whole_avg float DEFAULT NULL, " + " voting float DEFAULT NULL, "
									+ " PRIMARY KEY (journey_id))";

							s.executeUpdate(createRatingSql);
							LOGGER.info("table 'rating' has been successfully created.");

							CreateMysqlContent mysqlContentCreator = new CreateMysqlContent(amount);

							try {
								mysqlContentCreator.create();
							} finally {
								mysqlContentCreator.close();
							}
						}
					} catch (IOException e) {
						LOGGER.warn("Exception while creating mysql content", e);
						return Feedback.Failure;
					} catch (SQLException e) {
						LOGGER.warn("Exception while creating mysql content", e);
						return Feedback.Failure;
					}
				} else {
					LOGGER.info("Database content will be reused, database exists already. ");
				}
			} finally {
				isRunning.set(false);
				releaseMySQLConnection();
			}
		}
		return Feedback.Neutral;
	}

    /**
     * Estabslish MySQL Connection
     * @return
     */
    private Connection acquireMySQLConnection() {
        final EasyTravelConfig config = EasyTravelConfig.read();

        Connection conn = null;

        try {
            conn = DriverManager.getConnection(config.mysqlUrl, config.mysqlUser, config.mysqlPassword);
            LOGGER.info(TextUtils.merge("Established connection to MySQL {0} ", getMySQLVersion(conn)));
        } catch (SQLException e) {
            throw new RuntimeException("Cannot establish MySQL connection");
        }

        return conn;
    }

    /**
     * Close MySQL connection
     */
    private void releaseMySQLConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                LOGGER.info("Closing MySQL connection...");
            }
        } catch (SQLException e) {
            LOGGER.info("Cannot close MySQL connection", e);
        }
    }

    /**
     * Get MySQL instance product version
     *
     * @param conn
     * @return
     */
    private String getMySQLVersion(Connection conn) {
        String dbProductVersion = null;
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            dbProductVersion = metaData.getDatabaseProductVersion();
        } catch (SQLException e) {
            LOGGER.warn("Cannot get MySQL product version", e);
        }
        return dbProductVersion;
    }

    /**
	 * Check if the specified database exists
	 *
	 * @param database
	 */
	private synchronized boolean checkIfdatabaseExists(String database) {
        boolean databaseExists = false;
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getCatalogs();

            LOGGER.info(TextUtils.merge("Looking for ''{0}'' database...", database));

            while (rs.next()) {
                String listOfDatabase = rs.getString("TABLE_CAT");
                if (database.equalsIgnoreCase(listOfDatabase)) {
                    databaseExists = true;
                    LOGGER.info(TextUtils.merge("Found ''{0}'' database. ", database));
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(TextUtils.merge("Cannot find ''{0}'' database.", database), e);
        }

        return databaseExists;
    }

	@Override
	public boolean isStoppable() {
		return true;
	}

	@Override
	public StopMode getStopMode() {
		return StopMode.PARALLEL;
	}

	@Override
	public Feedback stop() {
		LOGGER.debug("Stopping procedures. Stopping MysqlContentCreator");
		// wait until procedure is done before allowing DBMS to be stopped
		while (isRunning()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				LOGGER.warn("Interrupted while waiting for procedure", e);
			}
		}

		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				LOGGER.warn("Error closing connection", e);
			}
			conn = null;
		}

		LOGGER.debug("Stopping procedures. MysqlContentCreator stopped");
		return Feedback.Success;
	}

	@Override
	public boolean isRunning() {
		return isRunning.get();
	}

	@Override
	public boolean isOperatingCheckSupported() {
		return false;
	}

	@Override
	public boolean isOperating() {
		throw new UnsupportedOperationException("Operating check not supported.");
	}

	@Override
	public boolean isSynchronous() {
		return true;
	}

	@Override
	public String getDetails() {
		return "Rnning: " + isRunning.get();
	}

	@Override
	public boolean hasLogfile() {
		return false;
	}

	@Override
	public String getLogfile() {
		return null;
	}

	@Override
	public boolean agentFound() {
		return false;
	}

	@Override
	public Technology getTechnology() {
		return null;
	}

	@Override
	public void addStopListener(StopListener stopListener) {
		// stop notifications not supported
	}

	@Override
	public void removeStopListener(StopListener stopListener) {
		// stop notifications not supported
	}

	@Override
	public void clearStopListeners() {
		// stop notifications not supported
	}

}
