package com.dynatrace.easytravel.launcher.procedures;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.database.DatabaseBase;
import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.TestHelpers;

public class BusinessBackendProcedureTest extends DatabaseBase {
    private static final int SHUTDOWN_TIMEOUT = 2 * 60 * 1000;
	private static final Logger LOGGER = LoggerFactory.make();

    static {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

		LOGGER.info("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

	@BeforeClass
	public static void setUpClass() throws IOException {
		// start database if necessary
		DatabaseBase.setUpClass();

		LoggerFactory.initLogging();

		int port = EasyTravelConfig.read().backendPortRangeStart;
		assertTrue("Need port " + port + " for unit test",
				SocketUtils.isPortAvailable(port, null));
	}
	
	@Test
	public void testEnvironment() throws IOException, CorruptInstallationException, ConfigurationException, InterruptedException {
		final ProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);
		
		// Set config values to test if they are overwritten, and append to.
		// Note that the EasyTravelConfig object will contain already cut frontEnfArgs strings
		// one variable and value per string.
		EasyTravelConfig.read().backendEnvArgs = new String[3];
		EasyTravelConfig.read().backendEnvArgs[0] = "RUXIT_CLUSTER_ID=BB-FROM_CONFIG_CLUSTERID";
		EasyTravelConfig.read().backendEnvArgs[1] = "RUXIT_NODE_ID=BB-FROM_CONFIG_NODEID";
		EasyTravelConfig.read().backendEnvArgs[2] = "SOME_OTHER_VARIABLE=fred";
		EasyTravelConfig.read().shutdownTimeoutMs = SHUTDOWN_TIMEOUT;
	
		// Add per-procedure settings to the mapping: these need to be in the un-cut form.
		mapping.addSetting(new DefaultProcedureSetting("procedure_config", "config.backendEnvArgs",
				"RUXIT_CLUSTER_ID=BB-MultipleTest_CLUSTER,RUXIT_NODE_ID=BB-MultipleTest_NODE"));

		// Create a procedure with those custom per-procedure settings
		BusinessBackendProcedure proc = new BusinessBackendProcedure(mapping);

		assertFalse(proc.isOperating());

		assertEquals(Feedback.Success, proc.run());
		
		try {
			assertTrue(proc.isOperatingCheckSupported());

			while(!proc.isOperating() && proc.isRunning()) {
				Thread.sleep(500);
			}

			assertTrue("Procedure should be running now, but wasn't", proc.isRunning());
			assertTrue(proc.isOperating());
			
			// Test that the custom settings are taken into account.
			DtAgentConfig myDtAgentConfig = proc.getAgentConfig();
			assertNotNull(myDtAgentConfig);
			Map<String, String> envArgs = myDtAgentConfig.getEnvironmentArgs();
			assertTrue (envArgs.get("RUXIT_CLUSTER_ID").equals("BB-MultipleTest_CLUSTER"));
			assertTrue (envArgs.get("RUXIT_NODE_ID").equals("BB-MultipleTest_NODE"));
			assertTrue (envArgs.get("SOME_OTHER_VARIABLE").equals("fred"));

		} finally {
			assertEquals("First stop works", Feedback.Success, proc.stop());
			assertEquals("Second stop reports success but does not do anything", Feedback.Success, proc.stop());
		}
		
		EasyTravelConfig.resetSingleton();
	}
	
	@Test
	public void test() throws InterruptedException, CorruptInstallationException {
		EasyTravelConfig.read().shutdownTimeoutMs = SHUTDOWN_TIMEOUT;
		
		BusinessBackendProcedure proc = new BusinessBackendProcedure(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID));

		assertFalse(proc.isOperating());

		assertEquals(Feedback.Success, proc.run());

		try {
			assertTrue(proc.isOperatingCheckSupported());

			while(!proc.isOperating() && proc.isRunning()) {
				Thread.sleep(500);
			}

			assertTrue("Procedure should be running now, but wasn't",
					proc.isRunning());
			assertTrue(proc.isOperating());

			assertTrue(proc.hasLogfile());
			assertNotNull(proc.getLogfile());
			assertEquals(EasyTravelConfig.read().backendPort, proc.getPort());
			assertEquals("businessBackendPort", proc.getPortPropertyName());
			assertEquals(StopMode.PARALLEL, proc.getStopMode());

			try {
				proc.getContextRoot();
				fail("Should catch Exception here");
			} catch (IllegalStateException e) {
				TestHelpers.assertContains(e, proc.getName(), "No contextRoot assigned");
			}

			try {
				proc.getShutdownPort();
				fail("Should catch Exception here");
			} catch (IllegalStateException e) {
				TestHelpers.assertContains(e, proc.getName(), "No shutdown port assigned");
			}
		} finally {
			assertEquals("First stop works", Feedback.Success, proc.stop());
			assertEquals("Second stop reports success but does not do anything", Feedback.Success, proc.stop());
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testWithMultiple() throws InterruptedException, CorruptInstallationException {
		EasyTravelConfig config = EasyTravelConfig.read();
		config.backendMultiEnabled = true;
		config.shutdownTimeoutMs = SHUTDOWN_TIMEOUT;
		try {
			BusinessBackendProcedure proc = new BusinessBackendProcedure(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID));

			assertFalse(proc.isOperating());

			assertEquals(Feedback.Success, proc.run());

			try {
				assertTrue(proc.isOperatingCheckSupported());

				while(!proc.isOperating() && proc.isRunning()) {
					Thread.sleep(500);
				}

				assertTrue(proc.isRunning());
				assertTrue(proc.isOperating());

				assertTrue(proc.hasLogfile());
				assertNotNull(proc.getLogfile());
				assertEquals(EasyTravelConfig.read().backendPortRangeStart, proc.getPort());
				assertEquals("businessBackendPort", proc.getPortPropertyName());
				assertEquals(StopMode.PARALLEL, proc.getStopMode());

				assertEquals(config.backendContextRoot, proc.getContextRoot());
				assertEquals(config.backendShutdownPortRangeStart, proc.getShutdownPort());
			} finally {
				assertEquals("First stop works", Feedback.Success, proc.stop());
				assertEquals("Second stop reports success but does not do anything", Feedback.Success, proc.stop());
			}
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}	
}
