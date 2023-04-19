package com.dynatrace.easytravel.launcher.procedures;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.process.NativeProcess;
import com.dynatrace.easytravel.launcher.process.Process;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPRunnable;
import com.dynatrace.easytravel.utils.TestEnvironment;
import com.dynatrace.easytravel.utils.TestHelpers;


public class AbstractDotNetProcedureTest {
    private static final Logger LOGGER = LoggerFactory.make();

	@BeforeClass
	public static void setupUpClass() throws IOException {
		LoggerFactory.initLogging();

		// for testing set a short timeout to not let the test run for three minutes!
		AbstractDotNetProcedure.setISSRequestTimeout(1000);

		TestEnvironment.createOrClearRuntimeData();
		exec = File.createTempFile("test", ".exe", new File(TestEnvironment.RUNTIME_DATA_PATH));
	}

    @Before
	public void setUp() {
		AbstractDotNetProcedure.clearCache();
	}

	private static File exec;

	@Test
	public void testRunAndStop() throws CorruptInstallationException {
		AbstractDotNetProcedure proc = new MockDotNetProcedure();

		assertTrue(proc.hasLogfile());

		assertEquals("We try to start, but don't know if it succeeds", Feedback.Neutral, proc.run());

		assertEquals("Stop succeeds nevertheless", Feedback.Success, proc.stop());
	}

	private static String getExecName() {
		String path = exec.getPath();
		if (OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
			return path.substring(0, path.length() - ".exe".length());
		} else {
			return path;
		}
	}

    private static DtAgentConfig createAgentConfig() {
    	final EasyTravelConfig config = EasyTravelConfig.read();
    	return new DtAgentConfig(config.b2bFrontendSystemProfile, null, config.b2bFrontendAgentOptions, config.b2bFrontendEnvArgs);
    }

	@Test
	public void testAbstractDotNetProcedureProcedureMappingStringDtAgentConfigString() throws CorruptInstallationException {
		AbstractDotNetProcedure proc = new MockDotNetProcedure();
		assertNotNull(proc);
	}

	@Test
	public void testIsOperating() throws CorruptInstallationException {
		AbstractDotNetProcedure proc = new MockDotNetProcedure();

		assertTrue(proc.isOperatingCheckSupported());
		assertFalse(proc.isOperating());
	}

	@Test
	public void testCreateInvalidExe() {
		try {
			new MockDotNetProcedure() {
				@Override
				public String getExecutable(ProcedureMapping mapping) {
					return "nonexisting";
				}
			};

			fail("Should catch exception because of non-existing executeable file");
		} catch (CorruptInstallationException e) {
			TestHelpers.assertContains(e, "file could not be found", "nonexisting");
		}
	}

