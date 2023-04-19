package com.dynatrace.easytravel.launcher.procedures;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildException;
import org.eclipse.swt.widgets.Display;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.ant.AntController;
import com.dynatrace.easytravel.launcher.ant.InProcessAntController;
import com.dynatrace.easytravel.launcher.ant.OutOfProcessAntController;
import com.dynatrace.easytravel.launcher.engine.AbstractProcedure;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.engine.ProcedureFactory;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.engine.StopListener;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.engine.ThreadEngine;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.process.JavaProcess;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureSetting;
import com.dynatrace.easytravel.logging.BasicLoggerConfig;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.util.TextUtils;


public class AntProcedure extends AbstractProcedure {

    private static final Logger LOGGER = Logger.getLogger(AntProcedure.class.getName());

    public static final String TITLE = "title";
    public static final String FILE = "file";
    public static final String TARGET = "target";
    public static final String RECURRENCE = "recurrence";
    public static final String INSTANCES = "instances";
    public static final String FORK = "fork";
    public static final String RECURRENCE_INTERVAL_SEC = "recurrenceIntervalSeconds";
    public static final String START_INTERVAL_SEC = "startIntervalSeconds";
    public static final String INSTRUMENTATION = "instrumentation";
    public static final String SETTING_VALUE_ON = Constants.Misc.SETTING_VALUE_ON;
    public static final String PROPERTY = "property";
    public static final String WEBDRIVER_BROWSER = "webdriver.browser.default";
    public static final String TEST_REPORT_DIR = "test.report";
    public static final String PROPERTY_DTAGENT = "dtagent";
    public static final String PROPERTY_BUILD_NUMBER_FILE = "test.build.number";
    public static final String DTTESTRUNNAME = "dtTestRunName";

    private static final String PROPERTY_DT_SERVER_NAME = "dynatrace.server.name";
    private static final String PROPERTY_DT_SERVER_PORT = "dynatrace.server.port";
    private static final String PROPERTY_DT_SERVER_USER = "dynatrace.server.user";
    private static final String PROPERTY_DT_SERVER_PASS = "dynatrace.server.pass";
    private static final String PROPERTY_DT_SERVER_COLLECTOR = "dynatrace.server.collector";

    private static final String PROPERTY_BUSINESS_BACKEND_HOST = "businessBackendHost";
    private static final String PROPERTY_CUSTOMER_FRONTEND_HOST = "customerFrontendHost";
    private static final String PROPERTY_B2B_FRONTEND_HOST = "b2bFrontendHost";
    private static final String PROPERTY_PAYMENT_BACKEND_HOST = "paymentBackendHost";

    public static final String PROPERTY_SESSION_RECORDING_SKIP = "test.session.recording.skip";

    private static final int DEFAULT_INSTANCES = 1;
    private static final int DEFAULT_START_INTERVAL_SEC = 5;
    private static final int DEFAULT_RECURRENCE = 1;
    private static final int DEFAULT_RECURRENCE_INTERVAL_SEC = 60;

    private final AntController controller;
    private final boolean isInstrumentationTurnedOn;
    private final File antFile;

