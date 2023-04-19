package com.dynatrace.easytravel.launcher.procedures;

import static com.dynatrace.easytravel.launcher.misc.Constants.Misc.SETTING_COPY_DERBY_DATA_FROM;

import java.io.File;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.derby.drda.NetworkServerControl;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.AbstractProcedure;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopListener;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureSetting;
import com.dynatrace.easytravel.util.NetstatUtil;
import com.dynatrace.easytravel.util.TextUtils;


/**
 * Start and stop the Derby Database Management System.
 *
 * @author martin.wurzinger
 */
public class DbmsProcedure extends AbstractProcedure {
    private static final Logger LOGGER = Logger.getLogger(DbmsProcedure.class.getName());

    private NetworkServerControl dbServer = null;
    private PrintWriter consoleWriter = null;


    public DbmsProcedure(ProcedureMapping mapping) {
        super(mapping);
    }

    /**
     * @param consoleWriter The {@link PrintWriter} to which server console will be output. Console output will be disabled if <code>null</code> is passed in.
     * @author martin.wurzinger
     */
    public void setConsoleWriter(PrintWriter consoleWriter) {
        this.consoleWriter = consoleWriter;
    }

    private static int parsePort(int port) {
        if (port >= 0) {
            return port;
        }
        return NetworkServerControl.DEFAULT_PORTNUMBER;
    }

    /*private static InetAddress parseAdress(InetAddress address) {
        if (address == null) {
            return LocalUriProvider.getLoopbackAdapter();
        }
        return address;
    }*/

    /**
     * Initialize the Derby in order to make running check work correctly.
     * This does not start the database.
     */
    public boolean init() {
        final EasyTravelConfig config = EasyTravelConfig.read();

        int dbServerPort = parsePort(config.internalDatabasePort);
        try {
        	if (dbServer == null) {
	            LOGGER.info("Initialize internal database management system on host: '" + config.internalDatabaseHost + "' and port: '" + dbServerPort + "'");
	            System.setProperty(BaseConstants.SystemProperties.DERBY_PORT_NUMBER, Integer.toString(dbServerPort));
	            System.setProperty(BaseConstants.SystemProperties.DERBY_HOST, config.internalDatabaseHost);
	            System.setProperty(BaseConstants.SystemProperties.DERBY_LOGGER_METHOD, DerbyLogger.getLogMethod());
	            System.setProperty(BaseConstants.SystemProperties.DERBY_SYSTEM_HOME, Directories.getExistingDatabaseDir().getAbsolutePath());

	            // apply all settings provided as part of the Scenario
	            for(ProcedureSetting setting : getMapping().getSettings("derby")) {
	            	LOGGER.info("Setting config for Derby Database from Scenario: " + setting);
	            	System.setProperty(setting.getName(), setting.getValue());
	            }

	            dbServer = new NetworkServerControl();
        	}
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, TextUtils.merge("Unable to create database server controller for ''{0}:{1,number,#}'' (<address>:<port>).", config.internalDatabaseHost, dbServerPort), e);
    		return false;
        }
    }

    /**
     * Start the Derby Database.
     * A call to init() prior to run() is not necessary, but does no harm.
     */
    @Override
    public Feedback run() {
        final EasyTravelConfig config = EasyTravelConfig.read();

        int dbServerPort = parsePort(config.internalDatabasePort);
        LOGGER.info("Starting internal database management system on host: '" + config.internalDatabaseHost + "' and port: '" + dbServerPort + "'");

		String derbyDataRepo = getMapping().getSettingValue(SETTING_COPY_DERBY_DATA_FROM);
		if (derbyDataRepo != null) {
			new CopyDatabaseContent(new File(Directories.getDatabaseDir(), "easyTravelBusiness")).copyDbDataFrom(derbyDataRepo);
		}

        /* This does not work on a Linux VirtualBox, somehow we do always get the port as taken!
        if(SocketUtils.isPortAvailable(dbServerPort, null)) {
        	LOGGER.log(Level.WARNING, TextUtils.merge("Unable to create database server controller for ''{0}:{1,number,#}'' (<address>:<port>), port is already taken.", dbServerAddress, dbServerPort));
        	return;
        }*/

        if (!init()) {
        	return Feedback.Failure;
        }

        try {
            dbServer.start(consoleWriter);

            if (!waitUntilRunning()) {
            	// give more information
    			NetstatUtil netstatUtil = new NetstatUtil(Runtime.getRuntime());
    			String process = netstatUtil.findProcessForPort(dbServerPort);
                LOGGER.log(Level.SEVERE, "Waiting for database management system on host: '" + config.internalDatabaseHost + "' and port: '" + dbServerPort + "' timed out, had the following process on the port: " + process);
                return Feedback.Failure;
            }

            return Feedback.Success;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to start embedded database (Derby) on host: '" + config.internalDatabaseHost + "' and port: '" + dbServerPort + "' timed out.", e);
            return Feedback.Failure;
        }
    }





    @Override
    public Feedback stop() {
    	LOGGER.warning("Stopping procedures. Stopping Dbms");
        if (dbServer == null) {
            LOGGER.info("Database management system actually not running, so it cannot be stopped.");
            return Feedback.Neutral;
        }

        try {
            dbServer.shutdown();
            dbServer = null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to stop database management system.", e);
            return Feedback.Failure;
        }

        // close log writer
        DerbyLogger.closeWriter();

        // remove settings provided as part of the Scenario
        for(ProcedureSetting setting : getMapping().getSettings("derby")) {
        	System.clearProperty(setting.getName());
        }

        LOGGER.warning("Stopping procedures. Dbms - neutral...");
        return Feedback.Neutral;
    }

    /**
     * Check if the database is running.
     * @return <code>true</code> if the database is running or <code>false</code> otherwise
     * @author martin.wurzinger
     */
    @Override
    public boolean isRunning() {
        return isOperating();
    }

	@Override
	public StopMode getStopMode() {
		return StopMode.SEQUENTIAL;
	}

    @Override
    public boolean isStoppable() {
        return true;
    }

    @Override
    public boolean isOperatingCheckSupported() {
        return true;
    }

    /**
     * Check if the database is running.
     *
     * @return <code>true</code> if the database is running or <code>false</code> otherwise
     * @author martin.wurzinger
     */
    @Override
    public boolean isOperating() {
        if (dbServer == null) {
            return false;
        }

        try {
            dbServer.ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        final EasyTravelConfig config = EasyTravelConfig.read();
        return config.internalDatabaseEnabled && super.isEnabled();
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

	@Override
	public String getDetails() {
        final EasyTravelConfig config = EasyTravelConfig.read();
		return "Database running at: " + config.internalDatabaseHost + ":" + config.internalDatabasePort;
	}

	@Override
	public String getLogfile() {
        return null;
	}

	@Override
	public boolean hasLogfile() {
	    return false;
	}

	@Override
	public Technology getTechnology() {
	    return null;
	}

    @Override
    public boolean agentFound() {
        return false;
    }
}