	@Test
	public void testIsAgentFound() throws Exception {
		EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
		DtVersionDetector.enforceInstallationType(null);
		try {
			assertTrue(DtVersionDetector.isClassic());

			AbstractDotNetProcedure proc = new MockDotNetProcedure();

			assertTrue(proc.isInstrumentationSupported());
			String enabled = System.getenv(Constants.Misc.ENV_VAR_COR_ENABLE_PROFILING);
			if(enabled == null) {
				enabled = proc.getProcess().getDtAgentConfig().getEnvironmentArgs().get(Constants.Misc.ENV_VAR_COR_ENABLE_PROFILING);
			}
			if((System.getenv(Constants.Misc.ENV_VAR_COR_PROFILER) != null ||
					proc.getProcess().getDtAgentConfig().getEnvironmentArgs().get(Constants.Misc.ENV_VAR_COR_PROFILER) != null) &&
					("0x01".equals(enabled) || "0x1".equals(enabled))) {
				assertTrue("Expecting agent found if both system wide vars are set",
						proc.agentFound());
			} else {
				assertFalse("Expecting no agent found if one of the system wide vars are set",
						proc.agentFound());
			}

			Process process = proc.getProcess();
			assertTrue(process.getClass().getName(), process instanceof NativeProcess);
			NativeProcess nprocess = (NativeProcess)process;

			// cannot set them via system wide env-var here, only via the environment of the process
			nprocess.getDtAgentConfig().getEnvironmentArgs().put(Constants.Misc.ENV_VAR_COR_PROFILER, "{0000}");	// content is not checked
			nprocess.getDtAgentConfig().getEnvironmentArgs().put(Constants.Misc.ENV_VAR_COR_ENABLE_PROFILING, "0x1");	// content is checked

			assertTrue(proc.isInstrumentationSupported());
			assertTrue("We should report an agent now that we set both variables correctly",
					proc.agentFound());

			nprocess.getDtAgentConfig().getEnvironmentArgs().put(Constants.Misc.ENV_VAR_COR_PROFILER, "{0000}");	// content is not checked
			nprocess.getDtAgentConfig().getEnvironmentArgs().put(Constants.Misc.ENV_VAR_COR_ENABLE_PROFILING, "0x0");	// content is checked

			assertTrue(proc.isInstrumentationSupported());
			assertFalse("We should not report an agent now that we set 'enable' to 0x0",
					proc.agentFound());
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testIsAgentFoundAPM() throws Exception {
		DtVersionDetector.enforceInstallationType(InstallationType.APM);
		try {
			assertTrue(DtVersionDetector.isAPM());

			AbstractDotNetProcedure proc = new MockDotNetProcedure();

			assertFalse(proc.isInstrumentationSupported());
		} finally {
			DtVersionDetector.enforceInstallationType(null);
		}
	}

	@Test
	public void testGetTechnology() throws Exception {
		AbstractDotNetProcedure proc = new MockDotNetProcedure();
		assertEquals(Technology.DOTNET_20, proc.getTechnology());
	}

	@Test
	public void testGetStopMode() throws Exception {
		AbstractDotNetProcedure proc = new MockDotNetProcedure();
		assertEquals(proc.isRunningOnLocalIIS() ? StopMode.NONE : StopMode.PARALLEL,
				proc.getStopMode());
	}

	@Test
	public void testIsProcessOnPortWhitelist() {

		assertTrue("True on empty name", AbstractDotNetProcedure.isProcessOnPortWhitelist(null));
		assertTrue("True on empty name", AbstractDotNetProcedure.isProcessOnPortWhitelist(""));

		assertTrue("True on whitelist-entry", AbstractDotNetProcedure.isProcessOnPortWhitelist("svchost.exe"));
		assertTrue("True on whitelist-entry", AbstractDotNetProcedure.isProcessOnPortWhitelist("sVCHost.exe"));

		assertFalse("False on non-whitelist-entry", AbstractDotNetProcedure.isProcessOnPortWhitelist("someproc.exe"));
		assertFalse("False on non-whitelist-entry", AbstractDotNetProcedure.isProcessOnPortWhitelist("java.exe"));
	}

	@Test
	public void testCheckIsRunningOnIIS() throws IOException {
		checkIsRunningOnIISWithTimeout(null, 0);
		checkIsRunningOnIISWithTimeout("site", 0);
		checkIsRunningOnIISWithTimeout("site/", 0);

		int port = SocketUtils.reserveNextFreePort(9100, 10000, null);
		try {
			checkIsRunningOnIISWithTimeout("site/", port);
		} finally {
			SocketUtils.freePort(port);
		}
	}

	@Test
	public void testCheckIsRunningOnIISWithServer() throws IOException {
		MockRESTServer server = new MockRESTServer("status", "mime", "message");
		try {
			checkIsRunningOnIISWithTimeout("site/", server.getPort());
		} finally {
			server.stop();
		}
	}

	@Test
	public void testCheckIsRunningOnIISWithServerWhichHangs() throws IOException {
		final Object lock = new Object();

		HTTPRunnable runnable = new HTTPRunnable() {
			@Override
			public void run(String uri, String method, Properties header, Properties parms) {
				try {
					synchronized (lock) {
						lock.wait();
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		};

		MockRESTServer server = new MockRESTServer(runnable, "status", "mime", "message");
		try {
			checkIsRunningOnIISWithTimeout("site/", server.getPort());

			// allow response-thread to stop
			synchronized (lock) {
				lock.notify();
			}
		} finally {
			server.stop();
		}
	}

	@Test
	public void testCheckIsRunningOnIISWithServerException() throws IOException {
		HTTPRunnable runnable = new HTTPRunnable() {
			@Override
			public void run(String uri, String method, Properties header, Properties parms) {
				throw new RuntimeException("Testexception");
			}
		};

		MockRESTServer server = new MockRESTServer(runnable, "status", "mime", "message");
		try {
			checkIsRunningOnIISWithTimeout("site/", server.getPort());
		} finally {
			server.stop();
		}
	}

	public static int IIS_REQUEST_TIMEOUT_MS = (int) TimeUnit.MINUTES.toMillis(3); // we use a high Timeout for IIS request,
	@Ignore("Local test to see why IIS based procedures are not reported correctly on dynaSprint")
	@Test
	public void testDynaSprint() throws Exception {
        final EasyTravelConfig CONFIG = EasyTravelConfig.read();

		String site = CONFIG.paymentBackendPageToIdentify;
		int port = CONFIG.paymentBackendPort;

		if (site == null) {
			LOGGER.warn("Cannot check if site on port " + port +
					" is running on local IIS. Possible reason: URL to indentify easyTravel site seems to be missing in easyTravelConfig.properties.");
			fail("failed");
		}

		final URL url;
		//url = new URL("SECRET");
		url = new URL("SECRET");

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try {
			conn.setDoOutput(false);
			conn.setDoInput(true);
			conn.setConnectTimeout(IIS_REQUEST_TIMEOUT_MS);
			conn.setReadTimeout(IIS_REQUEST_TIMEOUT_MS);

			// if connecting is not possible this will throw a connection refused exception
			conn.connect();

			// if site searched for is not running on given port on local iis we return false here
			if (conn.getResponseCode() >= 400) {
				fail("URL " + url + " Error: " + conn.getResponseMessage());
			}

			String headerField = conn.getHeaderField("Server");
			boolean ret = headerField != null && headerField.contains("IIS");
			assertTrue(ret);
		} finally {
			conn.disconnect();
		}

		//assertTrue(AbstractDotNetProcedure.checkIsRunningOnIIS(CONFIG.paymentBackendPageToIdentify, CONFIG.paymentBackendPort));
	}

	private void checkIsRunningOnIISWithTimeout(String site, int port) {
		long start = System.currentTimeMillis();
		assertFalse(AbstractDotNetProcedure.checkIsRunningOnIIS(site, port));
		assertFalse(AbstractDotNetProcedure.checkIsRunningOnIIS(site, port));
		assertTrue("Request ("+site+":"+port+") should not take more than "+ TimeUnit.MILLISECONDS.toMinutes(AbstractDotNetProcedure.IIS_REQUEST_TIMEOUT_MS)+" minutes, but took: " + (System.currentTimeMillis() - start),
				(System.currentTimeMillis() - start) < (AbstractDotNetProcedure.IIS_REQUEST_TIMEOUT_MS+1000));
	}

	private static class MockDotNetProcedure extends AbstractDotNetProcedure {

		protected MockDotNetProcedure() throws CorruptInstallationException {
			super(new DefaultProcedureMapping("somemapping"));
		}

		@Override
		public String getLogfile() {
			return null;
		}

		@Override
		protected String getExecutable(ProcedureMapping mapping) {
			return getExecName();
		}

		@Override
		protected DtAgentConfig getAgentConfig() {
			return createAgentConfig();
		}

		@Override
		protected String getWorkingDir() {
			return "..";
		}

		@Override
		public boolean isRunningOnLocalIIS(){
			return false;
		}

		@Override
		protected void log(String logMessage){
			//do nothing
		}

	}
}