    public AntProcedure(ProcedureMapping mapping) throws IllegalArgumentException {
        super(mapping);

        String antFileString = mapping.getSettingValue(FILE);
        if(antFileString == null) {
        	throw new IllegalStateException("Could not read setting '" + FILE + "' for ant procedure '" + mapping.getSettingValue(TITLE) + "'");
        }

        File antFile = new File(antFileString);
        if (!antFile.isAbsolute()) {
            antFile = new File(Directories.getInstallDir(), antFileString);
        }
        if (!antFile.exists()) {
            throw new IllegalArgumentException(TextUtils.merge("The specified ant file cannot be found: ''{0}''", antFile.getAbsolutePath()));
        }
        if (!antFile.isFile()) {
            throw new IllegalArgumentException(TextUtils.merge("The ant file argument is not a file: ''{0}''", antFile.getAbsolutePath()));
        }
        this.antFile = antFile;

        String target = mapping.getSettingValue(TARGET);
        if (target == null || target.isEmpty()) {
            throw new IllegalArgumentException(TextUtils.merge("Illegal ant target."));
        }

        // set title setting and set procedure name
        this.name = getTitle(mapping, target);

        // read if instrumentation is turned on or off
        isInstrumentationTurnedOn = SETTING_VALUE_ON.equalsIgnoreCase(mapping.getSettingValue(INSTRUMENTATION));

        boolean fork = parseBoolean(mapping.getSettingValue(FORK), Boolean.FALSE);

        if (fork) {
            controller = new OutOfProcessAntController(antFile, target);
        } else {
            controller = new InProcessAntController(antFile, target);
        }

        controller.setInstances(parseInt(mapping.getSettingValue(INSTANCES), DEFAULT_INSTANCES));
        controller.setStartIntervalMs(parseInt(mapping.getSettingValue(START_INTERVAL_SEC), DEFAULT_START_INTERVAL_SEC) * 1000L);

        RecurrenceData recurrenceData = new RecurrenceData(mapping.getSettingValue(RECURRENCE), mapping.getSettingValue(RECURRENCE_INTERVAL_SEC));
        controller.setRecurrence(recurrenceData.recurrence);
        controller.setRecurrenceIntervalMs(recurrenceData.intervalSec * 1000L);

        for (ProcedureSetting setting : mapping.getSettings(PROPERTY)) {
            controller.addProperty(setting.getName(), setting.getValue());
        }

        controller.addProperty(PROPERTY_CUSTOMER_FRONTEND_HOST, ProcedureFactory.getHostOrLocal(Constants.Procedures.CUSTOMER_FRONTEND_ID));
        controller.addProperty(PROPERTY_BUSINESS_BACKEND_HOST, ProcedureFactory.getHostOrLocal(Constants.Procedures.BUSINESS_BACKEND_ID));
        controller.addProperty(PROPERTY_B2B_FRONTEND_HOST, ProcedureFactory.getHostOrLocal(Constants.Procedures.B2B_FRONTEND_ID));
        controller.addProperty(PROPERTY_PAYMENT_BACKEND_HOST, ProcedureFactory.getHostOrLocal(Constants.Procedures.PAYMENT_BACKEND_ID));

        final EasyTravelConfig config = EasyTravelConfig.read();
        controller.addProperty(PROPERTY_DT_SERVER_NAME, config.dtServer);
        controller.addProperty(PROPERTY_DT_SERVER_PORT, config.dtServerWebPort);
        controller.addProperty(PROPERTY_DT_SERVER_USER, config.dtServerUsername);
        controller.addProperty(PROPERTY_DT_SERVER_PASS, config.dtServerPassword);


        if (null != mapping.getSettingValue(DTTESTRUNNAME)) {
        	controller.addProperty(DTTESTRUNNAME, mapping.getSettingValue(DTTESTRUNNAME));
        }

        int dtServerPort = DtAgentConfig.parseServerPort(config.dtServerPort);
        String collector = config.dtServer;
        if (dtServerPort > 0) {
            collector = config.dtServer + BaseConstants.COLON + config.dtServerPort;
        }
        controller.addProperty(PROPERTY_DT_SERVER_COLLECTOR, collector);
    }

	public static String getTitle(ProcedureMapping mapping, String target) {
		String title = mapping.getSettingValue(TITLE);
        if (title == null) {
            title = TextUtils.merge(MessageConstants.MODULE_PATTERN_ANT, target);
        }
		return title;
	}

    @Override
    public Feedback run() {
        // set possibly dynamic assigned ports
        // this expects the ant procedure to be the last one started - to have the other procedures already in the list.
        if(LaunchEngine.getRunningBatch() != null) {
	        int nWebProc = 0;
	        for (StatefulProcedure proc : LaunchEngine.getRunningBatch().getProcedures()) {
	        	if (proc.isWebProcedure()) {
	        		controller.addProperty(proc.getPortPropertyName(), String.valueOf(proc.getPort()));
	        		nWebProc++;
	        		if (LOGGER.isLoggable(Level.INFO)) {
						LOGGER.log(Level.INFO, "Assigning port value " + proc.getPort() + " to '" + proc.getPortPropertyName() + "'");
					}
	        	}
	        }
	        if (nWebProc == 0) {
	    		LOGGER.log(Level.WARNING, "Expected to find web procedures to assign port properties, but had none. Ant procedure should be the very last procedure listed.");
	        }
        }

        if (isInstrumentationTurnedOn) {
            try {
                controller.addProperty(PROPERTY_DTAGENT, getDtAgentString());
            } catch (ConfigurationException e) {
                LOGGER.log(Level.WARNING, TextUtils.merge("Unable to set dynaTrace agent to Ant procedure (file: {0}).", antFile.getName()), e);
            }
        }

        try {
            controller.start();
            return Feedback.Neutral;
        } catch (BuildException be) {
            return Feedback.Failure;
        }
    }

    @Override
    public StopMode getStopMode() {
        return StopMode.PARALLEL;
    }

	@Override
	public boolean isStoppable() {
		return true;
	}

