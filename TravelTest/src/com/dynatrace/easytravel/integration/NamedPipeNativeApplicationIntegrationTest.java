/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: NativeApplicationIntegrationTest.java
 * @date: 06.11.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.integration;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.ipc.NamedPipeTest;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.Procedure;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.CreditCardAuthorizationProcedure;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;

import ch.qos.logback.classic.Logger;


/**
 * Small integration test which verifies the Native Applications
 * which use Named Pipe or Socket communication
 *
 * It manually starts the procedure and does not use the
 * Integration-Test-Support.
 *
 * @author dominik.stadler
 */
@RunWith(Suite.class)
@SuiteClasses({
	NamedPipeTest.class
})
public class NamedPipeNativeApplicationIntegrationTest {
	private static final Logger log = LoggerFactory.make();

	private static MockRESTServer businessbackendmock;

	private static Procedure proc;

	@BeforeClass
	public static void setUpClass() throws Exception {
		LoggerFactory.initLogging();

		// try to stop things that are still running now
		IntegrationTestBase.cleanup();

		// check that ports are available
		IntegrationTestBase.checkAllPorts(EasyTravelConfig.read());

		businessbackendmock = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, "invalid");
 		EasyTravelConfig.read().backendPort = businessbackendmock.getPort();

		// first run with default settings
		proc = runCreditCardCheck(null);
		proc.stop();

		// then also run with explicitely setting NamedPipe
		proc = runCreditCardCheck(CreditCardAuthorizationProcedure.IpcMode.NamedPipe);

		log.info("Now starting tests...");
	}

	protected static Procedure runCreditCardCheck(CreditCardAuthorizationProcedure.IpcMode mode) throws CorruptInstallationException, InterruptedException {
		log.info("Starting CreditCardAuthorization procedure with install-dir: " + Directories.getInstallDir() + " and ipc mode: " + mode);

		ProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID);

		if(mode != null) {
			mapping.addSetting(new DefaultProcedureSetting(CreditCardAuthorizationProcedure.SETTING_IPC_MODE, mode.name()));
		}

		Procedure proc = new CreditCardAuthorizationProcedure(mapping);
		Feedback feed = proc.run();
		assertEquals(Feedback.Neutral, feed);

		while (!proc.isOperating()) {
			log.info("Waiting for procedure to become operating");
			Thread.sleep(5000);
		}

		log.info("Result from starting procedure with IPC Mode: " + mode + ": " + feed);

		return proc;
	}

	@AfterClass
	public static void tearDownClass() {
		if(proc != null) {
			log.info("Stopping procedure.");
			proc.stop();
		}

		if(businessbackendmock != null) {
			businessbackendmock.stop();
		}
	}
}
