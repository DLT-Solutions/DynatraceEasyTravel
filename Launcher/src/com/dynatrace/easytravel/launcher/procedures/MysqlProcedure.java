package com.dynatrace.easytravel.launcher.procedures;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.AbstractNativeProcedure;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.mysqld.MysqlUtils;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;


/**
 * Start and stop the Mysql Database Management System.
 *
 * @author Chun-ting Chen
 */

public class MysqlProcedure extends AbstractNativeProcedure {

	private static final Logger LOGGER = LoggerFactory.make();

	public static final int DEFAULT_PORTNUMBER = 3306;
	private static final String CURRENT_DATA_VERSION = "5.6.19";

	public MysqlProcedure(ProcedureMapping mapping)
			throws CorruptInstallationException {

		super(mapping);
		adaptAndCopyMySQLConf();
		adjustMysqlSetting();
		copyMySQLDataFolder();
		createBusinessDBFolder();

		final EasyTravelConfig config = EasyTravelConfig.read();

		int dbServerPort = parsePort(config.mysqlPort);
		LOGGER.info("Starting MySQL database management system on host: '" + config.mysqlHost + "' and port: '" +
				dbServerPort + "'");

	}

    @Override
    public Feedback run() {
        Feedback processStartFeedback = super.run();
        if (!processStartFeedback.isOk()) {
            return processStartFeedback;
        }

        if (!waitUntilRunning()) {
            LOGGER.warn(TextUtils.merge("Unable to wait until {0} has been started.", getName()));
            return Feedback.Failure;
        }

        return Feedback.Success;
    }

    /* This initializer copies the required files to the EasyTravel
    * configuration folder. Moreover, it initiates the configuration of the
    * my.ini file by calling HttpConfSetup.write(); */
	private void adaptAndCopyMySQLConf() {
		final EasyTravelConfig config = EasyTravelConfig.read();
		if (config.mysqlGeneratedMyIniConfig || isMysqlConfFileMissing()) {

			try {
				FileUtils.copyFile(new File(MysqlUtils.MySQL_INI_CONF),
						new File(MysqlUtils.MySQL_INI));
				MySQLConfSetup.write();
			} catch (IOException e) {
				LOGGER.warn("Could not set up my.ini file", e);
			}
		}

	}

	/**
	 * This method enforces the use of the my.ini file in the default
	 * EasyTravel configuration folder (i.e., /.dynaTrace/easyTravel
	 * X.X.X/config).
	 */
	private void adjustMysqlSetting() {
		File mysqlConf = new File(Directories.getConfigDir().getAbsolutePath() + "/my.ini");

		process.addApplicationArgument("--defaults-file=" + mysqlConf.getAbsolutePath());
		process.addApplicationArgument("--basedir=" + MysqlUtils.getMySqlbaseDir());

	}

	/**
	 * check if the easyTravelRatings directory exists and copy data/mysql
	 * to EasyTravel configuration folder
	 */

	private void copyMySQLDataFolder() {
		File mysqlDataFolder = new File(Directories.getMysqlDataDir().getAbsoluteFile() + "/");
		File mysqlDataDir = new File(Directories.getMysqlDir().getAbsolutePath() + "/data");
		
		removeIncompatibleDataFolder();
		
		if (!mysqlDataFolder.exists()) {
			try {
				FileUtils.copyDirectory(mysqlDataDir, mysqlDataFolder);
			} catch (IOException e) {
				LOGGER.warn("Could not copy data/mysql directory", e);
			}
		} 
	}
	
	private void removeIncompatibleDataFolder() {		
		String version = getCurrentVersionOfDataDir();
		
		if (!CURRENT_DATA_VERSION.equals(version)) {
			LOGGER.warn( TextUtils.merge("Removing myslq data directory. Old version: {0}, new version: {1}", version, CURRENT_DATA_VERSION));
			try {
				FileUtils.deleteDirectory(Directories.getMysqlDataDir().getAbsoluteFile());
			} catch (IOException e) {
				LOGGER.warn("Could not delete directory: " + Directories.getMysqlDataDir().getAbsoluteFile(), e);
			}
		}
	}
	