    @Override
    public Feedback stop() {
    	LOGGER.warning("Stopping procedures. Stopping Ant");
        controller.stopSoft();

        final EasyTravelConfig config = EasyTravelConfig.read();
        boolean isStillRunning = waitUntilNotRunning(config.softShutdownTimeoutMs, config.processRunningCheckInterval);
        if (isStillRunning) {
            controller.stopHard();
        }

        LOGGER.warning("Stopping procedures. Ant stopped");
        return Feedback.Success;
    }

    @Override
    public boolean isRunning() {
        return controller.isProcessing();
    }

    @Override
    public boolean isOperatingCheckSupported() {
        return true;
    }

    @Override
    public boolean isOperating() {
        return controller.isProcessing();
    }

    @Override
    public void addStopListener(StopListener stopListener) {
        controller.addStopListener(stopListener);
    }

    @Override
    public void removeStopListener(StopListener stopListener) {
        controller.removeStopListener(stopListener);
    }

    @Override
    public void clearStopListeners() {
        controller.clearStopListeners();
    }

    @Override
	public String getDetails() {
		return "Antfile: " + antFile + "\nTarget: " + getMapping().getSettingValue(TARGET);
	}

    @Override
    public void notifyProcedureStateChanged(final StatefulProcedure subject, State oldState, State newState) {
        super.notifyProcedureStateChanged(subject, oldState, newState);

        if (newState == State.STOPPED && controller.supportRestart() && controller.isRestartRequired()) {
            LOGGER.info("Trying to restart Ant procedure...");

            Thread async = ThreadEngine.createBackgroundThread(subject.getName() + " Restarter", new Runnable() {
                @Override
                public void run() {
                    boolean wasRestarted = subject.run().isOk();
                    if (!wasRestarted) {
                        LOGGER.warning(TextUtils.merge("Failed to restart procedure ''{0}''", subject.getName()));
                        return;
                    }

                    if (!subject.isOperating()) {
                        LOGGER.warning(TextUtils.merge("Procedure ''{0}'' was restarted but is not operating", subject.getName()));
                        return;
                    }

                    // set OPERATING state (thereupon the procedure is visualized as OPERATING in UI)
                    subject.setState(State.OPERATING);
                    LOGGER.info(TextUtils.merge("The procedure ''{0}'' was successfully restarted", subject.getName()));
                }
            }, Display.getCurrent());
            async.setDaemon(true);
            async.start();
        }
    }

	private String getDtAgentString() throws ConfigurationException {
        return JavaProcess.createAgentString(getDtAgentConfig());
    }

    private DtAgentConfig getDtAgentConfig() {
        final EasyTravelConfig config = EasyTravelConfig.read();
        String agentName = config.antSystemProfilePrefix + BaseConstants.UNDERSCORE + antFile.getName();
        DtAgentConfig agentConfig = new DtAgentConfig(agentName, config.antAgent, config.antAgentOptions, config.antEnvArgs);
        return agentConfig;
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }

        try {
            return Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static class RecurrenceData {

        public final int recurrence;
        public final int intervalSec;

        public RecurrenceData(String recurrenceString, String intervalSecString) {
            int recurrenceTemp = DEFAULT_RECURRENCE;
            int intervalSecTemp = DEFAULT_RECURRENCE_INTERVAL_SEC;

            if (recurrenceString != null) {
                try {
                    recurrenceTemp = Integer.parseInt(recurrenceString);

                    if (intervalSecString != null) {
                        try {
                            intervalSecTemp = Integer.parseInt(intervalSecString);
                        } catch (NumberFormatException e) {
                            intervalSecTemp = DEFAULT_RECURRENCE_INTERVAL_SEC;
                        }
                    }
                } catch (NumberFormatException e) {
                    recurrenceTemp = DEFAULT_RECURRENCE;
                }
            }

            this.recurrence = recurrenceTemp;
            this.intervalSec = intervalSecTemp;
        }
    }

	@Override
	public String getLogfile() {
		return BasicLoggerConfig.getLogFilePath(BaseConstants.LoggerNames.ANT);
	}

	@Override
	public boolean hasLogfile() {
	    return true;
	}

    @Override
    public Technology getTechnology() {
        return Technology.JAVA;
    }

	@Override
	public boolean isInstrumentationSupported() {
    	// no instrumentation support on APM
		if(DtVersionDetector.isAPM()) {
			return false;
		}

    	return super.isInstrumentationSupported();
	}

    @Override
    public boolean agentFound() {
        return agentFound(getDtAgentConfig());
    }

	public boolean setContinuously(boolean continuously) {
	    return controller.setContinuously(continuously);
	}

}

