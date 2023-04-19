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

public class CustomerFrontendProcedureTest {
    private static final Logger LOGGER = LoggerFactory.make();

    static {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

		LOGGER.info("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();

		int port = EasyTravelConfig.read().frontendPortRangeStart;
		assertTrue("Need port " + port + " for unit test",
				SocketUtils.isPortAvailable(port, null));
	}
	
	@Test
	public void testEnvironment() throws IOException, CorruptInstallationException, ConfigurationException, InterruptedException {
		final ProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID);
	
		// Set config values to test if they are overwritten, and append to.
		// Note that the EasyTravelConfig object will contain already cut frontEnfArgs strings
		// one variable and value per string.
		EasyTravelConfig.read().frontendEnvArgs = new String[3];
		EasyTravelConfig.read().frontendEnvArgs[0] = "RUXIT_CLUSTER_ID=CF-FROM_CONFIG_CLUSTERID";
		EasyTravelConfig.read().frontendEnvArgs[1] = "RUXIT_NODE_ID=CF-FROM_CONFIG_NODEID";
		EasyTravelConfig.read().frontendEnvArgs[2] = "SOME_OTHER_VARIABLE=fred";

		// Add per-procedure settings to the mapping: these need to be in the un-cut form.
		mapping.addSetting(new DefaultProcedureSetting("procedure_config", "config.frontendEnvArgs",
				"RUXIT_CLUSTER_ID=CF-MultipleTest1_CLUSTER,RUXIT_NODE_ID=CF-MultipleTest1_NODE"));

		// Create a procedure with those custom per-procedure settings
		CustomerFrontendProcedure proc = new CustomerFrontendProcedure(mapping);

		assertFalse(proc.isOperating());
		
		assertEquals(Feedback.Success, proc.run());
		
		try {
			assertTrue(proc.isOperatingCheckSupported());

			while(!proc.isOperating() && proc.isRunning()) {
				Thread.sleep(500);
			}

			assertTrue(proc.isRunning());
			assertTrue(proc.isOperating());

			// Test that the custom settings are taken into account.
			DtAgentConfig myDtAgentConfig = proc.getAgentConfig();
			assertNotNull(myDtAgentConfig);
			Map<String, String> envArgs = myDtAgentConfig.getEnvironmentArgs();
			assertTrue (envArgs.get("RUXIT_CLUSTER_ID").equals("CF-MultipleTest1_CLUSTER"));
			assertTrue (envArgs.get("RUXIT_NODE_ID").equals("CF-MultipleTest1_NODE"));
			assertTrue (envArgs.get("SOME_OTHER_VARIABLE").equals("fred"));
		
		} finally {
			assertEquals("First stop works", Feedback.Success, proc.stop());
			assertEquals("Second stop reports success but does not do anything", Feedback.Success, proc.stop());
		}
		
		EasyTravelConfig.resetSingleton();
	}

	@Test
	public void test() throws InterruptedException, CorruptInstallationException {
		CustomerFrontendProcedure proc = new CustomerFrontendProcedure(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));

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
			assertEquals(EasyTravelConfig.read().frontendPortRangeStart, proc.getPort());
			assertEquals("customerFrontendPort", proc.getPortPropertyName());
			assertEquals(StopMode.PARALLEL, proc.getStopMode());

			assertEquals(EasyTravelConfig.read().frontendContextRoot, proc.getContextRoot());
			assertEquals(EasyTravelConfig.read().frontendShutdownPortRangeStart, proc.getShutdownPort());
		} finally {
			assertEquals("First stop works", Feedback.Success, proc.stop());
			assertEquals("Second stop reports success but does not do anything", Feedback.Success, proc.stop());
		}
	}
}