	private String getCurrentVersionOfDataDir() {
		File versionFile = new File(Directories.getMysqlDataDir(), "Version.txt");
		String version = "";
		try {
			version = FileUtils.readFileToString(versionFile);
		} catch (IOException e) {
			LOGGER.warn("Cannot get data directory version: " + e);
		}

		return version;
	}

	/**
	 * Check if the easyTravelBusiness directory exists and create it if necessary.
	 * The database content creator for SQL expects this folder to already exist.
	 * It is debatable whether we should create it here, or in the actual db content creator,
	 * but since here we are preparing folders for the Bolg, we can also do the same
	 * for the business database.
	 * It follows that if mySQL is used as the main database, the eT configuration
	 * properties should state the same database name:
	 * 		config.databaseUrl=jdbc:mysql://<host>/easyTravelBusiness
	 */

	private void createBusinessDBFolder() {
	File mysqlBusinessFolder = new File(Directories.getMysqlDataDir().getAbsoluteFile() + "/" + "easyTravelBusiness");
	
		if (!mysqlBusinessFolder.mkdirs() && !mysqlBusinessFolder.isDirectory()) {
			
			LOGGER.warn("Could not create mySQL business folder."); 
		}
	}

	private boolean isMysqlConfFileMissing() {
		return !new File(MysqlUtils.MySQL_INI).exists();
	}

	private static int parsePort(int port) {
		if (port >= 0) {
			return port;
		}

		return DEFAULT_PORTNUMBER;
	}

	@Override
	public boolean isOperatingCheckSupported() {
		return true;
	}

	@Override
	public boolean isOperating() {
		final EasyTravelConfig config = EasyTravelConfig.read();

        Connection conn = null;

		try {
			conn = DriverManager.getConnection(config.mysqlUrl, config.mysqlUser, config.mysqlPassword);
			return true;
		} catch (Exception e) {
			LOGGER.info(TextUtils.merge("Is operating check for ''{0}:{1,number,#}''",
					config.mysqlHost, parsePort(config.mysqlPort)) + ": " + e.getClass().getName() + ": " + e.getMessage());
			return false;
		} finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.debug("Cannot close MySQL connection", e);
                }
            }
        }
	}

    /**
     * Potentially can lead to longer scenario startup
     * @return
     */
    @Override
    public boolean isRunning() {
        return isOperating();
    }

	@Override
	public boolean hasLogfile() {
		return true;
	}

	@Override
	public Technology getTechnology() {
		return Technology.MYSQL;
	}

	@Override
	protected String getExecutable(ProcedureMapping mapping) {

		return MysqlUtils.getExecutableDependingOnOs();
	}

	@Override
	protected String getWorkingDir() {
		return null;
	}

	@Override
	protected DtAgentConfig getAgentConfig() {
		return null;
	}

	/**
	 * This class configures the MySQL My.ini file at runtime. The My.ini
	 * file can be found in the /.dynaTrace/easyTravel x.x.x/config folder.
	 *
	 * @author cwat-cchen
	 *
	 */
	private static class MySQLConfSetup {
		private static PrintWriter writer;

		private static void write() throws IOException {
			initializeFileWriter();
			writeDatadirandErrorlogEntries();
			closeFileWriter();
		}

		private static void initializeFileWriter() throws IOException {
			writer = new PrintWriter(new FileWriter(MysqlUtils.MySQL_INI, true));
		}

		private static void writeDatadirandErrorlogEntries() {
			writer.println("\ndatadir=" + MysqlUtils.MySQL_DATA_DIR);
			writer.println("log-error=" + MysqlUtils.MySQL_ERROR_LOG);
			writer.println("socket=" + MysqlUtils.MySQL_SOCKET);
		}

		private static void closeFileWriter() {
			writer.close();
			LOGGER.info("New MySQL conf file was created successfully in the folder " + MysqlUtils.EASYTRAVEL_CONFIG_PATH);
		}
	}

	@Override
	public String getLogfile() {
		return MysqlUtils.MySQL_ERROR_LOG;
	}

	@Override
	public boolean isInstrumentationSupported() {
		return false;
	}
}
