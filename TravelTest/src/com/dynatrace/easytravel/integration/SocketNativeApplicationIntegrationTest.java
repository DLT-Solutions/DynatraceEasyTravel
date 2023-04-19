/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: NativeApplicationIntegrationTest.java
 * @date: 06.11.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.integration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.ipc.SocketTest;
import com.dynatrace.easytravel.launcher.engine.Procedure;
import com.dynatrace.easytravel.launcher.procedures.CreditCardAuthorizationProcedure;
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
	SocketTest.class
})
public class SocketNativeApplicationIntegrationTest {
	private static final Logger log = LoggerFactory.make();

	private static MockRESTServer businessbackendmock;

	private static Procedure proc;

	// TODO: adjust to NamedPipe version of this test, DRY!

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
		proc = NamedPipeNativeApplicationIntegrationTest.runCreditCardCheck(null);
		proc.stop();

		// then also run with explicitely setting NamedPipe
		proc = NamedPipeNativeApplicationIntegrationTest.runCreditCardCheck(CreditCardAuthorizationProcedure.IpcMode.Socket);

		log.info("Now starting tests...");
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
