package com.dynatrace.easytravel.weblauncher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.Version;
import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.engine.Procedure;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.misc.RESTConstants;
import com.dynatrace.easytravel.net.ZipUtils;

import ch.qos.logback.classic.Logger;

/**
 * This REST servlet provides an interface to remotely control the Launch Engine
 * in order to start/stop scenarios and query the state and download logs.
 *
 * Syntax: /scenario/<command>/<scenariogroup>/<scenario>
 *
 * <scenariogroup> and <scenario> are only needed for start command.
 *
 * <command>: one of
 * - start             start the denoted <scenario> of <scenariogroup>
 * - stop              stop the currently running scenario
 * - shutdown          stop the scenario and terminate the Launcher
 * - state             return the current scenario state, see {@link com.dynatrace.easytravel.launcher.engine.State}
 * - states            return the current state of all procedures see {@link com.dynatrace.easytravel.launcher.engine.State}
 * - which             return which scenario is running, or "NONE"
 * - log               collect log files log dir to in a zip file and provide it for download.
 *
 * The result of the operation is either "OK" if starting / stopping was successfully requested,
 * or the string representation of the LaunchEngine's state, or an error message,
 * all in plain text.
 *
 * Usage Examples:
 * - http://localhost:8094/scenario/start/Production/Standard
 *   Start the Production/Standard scenario
 * - http://localhost:8094/scenario/log
 *   Get the logs (so far).
 * - http://localhost:8094/scenario/stop
 *   Stop it again.
 *
 * @author philipp.grasboeck
 */
public class ScenarioServlet extends BaseServlet {

	private static final String ANSWER_NONE = "NONE";

	private static final String LF = "\n";

	private static final String SEP = ";";

	private static final long serialVersionUID = 4866001778373177886L;

	private static final String ACTION_START = RESTConstants.START;
	private static final String ACTION_STOP = RESTConstants.STOP;
	private static final String ACTION_STATE = "state";
	private static final String ACTION_STATES = "states";
	private static final String ACTION_WHICH = "which";
	private static final String ACTION_LOG = RESTConstants.LOG;
	private static final String ACTION_SHUTDOWN = RESTConstants.SHUTDOWN;
	private static final String ACTION_VERSION = RESTConstants.VERSION;
	private static final String ANSWER_OK = "OK";
	private static final String LOG_ZIP_FILE = "logs.zip";

	private static final Logger log = LoggerFactory.make();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected String doService(HttpServletRequest request, HttpServletResponse response) throws ParamException, IOException {
		String[] params = getMandatoryPathParams(request);
		String action = params[0];

		if (action.equals(ACTION_START)) {
			if (params.length < 3) {
				throw new ParamException("Missing parameters: Use /scenario/start/<scenariogroup>/<scenario>");
			}
			String scenarioGroupTitle = params[1];
			String scenarioTitle = params[2];
			startScenario(scenarioGroupTitle, scenarioTitle);
			return ANSWER_OK;
		} else if (action.equals(ACTION_STOP)) {
			if (LaunchEngine.getRunningBatch() == null) {
				throw new ParamException("Cannot stop, current state: %s", getStateString());
			}
			log.info("Stoppping scenario");
			LaunchEngine.stopAsync();
			return ANSWER_OK;
		} else if (action.equals(ACTION_STATE)) {
			String stateString = getStateString();
			if (log.isDebugEnabled()) log.debug(String.format("Querying state: %s",stateString));
			return stateString;
		} else if (action.equals(ACTION_STATES)) {
			StringBuilder buf = new StringBuilder();
			buf.append("Name").append(SEP);
			buf.append("State").append(SEP);
			buf.append("Agent").append(SEP).append(LF);
			if (LaunchEngine.getRunningBatch() != null) {
				for (Procedure procedure : LaunchEngine.getRunningBatch().getProcedures()) {
					buf.append(procedure.getName()).append(SEP);
					if (procedure instanceof StatefulProcedure) {
						buf.append(((StatefulProcedure) procedure).getState()).append(SEP);
					} else {
						buf.append("no-state").append(SEP);
					}
					buf.append(procedure.agentFound() ? "agent-ok" : "no-agent").append(SEP).append(LF);
				}
			}
			return buf.toString();
		} else if (action.equals(ACTION_WHICH)) {
			if (LaunchEngine.getRunningBatch() != null) {
				return LaunchEngine.getRunningBatch().getScenario().getGroup() + "/" + LaunchEngine.getRunningBatch().getScenario().getTitle();
			} else {
				return ANSWER_NONE;
			}
		} else if (action.equals(ACTION_LOG)) {
			// in remote scenario, this requets log files via REST - see RemoteProcedure
			if (LaunchEngine.getRunningBatch() != null) {
				for (StatefulProcedure procedure : LaunchEngine.getRunningBatch().getProcedures()) {
					if (log.isDebugEnabled()) log.debug(String.format("Requesting log for: %s", procedure.getName()));
					procedure.getLogfile(); // will put the log file in the log directory
				}
			}

			try {
				EasyTravelConfig.read().store(new File(Directories.getLogDir(), "easyTravelConfig.log"));
				FileOutputStream out = new FileOutputStream(new File(Directories.getLogDir(), "SystemProperties.log"));
				try {
					System.getProperties().store(out, "Current System Properties");
				} finally {
					out.close();
				}
			} catch (IOException e) {
				log.warn("Had IOException while writing config properties for log-zip", e);
			}

			// create the log zip file and provide it for download
			File zipFile = new File(Directories.getTempDir(), LOG_ZIP_FILE);
			log.info(String.format("Sending file download: %s", zipFile));
			ZipUtils.compressDir(zipFile, Directories.getLogDir(), ".*\\.lck", null, false);
			sendFileDownload(response, zipFile);
			return null;
		} else if (action.equals(ACTION_VERSION)) {
			String version = Version.read().toString();
			if (log.isDebugEnabled()) log.debug(String.format("Querying version: %s",version));
			return version;
		} else if (action.equals(ACTION_SHUTDOWN)) {
			log.info("Shutting down");
			System.exit(0);
			return null;
		} else {
			throw new ParamException("Invalid action: %s", action);
		}
	}

	protected static void startScenario(String scenarioGroupTitle, String scenarioTitle) throws ParamException {
		Scenario scenario = LaunchEngine.findScenario(scenarioGroupTitle, scenarioTitle);
		if (scenario == null) {
			throw new ParamException("Scenario not found: %s/%s", scenarioGroupTitle, scenarioTitle);
		}
		if (LaunchEngine.getRunningBatch() != null) {
			throw new ParamException("Cannot start, current state: %s", getStateString());
		}
		log.info(String.format("Starting scenario: %s/%s", scenarioGroupTitle, scenarioTitle));
		LaunchEngine.getNewInstance().runAsync(scenario);
	}

	private static String getStateString() {
		State state = LaunchEngine.getRunningBatch() != null ? LaunchEngine.getRunningBatch().getState() : State.getDefault();
		return String.valueOf(state);
	}
}
