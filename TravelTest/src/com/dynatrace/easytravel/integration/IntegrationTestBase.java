/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: IntegrationTestBase.java
 * @date: 14.09.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.integration;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.*;
import org.apache.commons.exec.environment.DefaultProcessingEnvironment;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.ThirdPartyContentProxySelector;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.ProcedureFactory;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.httpd.ApacheHttpdUtils;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.*;
import com.dynatrace.easytravel.launcher.process.AbstractProcess;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.builder.ProcedureBuilder;
import com.dynatrace.easytravel.launcher.sync.Predicate;
import com.dynatrace.easytravel.launcher.sync.PredicateMatcher;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.plugin.RemotePluginController;
import com.dynatrace.easytravel.util.LocalUriProvider;
import com.dynatrace.easytravel.utils.TestEnvironment;
import com.dynatrace.easytravel.utils.TestHelpers;
import com.google.common.base.Joiner;

import ch.qos.logback.classic.Level;


/**
 * Base class to allow to build integration tests which verify features of a running instance of easyTravel
 *
 * @author dominik.stadler
 */
public class IntegrationTestBase {
	private static final Logger log = LoggerFactory.make();

	private static final int MAX_PROCEDURES = 6;

	private static final String DIST_DIR = "../Distribution/dist";

	private static final File FILE_DIST_DIR = new File(DIST_DIR);

