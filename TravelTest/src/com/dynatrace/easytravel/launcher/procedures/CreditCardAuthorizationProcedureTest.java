package com.dynatrace.easytravel.launcher.procedures;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.Architecture;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.engine.DatabaseEnvArgs;
import com.dynatrace.easytravel.launcher.engine.DatabaseEnvArgs.ChannelType;
import com.dynatrace.easytravel.launcher.engine.DatabaseEnvArgs.Vendor;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.CreditCardAuthorizationProcedure.IpcMode;
import com.dynatrace.easytravel.launcher.process.AbstractProcess;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class CreditCardAuthorizationProcedureTest {
    private static final Logger LOGGER = LoggerFactory.make();

	private static String execFileStr;
	private static boolean created = false;

	private final String NAME = "easyTravelBusiness";
	private final String ENDPOINT = "dbserv:50000";

	@BeforeClass
	public static void setUpClass() throws IOException {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

		if (OperatingSystem.IS_WINDOWS) {
			execFileStr = Directories.getInstallDir() + "/" + (Architecture.pickUp() == Architecture.BIT64 ? Constants.Modules.CREDITCARD_AUTHORIZATION_64BIT : Constants.Modules.CREDITCARD_AUTHORIZATION) + OperatingSystem.getCurrentExecutableExtension();
		} else {
			execFileStr = Directories.getInstallDir() + "/" + (Architecture.pickUp() == Architecture.BIT64 ? Constants.Modules.CREDITCARD_AUTHORIZATION_64BIT_S : Constants.Modules.CREDITCARD_AUTHORIZATION_S) + OperatingSystem.getCurrentExecutableExtension();
		}

		File execFile = new File(execFileStr);
		if(!execFile.exists()) {
			created=true;
			FileUtils.writeStringToFile(execFile, "");
		}
	}

	@AfterClass
	public static void tearDownClass() {
		if(created) {
			File execFile = new File(execFileStr);
			execFile.delete();
		}
	}

	@Test
	public void testCreditCardAuthorizationProcedure() throws Exception {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID);

		if(!OperatingSystem.pickUp().equals(OperatingSystem.WINDOWS)) {
			mapping.addSetting(new DefaultProcedureSetting(CreditCardAuthorizationProcedure.SETTING_IPC_MODE, IpcMode.Socket.name()));
		}

		CreditCardAuthorizationProcedure proc = new CreditCardAuthorizationProcedure(mapping);
		proc.stop();
	}

	@Test
	public void testIsOperatingCheckSupported() throws Exception {
		CreditCardAuthorizationProcedure ccap = new CreditCardAuthorizationProcedure(new DefaultProcedureMapping(
				Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID));

		assertTrue(ccap.isOperatingCheckSupported());
		assertFalse(ccap.isOperating());
	}

	@Ignore("Currently we do not have the C++ binaries available during testing")
	@Test
	public void testRun() throws Exception {
		CreditCardAuthorizationProcedure ccap = new CreditCardAuthorizationProcedure(new DefaultProcedureMapping(
				Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID));

		Feedback feed = ccap.run();
		try {
			assertNotNull(feed);
			assertEquals(Feedback.Neutral, feed);

			assertTrue(ccap.isOperatingCheckSupported());

			// TODO: currently this sometimes returns false for the first execution on Linux
			boolean ret = false;
			for(int i = 0;i < 10;i ++) {
				ret = ccap.isOperating();
				LOGGER.info("Had result: " + ret);
				if(ret) {
					break;
				}
			}
			assertTrue("Procedure should at least after some time be operating, but wasn't for 20 seconds", ret);
		} finally {
			ccap.stop();
		}
	}

	@Test
	public void testLoadDatabaseEnvArgs() throws Exception {
		EasyTravelConfig.read().databaseUrl = "jdbc:jtds:sqlserver://" + ENDPOINT + "/" + NAME + ";instance=dynasqlserver";
		EasyTravelConfig.read().creditCardAuthorizationEnvArgs = new String[] {
				"DT_WAIT=5",
				"RUXIT_WAIT=5"
		};

		CreditCardAuthorizationProcedure ccap = new CreditCardAuthorizationProcedure(new DefaultProcedureMapping(
				Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID));
		ccap.loadDatabaseEnvArgs((AbstractProcess)ccap.getProcess());

		Map<String,String> envMap = ccap.getProcess().getEnvironment();
		assertEquals(NAME, envMap.get(DatabaseEnvArgs.ET_CCA_DB_NAME));
		assertEquals(Vendor.SQLSERVER.getPreciseName(), envMap.get(DatabaseEnvArgs.ET_CCA_DB_VENDOR));
		assertEquals(ChannelType.TCP_IP.name(), envMap.get(DatabaseEnvArgs.ET_CCA_DB_CHANNEL_TYPE));
		assertEquals(ENDPOINT, envMap.get(DatabaseEnvArgs.ET_CCA_DB_CHANNEL_ENDPOINT));
		assertEquals("5", envMap.get("DT_WAIT"));
		assertEquals("5", envMap.get("RUXIT_WAIT"));

		EasyTravelConfig.read().creditCardAuthorizationEnvArgs = new String[] {
				"DT_WAIT=5",
				"RUXIT_WAIT=5",
				"ET_CCA_DB_VENDOR=TESTVENDOR",
				"ET_CCA_DB_CHANNEL_TYPE=TESTTYPE"
		};

		ccap = new CreditCardAuthorizationProcedure(new DefaultProcedureMapping(
				Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID));
		ccap.loadDatabaseEnvArgs((AbstractProcess)ccap.getProcess());

		envMap = ccap.getProcess().getEnvironment();
		assertEquals(NAME, envMap.get(DatabaseEnvArgs.ET_CCA_DB_NAME));
		assertEquals("TESTVENDOR", envMap.get(DatabaseEnvArgs.ET_CCA_DB_VENDOR));
		assertEquals("TESTTYPE", envMap.get(DatabaseEnvArgs.ET_CCA_DB_CHANNEL_TYPE));
		assertEquals(ENDPOINT, envMap.get(DatabaseEnvArgs.ET_CCA_DB_CHANNEL_ENDPOINT));
		assertEquals("5", envMap.get("DT_WAIT"));
		assertEquals("5", envMap.get("RUXIT_WAIT"));
	}

	@Test
	public void testAutoDetectIPCMode() throws Exception {
		DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID);

		CreditCardAuthorizationProcedure proc = new CreditCardAuthorizationProcedure(mapping);


		String ipc = proc.getMapping().getSettingValue(CreditCardAuthorizationProcedure.SETTING_IPC_MODE);
		if(OperatingSystem.IS_WINDOWS) {
			assertEquals("Had: " + proc.getMapping().getSettings(), IpcMode.NamedPipe.name(), ipc);
		} else {
			assertEquals("Had: " + proc.getMapping().getSettings(), IpcMode.Socket.name(), ipc);
		}
	}
}