	static {
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, new File(DIST_DIR).getAbsolutePath());
	}

	private static final File FILE_CMDLAUNCHER = new File(FILE_DIST_DIR, SystemUtils.IS_OS_WINDOWS ? "runEasyTravelNoGUI.cmd" : "runEasyTravelNoGUI.sh");
	private static final File FILE_WEBLAUNCHER = new File(FILE_DIST_DIR, SystemUtils.IS_OS_WINDOWS ? "weblauncher/weblauncher.cmd" : "weblauncher/weblauncher.sh");

	private static final String DUMMY_PAYMENT_SERVICE = "DummyPaymentService";
	protected static final String DOT_NET_PAYMENT_SERVICE = "DotNetPaymentService";

	private static boolean started = false;
	private static DefaultExecuteResultHandler resultHandler;
	private static ExecuteWatchdog watchdog;

	private static List<String> expectedLogs = Collections.emptyList();

	private static InetAddress addr;
	static {
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new IllegalStateException(e);
		}
	}

	public static void cleanupAndStart(boolean webStart, String group, String scenario) throws IOException, InterruptedException {
		List<String> list = Collections.emptyList();
		cleanupAndStart(webStart, group, scenario, list);
	}

	public static void cleanupAndStart(boolean webStart, String group, String scenario, Collection<String> javaArgs)
			throws IOException, InterruptedException {
		cleanupAndStart(webStart, group, scenario, Collections.<String> emptyList(), javaArgs);
	}

	public static void cleanupAndStart(boolean webStart, String group, String scenario, List<String> expectedLogs) throws IOException, InterruptedException {
		cleanupAndStart(webStart, group, scenario, expectedLogs, Collections.<String> emptyList());
	}

	public static void cleanupAndStart(boolean webStart, String group, String scenario, List<String> expectedLogs,
			Collection<String> javaArgs) throws IOException, InterruptedException {
		LoggerFactory.initLogging();

		// try to stop things that are still running now
		cleanup();

		IntegrationTestBase.expectedLogs = new ArrayList<String>(expectedLogs);

		// ensure that easyTravel is running
		startup(webStart, group, scenario, javaArgs);

		// disable plugin which causes very long delays and/or breaking HtmlUnit tests
		RemotePluginController controller = new RemotePluginController();
		String result = controller.sendEnabled(Constants.Plugin.ThirdPartyContent, false, null);
		assertNotNull("Could not disable Third Party Content plugin, look at logfiles for details", result);

		// need to wait a few seconds to ensure that the changed plugin is active in all procedures
		Thread.sleep(10000);

		log.info("Started " + (webStart ? "web" : "cmd") + "-launcher with scenario " + group + "/" + scenario + ", now running tests");
	}


	private synchronized static void startup(boolean webLauncher, final String autostartGroup, final String autostartScenario,
			Collection<String> javaArgs) throws ExecuteException, IOException {
		if(started) {
			return;
		}


		EasyTravelConfig config = EasyTravelConfig.read();

		checkAllPorts(config);

		if(webLauncher) {
			checkSystem(FILE_WEBLAUNCHER);

			log.info("Starting launcher script " + FILE_WEBLAUNCHER + " in directory " + FILE_WEBLAUNCHER.getParentFile() + " with autostart " + autostartGroup + "/" + autostartScenario);
			final CommandLine cmdLine = new CommandLine(FILE_WEBLAUNCHER.getAbsolutePath());

			if(autostartGroup != null) {
				cmdLine.addArgument(autostartGroup);
			}
			if(autostartScenario != null) {
				cmdLine.addArgument(autostartScenario);
			}

			// pass environment variable to avoid opening the webpage
			String frontendPublicUrlArg = "-Dconfig.frontendPublicUrl=http://localhost:" + config.frontendPortRangeStart;
			Map<String, String> environment = Collections.singletonMap("LAUNCHER_ARGS",
					Joiner.on(" ").skipNulls().join(frontendPublicUrlArg, null, (Object[])javaArgs.toArray(new String[0])));

			start(cmdLine, FILE_WEBLAUNCHER.getParentFile(), environment);

			// wait for weblauncher-Servlet interface to become active
			final int timeout = config.syncProcessTimeoutMs;
			PredicateMatcher<Object> matcher = new PredicateMatcher<Object>(null, timeout, config.processOperatingCheckIntervalMs);

			String url = "http://localhost:" + config.weblauncherPort + "/scenario/state";
			URLPredicate launcherStartingPredicate = new URLPredicate(url, true);
	        assertTrue("Could not access launcher at " + url + " in the timeout of " + timeout + " ms",
	        		matcher.waitForMatch(launcherStartingPredicate));

			// now trigger the autostart
			//startWebLauncheScenario(autostartGroup, autostartScenario, config);

			// for WebLauncher, we also check if the State-Servlet reports "OPERATING" for the current Batch
			waitForWebLauncherScenario(State.OPERATING.toString());
		} else {
			checkSystem(FILE_CMDLAUNCHER);

			log.info("Starting launcher script " + FILE_CMDLAUNCHER + " in directory " + FILE_DIST_DIR + " with autostart " + autostartGroup + "/" + autostartScenario);
			final CommandLine cmdLine = new CommandLine(FILE_CMDLAUNCHER.getAbsolutePath());

			if(autostartGroup != null) {
				cmdLine.addArgument("-startgroup");
				cmdLine.addArgument(autostartGroup);
			}
			if(autostartScenario != null) {
				cmdLine.addArgument("-startscenario");
				cmdLine.addArgument(autostartScenario);
			}

			Map<String, String> environment = Collections.singletonMap("LAUNCHER_ARGS",
					Joiner.on(" ").join(javaArgs));
			start(cmdLine, FILE_DIST_DIR, environment);

			waitForLauncherStartupDone(config, webLauncher);
		}

    	// .NET Frontend is only available on Windows when not disabled in scenario configuration
    	// disable PaymentService and enable DummyPaymentService instead in this case
    	if(!TestHelpers.isDotNetEnabled()) {
			RemotePluginController controller = new RemotePluginController();
			controller.sendEnabled(DOT_NET_PAYMENT_SERVICE, false, null);
			controller.sendEnabled(DUMMY_PAYMENT_SERVICE, true, null);

			// verify that we now have these plugins
			List<String> plugins = Arrays.asList(controller.requestEnabledPluginNames());
			log.info("Having plugins after adjusting PaymentService: " + plugins);
			assertTrue("List: " + plugins, plugins.contains(DUMMY_PAYMENT_SERVICE));
			assertFalse("List: " + plugins, plugins.contains(DOT_NET_PAYMENT_SERVICE));
		}

    	// apply proxy settings if there are any
    	ThirdPartyContentProxySelector.applyProxy();

    	// ensure that Business Backend is still running now, we had cases where it failed to start, e.g. Cassandra, DB trouble, ...
    	verifyProcedureState(Constants.Procedures.BUSINESS_BACKEND_ID, State.OPERATING);

		started = true;
	}

	private static final int TIMEOUT = 60000;

	public static void verifyProcedureState(String procId, State state) throws IOException {
		EasyTravelConfig config = EasyTravelConfig.read();
		String states = UrlUtils.retrieveData("http://localhost:" + config.launcherHttpPort + "/statusAll", TIMEOUT);
		log.info("Looking for " + procId + " in state " + state + ", had states: \n" + states);
		assertNotNull(states);

		TestHelpers.assertContainsMsg("Expected to find procedure " + procId + " with state: " + state,
				states,
				ProcedureFactory.getNameOfProcedure(new DefaultProcedureMapping(procId)) + ": " + state.toString() );
	}

	public static void verifyPluginState(String plugin, boolean enabled) throws IOException {
		log.info("Verifying that plugin '" + plugin + "' is " + (enabled ? "enabled" : "disabled"));

		RemotePluginController controller = new RemotePluginController();
		String[] plugins = controller.requestAllPluginNames();
		assertTrue("Expected to find plugin: " + plugin + ", but had plugins: " + plugins,
				ArrayUtils.contains(plugins, plugin));

		String[] enabledPlugins = controller.requestEnabledPluginNames();
		assertEquals("Expected to have plugin " + (enabled ? "enabled" : "disabled") + ": " + plugin + ", but had enabled plugins: " + Arrays.toString(enabledPlugins),
				enabled, ArrayUtils.contains(enabledPlugins, plugin));
	}

	public static void startWebLauncheScenario(String autostartGroup, String autostartScenario, EasyTravelConfig config)
			throws IOException {
		assertEquals("OK",
				UrlUtils.retrieveData("http://localhost:" + config.weblauncherPort + "/scenario/start/" + autostartGroup.replace(" ", "%20") + "/" + autostartScenario.replace(" ", "%20"), 10000));
	}

	private static void checkSystem(File startFile) {
		log.info("Checking system");
		assertTrue("Dir exists: " + FILE_DIST_DIR,
				FILE_DIST_DIR.exists());
		assertTrue("Is dir: " + FILE_DIST_DIR,
				FILE_DIST_DIR.isDirectory());
		assertTrue("Launcher script does not exist: " + startFile.getAbsolutePath(),
				startFile.exists());
		assertTrue("Launcher script is not readable: " + startFile,
				startFile.canRead());
		assertTrue("Launcher script is not executable: " + startFile,
				startFile.canExecute());
	}


	/**
	 * Ensure that all procedures and the launcher are stopped.
	 *
	 * @throws InterruptedException
	 * @throws IOException
	 * @author dominik.stadler
	 */
	public static void stop() throws InterruptedException, IOException {
		// check if all expected log entries were seen
		if(expectedLogs.size() > 0) {
			throw new IllegalStateException("Did expect certain log entries, but did not find the following items in the logs: " + expectedLogs.toString());
		}

		cleanup();

		if(!started) {
			return;
		}

		// finally wait for process to stop
		resultHandler.waitFor(60000);

		if(!resultHandler.hasResult()) {
			log.warn("Launcher did not stop, tried to kill it!");
			watchdog.destroyProcess();
		}
	}

	public static void checkPortRange(int portStart, int portEnd) {
		assertTrue("Expected end to be higher or equal to start, but had start: " + portStart + " and end: " + portEnd,
				portEnd >= portStart);
		for(int i = portStart;i <= portEnd;i++) {
			if (i != 8081) {
				checkPort(i);
			}
		}
	}

	public static void checkPort(int port) {
		assertTrue("Port " + port + " is in use", SocketUtils.isPortAvailable(port, null));
		assertTrue("Port " + port + " is in use on localhost", SocketUtils.isPortAvailable(port, "localhost"));
		assertTrue("Port " + port + " is in use on hostname " + addr.getHostName(), SocketUtils.isPortAvailable(port, addr.getHostName()));
		assertTrue("Port " + port + " is in use on canonical hostname " + addr.getCanonicalHostName(), SocketUtils.isPortAvailable(port, addr.getCanonicalHostName()));
	}

	/**
	 *	If Launcher is running, stop it and all its Procedures.
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void cleanup() throws IOException, InterruptedException {
		log.info("Ensuring that no easyTravel-process is left over from previous tests");

		EasyTravelConfig config = EasyTravelConfig.read();

		// send shutdown to Business Backend and Customer Frontend if they are still running
		shutdownTomcat(config.backendShutdownPort);
		for(int port = config.backendShutdownPortRangeStart;port < config.backendShutdownPortRangeEnd;port++) {
			shutdownTomcat(port);
		}
		for(int port = config.frontendShutdownPortRangeStart;port < config.frontendShutdownPortRangeEnd;port++) {
			shutdownTomcat(port);
		}
		shutdownTomcat(config.thirdpartyShutdownPort);

		stopPluginService(config);

		// try to stop all procedures
		stopLauncher(config);

		// try to shutdown database if internal db is now still enabled and port is still in use
		shtudownRDBMS(config);

		// ensure that Apache is also stopped even if it did not gracefully shut down with previous steps
		stopApacheHttpd(config);

		stopMongoDb();

		// ensure that the CreditCardAuthentication procedure is not running any more
		stopNativeApplication();

		// reset list of expected log entries from previous runs
		expectedLogs = Collections.emptyList();
	}

	/**
	 * Delete Cassandra database nodes
	 */
	public static void cleanCassandraNodes () {

		File databaseDir = Directories.getDatabaseDir();

		if(databaseDir == null || !databaseDir.exists()) {
			log.info("Cannot read database dir: " + databaseDir);
			return;
		}

		log.info("Database directories: ");
		for(File dir: databaseDir.listFiles()) {
			log.info(dir.getAbsolutePath());
		}

		FilenameFilter nodeFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.startsWith("node")) {
					return true;
				} else {
					return false;
				}
			}
		};

		File[] nodes = databaseDir.listFiles(nodeFilter);

		log.info("Node count: " + Integer.valueOf(nodes.length));

		for(File node: nodes) {
			try {
				FileUtils.deleteDirectory(node);
			} catch (IOException e) {
				log.warn("Cannot delete Cassandra database nodes.",e);
			}
		}

	}


	private static void stopNativeApplication() {
		// ignore WARNINGS here as this usually means the procedure is not running currently
		ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger(CreditCardAuthorizationProcedure.class.getName());
		Level origLevel = log.getLevel();
		log.setLevel(Level.ERROR);

		ch.qos.logback.classic.Logger processlog = (ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger(AbstractProcess.class.getName());
		Level origProcessLevel = processlog.getLevel();
		processlog.setLevel(Level.ERROR);

		try {
			// create a procedure for the native app in order to stop it gracefully
			CreditCardAuthorizationProcedure proc = new CreditCardAuthorizationProcedure(ProcedureBuilder.creditCard().create());
			proc.stop();
		} catch (CorruptInstallationException e) {
			log.warn("Could not instantiate CreditCardAuthorizationProcedure for stop-check.", e);
		} finally {
			log.setLevel(origLevel);
			processlog.setLevel(origProcessLevel);
		}
	}

	private static void stopMongoDb() {
		if (SocketUtils.isPortAvailable(27017, null)) {
			return;
		}

		// ignore WARNINGS here as this usually means the procedure is not running currently
		ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger(MongoDbProcedure.class.getName());
		Level origLevel = log.getLevel();
		log.setLevel(Level.ERROR);

		ch.qos.logback.classic.Logger processlog = (ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger(AbstractProcess.class.getName());
		Level origProcessLevel = processlog.getLevel();
		processlog.setLevel(Level.ERROR);

		try {
			// create a procedure for the native app in order to stop it gracefully
			MongoDbProcedure proc = new MongoDbProcedure(ProcedureBuilder.mongodb().create());
			proc.stop();
		} catch (CorruptInstallationException e) {
			log.warn("Could not instantiate CreditCardAuthorizationProcedure for stop-check.", e);
		} finally {
			log.setLevel(origLevel);
			processlog.setLevel(origProcessLevel);
		}
	}

	private static void shtudownRDBMS(EasyTravelConfig config) throws InterruptedException {
		if(!config.internalDatabaseEnabled) {
			return;
		}

		// first wait some time to let launcher shut it down gracefully
		int i = 0;
		while(!isPortAvailable(config.internalDatabasePort)) {
			log.info("Waiting for database to shut down...");
			Thread.sleep(5000);

			if(i > 10) {
				break;
			}

			i++;
		}

		// then send the shutdown command a few times until giving up
		i = 0;
		while(!isPortAvailable(config.internalDatabasePort)) {
			log.warn("Trying to stop database on port " + config.internalDatabasePort);
			DbmsProcedure procedure = new DbmsProcedure(new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID));
			procedure.setConsoleWriter(new PrintWriter(System.out));
			procedure.init();
			procedure.stop();

			Thread.sleep(5000);

			if(i > 10) {
				throw new IllegalStateException("Port for internal database (" + config.internalDatabasePort + ") was still blocked after trying for 60 seconds to shutdown database server, process: " + TestHelpers.getProcessOnPort(config.internalDatabasePort));
			}

			i++;
		}
	}

	private static boolean isPortAvailable(int port) {
		return SocketUtils.isPortAvailable(port, null) &&
				SocketUtils.isPortAvailable(port, "localhost") &&
				SocketUtils.isPortAvailable(port, addr.getHostName()) &&
				SocketUtils.isPortAvailable(port, addr.getCanonicalHostName());
	}

	private static void stopLauncher(EasyTravelConfig config) throws IOException {
		if(isPortAvailable(config.launcherHttpPort)) {
			// make sure we send a shutdown as on Windows sometimes port is reported as available!
			try {
				UrlUtils.retrieveData("http://localhost:" + config.launcherHttpPort + "/" + Constants.REST.SHUTDOWN);
			} catch (ConnectException e) {
				log.debug("Had Connection Exception while sending shutdown to Launcher", e);
			}

			return;
		}

		log.info("Sending shutdown command to launcher");

		try {
			UrlUtils.retrieveData("http://localhost:" + config.launcherHttpPort + "/" + Constants.REST.SHUTDOWN);
		} catch (ConnectException e) {
			// accept this message as it may indicate that the Launcher stopped so quickly that the REST call failed
			// we are failing below if it
			if(!e.getMessage().contains("Connection refused")) {
				throw e;
			}

			log.warn("Had Connection Exception while sending shutdown to Launcher", e);
		}

		final int timeout = config.syncProcessTimeoutMs * MAX_PROCEDURES;
		PredicateMatcher<Object> matcher = new PredicateMatcher<Object>(null, timeout, config.processOperatingCheckIntervalMs);

		// first ensure that the Frontend is gone
		assertTrue("Customer Frontend did not stop in time",
				matcher.waitForMatch(new PortPredicate(config.frontendPortRangeStart, false)));
		assertTrue("Business Backend did not stop in time",
				matcher.waitForMatch(new PortPredicate(config.backendPort, false)));

		String url = "http://localhost:" + config.launcherHttpPort;
		assertTrue("Could not stop launcher at " + url + " in the timeout of " + timeout + " ms, process: " + TestHelpers.getProcessOnPort(config.launcherHttpPort),
				matcher.waitForMatch(new URLPredicate(url, false)));
	}

	private static void stopPluginService(EasyTravelConfig config) throws IOException, InterruptedException {
		if(config.pluginServiceHost == null) {
			return;
		}

		if(isPortAvailable(config.pluginServicePort)) {
			return;
		}

		// first wait some time to let launcher shut it down gracefully
		for(int i = 0;i < 10;i++) {
			if(isPortAvailable(config.pluginServicePort)) {
				// done already, no port is blocked any more
				return;
			}

			log.info("Waiting on port " + config.pluginServicePort + " for plugin service to shut down...");
			Thread.sleep(1000);
		}

		log.info("Sending shutdown command to launcher");

		String url = "http://localhost:" + config.pluginServicePort;
		try {
			UrlUtils.retrieveData(url + "/" + Constants.REST.SHUTDOWN);
		} catch (ConnectException e) {
			// accept this message as it may indicate that the Launcher stopped so quickly that the REST call failed
			// we are failing below if it
			if(!e.getMessage().contains("Connection refused")) {
				throw e;
			}

			log.warn("Had Connection Exception while sending shutdown to Launcher", e);
		}

		PredicateMatcher<Object> matcher = new PredicateMatcher<Object>(null, config.syncProcessTimeoutMs, config.processOperatingCheckIntervalMs);

		assertTrue("Could not stop plugin service at " + url + " in the timeout of " + config.syncProcessTimeoutMs + " ms, process: " + TestHelpers.getProcessOnPort(config.pluginServicePort),
				matcher.waitForMatch(new URLPredicate(url, false)));
	}

	private static void stopApacheHttpd(EasyTravelConfig config) throws IOException, InterruptedException {
		if(!isApacheHttpdRunning(config)) {
			return;
		}

		log.info("Found Apache still running on port(s) " +
				(isPortAvailable(config.apacheWebServerB2bPort) ? "" : "B2B: " + config.apacheWebServerB2bPort) +
				(isPortAvailable(config.apacheWebServerPort) ? "" : ", Frontend: " + config.apacheWebServerPort) +
				(isPortAvailable(config.apacheWebServerProxyPort) ? "" : ", Proxy: " + config.apacheWebServerProxyPort)
				);
		String[] killInstruction = ApacheHttpdUtils.getKillInstruction();

		ApacheHttpdUtils.killProcess(killInstruction);
		for (int i = 0; i < 10; i++) {
			if (!isApacheHttpdRunning(config)) {
				break;
			}
			Thread.sleep(100);
		}
		if(isApacheHttpdRunning(config) && new File(Directories.getTempDir(), "httpd.pid").exists()) {
			ApacheHttpdUtils.killIfNotTerminatedLinux();
		}
	}

	private static boolean isApacheHttpdRunning(EasyTravelConfig config) {
		return !isPortAvailable(config.apacheWebServerB2bPort) ||
				!isPortAvailable(config.apacheWebServerPort) ||
				!isPortAvailable(config.apacheWebServerProxyPort);
	}


	private static void shutdownTomcat(int port) {
		// linux
		if(!SocketUtils.isPortAvailable(port, null)) {
			TomcatShutdownCommand shutdown = new TomcatShutdownCommand(LocalUriProvider.getLoopbackAdapter(), port);
			Feedback feedback = shutdown.execute();

			// wait a bit before trying again, sometimes it takes a bit until shutdown is complete
			if(!feedback.isOk() && !SocketUtils.isPortAvailable(port, null)) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					fail("Interrupted: " + e);
				}
			}

			// if we get back a failure, check if Tomcat finished in the meantime
			assertTrue("Expected ok during shutdown of tomcat at port " + port + ", but had: " + feedback + " and port is still occupied, process: " + TestHelpers.getProcessOnPort(port),
					feedback.isOk() || SocketUtils.isPortAvailable(port, null));
		}

		// windows
		if(!SocketUtils.isPortAvailable(port, "localhost")) {
			TomcatShutdownCommand shutdown = new TomcatShutdownCommand(LocalUriProvider.getLoopbackAdapter(), port);
			Feedback feedback = shutdown.execute();

			// wait a bit before trying again, sometimes it takes a bit until shutdown is complete
			if(!feedback.isOk() && !SocketUtils.isPortAvailable(port, null)) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					fail("Interrupted: " + e);
				}
			}

			// if we get back a failure, check if Tomcat finished in the meantime
			assertTrue("Expected ok during shutdown of tomcat at port " + port + ", but had: " + feedback + " and port is still occupied, process: " + TestHelpers.getProcessOnPort(port),
					feedback.isOk() || SocketUtils.isPortAvailable(port, "localhost"));
		}
	}

	private static void waitForLauncherStartupDone(EasyTravelConfig config, boolean webLauncher) {
		log.info("Done starting launcher, waiting for scenario to be fully started");

		// wait for launcher-REST interface to become active
		// give some extra time for Cassandra
		final int timeout = (int) (config.syncProcessTimeoutMs * MAX_PROCEDURES + TimeUnit.SECONDS.toMillis(60));
		PredicateMatcher<Object> matcher = new PredicateMatcher<Object>(null, timeout, config.processOperatingCheckIntervalMs);

		String url = "http://localhost:" + config.launcherHttpPort;
		URLPredicate launcherStartingPredicate = new URLPredicate(url, true);
        assertTrue("Could not access launcher at " + url + " in the timeout of " + timeout + " ms",
        		matcher.waitForMatch(launcherStartingPredicate));
	}

	public static void waitForWebLauncherScenario(String expectedState) {
		EasyTravelConfig config = EasyTravelConfig.read();

		final int timeout = config.syncProcessTimeoutMs * MAX_PROCEDURES;
		PredicateMatcher<Object> matcher = new PredicateMatcher<Object>(null, timeout, config.processOperatingCheckIntervalMs);

		String url = "http://localhost:" + config.weblauncherPort + "/scenario/state";
		URLResponsePredicate weblauncherOperatingPredicate = new URLResponsePredicate(url, expectedState);
		assertTrue("WebLauncher did not report operating at " + url + " in the timeout of " + timeout + " ms",
				matcher.waitForMatch(weblauncherOperatingPredicate));
	}


	private static void start(final CommandLine cmdLine, final File workingDir, Map<String,String> environment) throws IOException, ExecuteException {
		resultHandler = new DefaultExecuteResultHandler();

		watchdog = new ExecuteWatchdog(180*1000);

		Executor executor = new DefaultExecutor();

		// set stream-handlers to log any output done by the application to our own logging
		executor.setStreamHandler(new PumpStreamHandler(
				new LogOutputStream() {
					@Override
					protected void processLine(String s, int i) {
						log.info(s);

						for(String expected : expectedLogs) {
							if(s.contains(expected)) {
								expectedLogs.remove(expected);
								break;
							}
						}
					}
				}));

		// working directory should not matter, but set it to have a defined working directory
		executor.setWorkingDirectory(workingDir);
		executor.setExitValue(1);
		executor.setWatchdog(watchdog);
		@SuppressWarnings("unchecked")
		Map<String,String> envMap = new DefaultProcessingEnvironment().getProcEnvironment();

		// remove some useless environment variables which screw up the log output on Linux
		envMap.remove("COLORFGBG");
		envMap.remove("LESSCLOSE");
		envMap.remove("LS_COLORS");
		envMap.remove("LESS_TERMCAP_mb");
		envMap.remove("LESS_TERMCAP_me");
		envMap.remove("LESS_TERMCAP_md");
		envMap.remove("LESS_TERMCAP_se");
		envMap.remove("LESS_TERMCAP_so");
		envMap.remove("LESS_TERMCAP_us");
		envMap.remove("LESSOPEN");

		envMap.put("COVERAGE", getCoverageSetting("${id}"));
		if(environment != null) {
			envMap.putAll(environment);
		}

		log.info("Starting with commandline: " + cmdLine +
				"\nWorking directory: " + workingDir +
				"\nEnvironment-Variables: " + envMap);
		executor.execute(cmdLine, envMap, resultHandler);
	}

	public static String getCoverageSetting(String id) {
		String jacocoExcludes = System.getProperty("jacoco.excludes");
		if(jacocoExcludes == null) {
			// current fallback if started without actual settings, otherwise icefaces fails with some portlet-class not found
			jacocoExcludes = "org.*:junit.*:net.sf.*:net.sourceforge.*:com.dumbster.*:javax.*:sun.*:serp.*:$Proxy*:$java.lang.*:com.steadystate.*:com.gargoylesoftware.*:antlr.*:com.sun.*:com.ibm.*:oracle.*:com.mysql.*:javassist.*:edu.*:com.icesoft.*:schemaorg_apache_xmlbeans.*:com.dynatrace.webautomation.*:com.yahooo.*:*$$EnhancerByMockitoWithCGLIB$$*:*$$_javassist_*:com.dynatrace.installer.*:com.dynatrace.util.*";
		}

		return "-javaagent:" + TestEnvironment.ROOT_PATH + "/ThirdPartyLibraries/JaCoCo/jacocoagent.jar=" +
				"destfile=" + TestEnvironment.ROOT_PATH + "/TravelTest/jacoco." + id + ".exec," +
				"excludes=" + jacocoExcludes;
	}

	private static void failIfProcessStopped() {
		if(!resultHandler.hasResult()) {
			return;
		}

/*			{
				final CommandLine cmdLine = new CommandLine("ps");
				cmdLine.addArgument("-aef");

				DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

				//ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);

				Executor executor = new DefaultExecutor();

				// set stream-handlers to log any output done by the application to our own logging
				executor.setStreamHandler(new PumpStreamHandler(
						new LogOutputStream() {

							@Override
							protected void processLine(String s, int i) {
								log.info(s);
							}
				}));

				try {
					executor.execute(cmdLine, envMap, resultHandler);

					resultHandler.waitFor();
				} catch (ExecuteException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}*/

		if(resultHandler.getException() != null) {
			// log to get a timestamp entry in the log
			log.warn("Process stopped while we did not expect it to, ", resultHandler.getException());
			//throw new RuntimeException(resultHandler.getException());
		}

		// log to get a timestamp entry in the log
		log.warn("Process stopped while we did not expect it to, had exit value: " + resultHandler.getExitValue());
		//throw new RuntimeException("Did not yet expect process to be stopped, but stopped with exit value " + resultHandler.getExitValue());
	}

    private static final class URLPredicate implements Predicate<Object> {
    	private final String url;
    	private final boolean startedOnUrl;

        public URLPredicate(String url, boolean started) {
			super();
			this.url = url;
			this.startedOnUrl = started;
		}

		@Override
        public boolean eval(Object unused) {
			log.info("Checking if url " + url + " is " + (startedOnUrl ? "available now" : "not available any more"));
			if(startedOnUrl) {
				failIfProcessStopped();

				return UrlUtils.checkConnect(url).isOK();
			} else {
				return !UrlUtils.checkConnect(url).isOK();
			}
        }

		@Override
		public boolean shouldStop() {
			return false;
		}
    }

    private static final class URLResponsePredicate implements Predicate<Object> {
    	private final String url;
    	private final String expectedResponse;

        public URLResponsePredicate(String url, String expectedResponse) {
			super();
			this.url = url;
			this.expectedResponse = expectedResponse;
		}

		@Override
        public boolean eval(Object unused) {
			failIfProcessStopped();

			String ret;
			try {
				ret = UrlUtils.retrieveData(url, 5000);
				log.info("Checking if url " + url + " returns " + expectedResponse + ": had response: " + ret);
				return expectedResponse.equals(ret);
			} catch (IOException e) {
				log.info("Had exception while checking url response from url '" + url + "': " + e.getMessage());
				return false;
			}
        }

		@Override
		public boolean shouldStop() {
			return false;
		}
    }

    private static final class PortPredicate implements Predicate<Object> {
    	private final int port;
    	private final boolean startedOnUrl;

        public PortPredicate(int port, boolean started) {
			super();
			this.port = port;
			this.startedOnUrl = started;
		}

		@Override
        public boolean eval(Object unused) {
			log.info("Checking if port " + port + " is " + startedOnUrl);
			if(startedOnUrl) {
				failIfProcessStopped();

				return !SocketUtils.isPortAvailable(port, null);
			} else {
				return SocketUtils.isPortAvailable(port, null);
			}
        }

		@Override
		public boolean shouldStop() {
			return false;
		}
    }

	public static void checkAllPorts(EasyTravelConfig config) {
		checkProcedurePorts(config);

		// and also launcher/weblauncher related ports
		log.info("Check that the configured ports are not already used: " + config.filePath + ", \n" +
				EasyTravelConfig.buildLocalETPropertiesFileSituationLogEntry() + ", " +
				EasyTravelConfig.buildPrivateETPropertiesFileSituationLogEntry() +
				config.launcherHttpPort + ", " +
				config.weblauncherPort + ", " +
				config.weblauncherShutdownPort
				);

		checkPort(config.launcherHttpPort);
		checkPort(config.weblauncherPort);
		checkPort(config.weblauncherShutdownPort);
	}

	public static void checkProcedurePorts(EasyTravelConfig config) {
		log.info("Check that the configured procedure-ports are not already used: " + config.filePath + ", \n" +
				EasyTravelConfig.buildLocalETPropertiesFileSituationLogEntry() + ", " +
				EasyTravelConfig.buildPrivateETPropertiesFileSituationLogEntry() +
				config.apacheWebServerB2bPort + ", " +
				config.apacheWebServerPort + ", " +
				config.apacheWebServerProxyPort + ", " +
				config.b2bFrontendPortRangeStart + "-" +config.b2bFrontendPortRangeEnd + ", " +
				config.paymentBackendPort + ", " +
				config.backendPort + ", " +
				config.backendShutdownPort + ", " +
				config.frontendAjpPortRangeStart + "-" +config.frontendAjpPortRangeEnd + ", " +
				config.frontendPortRangeStart + "-" +config.frontendPortRangeEnd + ", " +
				config.frontendShutdownPortRangeStart + "-" +config.frontendShutdownPortRangeEnd + ", " +
				config.internalDatabasePort + ", " +
				config.memcachedServerPort + ", " +
				config.creditCardAuthorizationSocketPort + ", " +
				config.apacheWebServerStatusPort + ", " +
				config.pluginServiceHost + ":" + config.pluginServicePort
				);
		checkPort(config.apacheWebServerB2bPort);
		checkPort(config.apacheWebServerPort);
		checkPort(config.apacheWebServerProxyPort);
		if(!AbstractDotNetProcedure.checkIsRunningOnIIS(config.b2bFrontendPageToIdentify, config.b2bFrontendPortRangeStart)) {
			checkPortRange(config.b2bFrontendPortRangeStart, config.b2bFrontendPortRangeEnd);
		} else {
			log.info("Not checking B2B Frontend port because it is running on IIS");
		}
		if(!AbstractDotNetProcedure.checkIsRunningOnIIS(config.paymentBackendPageToIdentify, config.paymentBackendPort)) {
			checkPort(config.paymentBackendPort);
		} else {
			log.info("Not checking Payment Backend port because it is running on IIS");
		}
		checkPort(config.backendPort);
		checkPort(config.backendShutdownPort);
		checkPortRange(config.frontendAjpPortRangeStart, config.frontendAjpPortRangeEnd);
		checkPortRange(config.frontendPortRangeStart, config.frontendPortRangeEnd);
		checkPortRange(config.frontendShutdownPortRangeStart, config.frontendShutdownPortRangeEnd);
		checkPort(config.internalDatabasePort);
		checkPort(config.memcachedServerPort);
		checkPort(config.creditCardAuthorizationSocketPort);
		checkPort(config.thirdpartyPort);
		checkPort(config.thirdpartyShutdownPort);
		checkPort(config.mysqlPort);
		if(config.apacheWebServerStatusPort > 0) {
			checkPort(config.apacheWebServerStatusPort);
		}
		if(config.pluginServiceHost != null) {
			checkPort(config.pluginServicePort);
		}

//		assertEquals(Availability.NOT_FOUND, UrlUtils.checkConnect(config.thirdpartyUrl, 1000));
//		assertEquals(Availability.NOT_FOUND, UrlUtils.checkConnect(config.thirdpartyCdnUrl, 1000));
//		assertEquals(Availability.NOT_FOUND, UrlUtils.checkConnect(config.thirdpartySocialMediaUrl, 1000));
//		assertEquals(Availability.NOT_FOUND, UrlUtils.checkConnect(config.b2bFrontendPublicUrl, 1000));
//		assertEquals(Availability.NOT_FOUND, UrlUtils.checkConnect(config.frontendPublicUrl, 1000));
//		assertEquals(Availability.NOT_FOUND, UrlUtils.checkConnect(config.databaseUrl, 1000));
//		assertEquals(Availability.NOT_FOUND, UrlUtils.checkConnect(config.mysqlUrl, 1000));
//		assertEquals(Availability.NOT_FOUND, UrlUtils.checkConnect(config.apacheFrontendPublicUrl, 1000));
//		assertEquals(Availability.NOT_FOUND, UrlUtils.checkConnect(config.apacheB2BFrontendPublicUrl, 1000));

		// some ports used in tests
		checkPortRange(15100, 15110);	// MockRESTServer
	}

	/*public static void assertRunClasses(Class<?>... classes) {
		Result result = JUnitCore.runClasses(classes);
		assertEquals("Expected zero failures while running tests from " + ArrayUtils.toString(classes) + ", \n" +
				"but had: " + result.getFailureCount() + ":\n" +
				result.getFailures(),
				0, result.getFailureCount());
	}*/
}
