package com.dynatrace.easytravel.launcher.remote;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.OperatingSystem;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.Procedure;
import com.dynatrace.easytravel.launcher.engine.SingleProcedureBatch;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.CustomerFrontendProcedure;
import com.dynatrace.easytravel.launcher.procedures.utils.CentralTechnologyActivator;
import com.dynatrace.easytravel.launcher.process.AbstractProcess;
import com.dynatrace.easytravel.launcher.process.AbstractProcessTest;
import com.dynatrace.easytravel.launcher.process.Process;
import com.dynatrace.easytravel.launcher.scenarios.*;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.misc.RESTConstants;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.TestHelpers;

public class RESTProcedureControlTest {
    private static final Logger LOGGER = LoggerFactory.make();

	// Note: some of the features of this class are already tested in tests for RESTProcedureClient and RemoteProcedure!!

	@BeforeClass
	public static void setUpClass() throws IOException {
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");
	}

	@Test
	public void testPrepare() {
		RESTProcedureControl contr = new RESTProcedureControl();
		String uuid = contr.prepare(Constants.Procedures.INPROCESS_DBMS_ID,
				null, null, null, null).split("\\|")[0];
		assertNotNull(uuid);
		assertNotNull("Had: " + uuid, UUID.fromString(uuid));
	}

    @Test
    public void testPrepareClassicMode() {
        RESTProcedureControl contr = new RESTProcedureControl();
        String uuid = contr.prepare(Constants.Procedures.INPROCESS_DBMS_ID,
                null, null, null, InstallationType.Classic).split("\\|")[0];

        assertNotNull(uuid);
        assertNotNull("Had: " + uuid, UUID.fromString(uuid));
        assertEquals(InstallationType.Classic.name(), DtVersionDetector.getInstallationType().name());
    }

    @Test
    public void testPrepareAPMMode() {
        RESTProcedureControl contr = new RESTProcedureControl();
        String uuid = contr.prepare(Constants.Procedures.INPROCESS_DBMS_ID,
                null, null, null, InstallationType.APM).split("\\|")[0];

        assertNotNull(uuid);
        assertNotNull("Had: " + uuid, UUID.fromString(uuid));
        assertEquals(InstallationType.APM.name(), DtVersionDetector.getInstallationType().name());
    }

	@Test
	public void testPrepareProperties() {
		RESTProcedureControl contr = new RESTProcedureControl();
		String uuid = contr.prepare(Constants.Procedures.INPROCESS_DBMS_ID,
				Collections.singletonList("config.frontendAgent" + Constants.REST.PROPERTY_DELIMITER + "myvalue"), null, null, null).split("\\|")[0];
		assertNotNull(uuid);
		assertNotNull(UUID.fromString(uuid));

		assertEquals("myvalue", EasyTravelConfig.read().frontendAgent);
	}

	@Test
	public void testPreparePropertiesEmpty() {
		RESTProcedureControl contr = new RESTProcedureControl();
		String uuid = contr.prepare(Constants.Procedures.INPROCESS_DBMS_ID,
				Collections.singletonList("config.frontendAgent"), null, null, null).split("\\|")[0];
		assertNotNull(uuid);
		assertNotNull(UUID.fromString(uuid));

		assertEquals("", EasyTravelConfig.read().frontendAgent);
	}

	@Test
	public void testPrepareSettings() {
		RESTProcedureControl contr = new RESTProcedureControl();
		String uuidStr = contr.prepare(Constants.Procedures.INPROCESS_DBMS_ID,
				null, Collections.singletonList(new DefaultProcedureSetting("mytype", "myname", "myvalue")), null, null).split("\\|")[0];
		assertNotNull(uuidStr);
		UUID uuid = UUID.fromString(uuidStr);
		assertNotNull(uuid);

		SingleProcedureBatch batch = RESTProcedureControl.getBatches().get(uuid);
		assertNotNull(batch);
		assertEquals("myvalue", batch.getProcedure().getMapping().getSettingValue("mytype", "myname"));
	}

	@Test
	public void testPrepareSettingsWithStayOnOff() {
		RESTProcedureControl contr = new RESTProcedureControl();
		DefaultProcedureSetting origSetting = new DefaultProcedureSetting("mytype", "myname", "myvalue", 15, 20);
		assertEquals(15, origSetting.getStayOnDuration());
		assertEquals(20, origSetting.getStayOffDuration());

		String uuidStr = contr.prepare(Constants.Procedures.INPROCESS_DBMS_ID,
				null, Collections.singletonList(origSetting), null, null).split("\\|")[0];
		assertNotNull(uuidStr);
		UUID uuid = UUID.fromString(uuidStr);
		assertNotNull(uuid);

		SingleProcedureBatch batch = RESTProcedureControl.getBatches().get(uuid);
		assertNotNull(batch);
		ProcedureMapping mapping = batch.getProcedure().getMapping();
		assertEquals("myvalue", mapping.getSettingValue("mytype", "myname"));
		assertEquals(1, mapping.getSettings().size());
		ProcedureSetting newSetting = mapping.getSettings().iterator().next();

		assertEquals(origSetting, newSetting);
		assertEquals(origSetting.getStayOffDuration(), newSetting.getStayOffDuration());
		assertEquals(origSetting.getStayOnDuration(), newSetting.getStayOnDuration());
		assertEquals(origSetting.getValue(), newSetting.getValue());

		assertEquals(15, newSetting.getStayOnDuration());
		assertEquals(20, newSetting.getStayOffDuration());
	}

	@Test
	public void testPrepareEnvironment() {
		System.clearProperty("testenv");

		RESTProcedureControl contr = new RESTProcedureControl();
		String uuidStr = contr.prepare(Constants.Procedures.INPROCESS_DBMS_ID,
				null, null, Collections.singletonList("testenv" + Constants.REST.PROPERTY_DELIMITER + "testval"),null).split("\\|")[0];

		assertNotNull(uuidStr);
		UUID uuid = UUID.fromString(uuidStr);
		assertNotNull(uuid);

		assertEquals("testval", System.getProperty("testenv"));

		/*
Collection<Setting> customSettings = mapping.getCustomSettings();
             if (customSettings != null) {
              HashMap<String, String> configOverrides = new HashMap<String, String>();
              for (Setting setting : customSettings) {
            configOverrides.put(setting.getName(), setting.getValue());
           }
                 config.applyCustomSettings(configOverrides);
             }
             		 *
		 */
	}

	@Test
	public void testPrepareCustomSettings() {
		RESTProcedureControl contr = new RESTProcedureControl();
		String uuidStr = contr.prepare(Constants.Procedures.INPROCESS_DBMS_ID,
				null, Collections.singletonList(
						new DefaultProcedureSetting(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG, "config.backendAgent", "testvalue2")),
						null,null).split("\\|")[0];
		assertNotNull(uuidStr);
		UUID uuid = UUID.fromString(uuidStr);
		assertNotNull(uuid);

		SingleProcedureBatch batch = RESTProcedureControl.getBatches().get(uuid);
		assertNotNull(batch);

		// check that the procedure has the
		Collection<Setting> customSettings = batch.getProcedure().getMapping().getCustomSettings();
		assertNotNull(customSettings);
		assertEquals("Had: " + customSettings, 1, customSettings.size());
		assertEquals("config.backendAgent", customSettings.iterator().next().getName());
		assertEquals("testvalue2", customSettings.iterator().next().getValue());

		assertEquals("testvalue2", EasyTravelConfig.read().backendAgent);
	}

	@Test
	public void testPrepareCustomSettingsCustomerFrontend() throws Exception {
		RESTProcedureControl contr = new RESTProcedureControl();
		String uuidStr = contr.prepare(Constants.Procedures.CUSTOMER_FRONTEND_ID,
				null, Collections.singletonList(
						new DefaultProcedureSetting(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG, "config.backendAgent", "testvalue2")),
						null,null).split("\\|")[0];
		assertNotNull(uuidStr);
		UUID uuid = UUID.fromString(uuidStr);
		assertNotNull(uuid);

		SingleProcedureBatch batch = RESTProcedureControl.getBatches().get(uuid);
		assertNotNull(batch);

		// check that the procedure has the
		Collection<Setting> customSettings = batch.getProcedure().getMapping().getCustomSettings();
		assertNotNull(customSettings);
		assertEquals("Had: " + customSettings, 1, customSettings.size());
		assertEquals("config.backendAgent", customSettings.iterator().next().getName());
		assertEquals("testvalue2", customSettings.iterator().next().getValue());

		assertEquals("testvalue2", EasyTravelConfig.read().backendAgent);

		Procedure proc = batch.getProcedure().getDelegate();
		CustomerFrontendProcedure cproc = (CustomerFrontendProcedure) proc;
		Process process = cproc.getProcess();
		List<String> args = AbstractProcessTest.getApplicationArguments((AbstractProcess) process);
		assertTrue("Had: " + args, args.size() >= 2);

		LOGGER.info("Having: " + args);
		boolean found = false;
		for(int i = 0;i < args.size()-1;i++) {
			if(args.get(i).equals(BaseConstants.MINUS + Constants.CmdArguments.PROPERTY_FILE)) {
				String propertiesFilePath = args.get(i+1);

				String contents = FileUtils.readFileToString(new File(propertiesFilePath));
				assertTrue(contents.contains("config.backendAgent=testvalue2"));

				EasyTravelConfig.resetSingleton();
				EasyTravelConfig.createSingleton(propertiesFilePath);

				// still need to have the value set after reading the file
				assertEquals("testvalue2", EasyTravelConfig.read().backendAgent);

				found = true;

				break;
			}
		}

		assertTrue(found);
	}

	@Test
	public void testStartStopStatus() {
		// prepare Procedures/uuids
		RESTProcedureControl contr = new RESTProcedureControl();
		String uuidStr = contr.prepare(Constants.Procedures.INPROCESS_DBMS_ID,
				null, null, null,null).split("\\|")[0];

		assertNotNull(uuidStr);
		UUID uuid = UUID.fromString(uuidStr);
		assertNotNull(uuid);

		// invalid UUID
		String result = contr.start(UUID.randomUUID().toString());
		assertTrue("Had: " + result, result.startsWith("UNKNOWN"));
		result = contr.status(UUID.randomUUID().toString());
		assertTrue("Had: " + result, result.startsWith("UNKNOWN"));
		result = contr.stop(UUID.randomUUID().toString());
		assertTrue("Had: " + result, result.startsWith("NOTOK"));

		// initial status
		result = contr.status(uuidStr);
		assertEquals("Had: " + result, State.STOPPED.name(), result);
		assertTrue("Should find UUID in statusALL", contr.statusAll().contains(uuidStr));
		result = contr.stop(uuidStr);
		assertTrue("Had: " + result, result.startsWith("NOTOK"));

		// start it
		result = contr.start(uuidStr);
		assertEquals("Had: " + result, State.OPERATING.name(), result);
		result = contr.status(uuidStr);
		assertEquals("Had: " + result, State.OPERATING.name(), result);

		// second time fails
		result = contr.start(uuidStr);
		assertEquals("Had: " + result, State.FAILED.name(), result);

		assertEquals("OK", contr.stop(uuidStr));

		result = contr.status(uuidStr);
		assertEquals("Had: " + result, State.STOPPED.name(), result);

		result = contr.start(uuidStr);
		assertEquals("Had: " + result, State.OPERATING.name(), result);
		RESTProcedureControl.stopAll();

		result = contr.status(uuidStr);
		assertEquals("Had: " + result, State.STOPPED.name(), result);

		RESTProcedureControl.stopAll();
	}

	@Test
	public void testLog() {
		RESTProcedureControl contr = new RESTProcedureControl();
		String uuidStr = contr.prepare(Constants.Procedures.INPROCESS_DBMS_ID,
				null, null, null,null).split("\\|")[0];

		assertNotNull(uuidStr);
		UUID uuid = UUID.fromString(uuidStr);
		assertNotNull(uuid);

		String result = contr.log(UUID.randomUUID().toString());
		assertTrue("Had: " + result, result.startsWith("UNKNOWN"));


		result = contr.log(uuidStr);
		assertEquals("Procedure does not provide log", result);
	}

	@Test
	public void testDetails() {
		RESTProcedureControl contr = new RESTProcedureControl();
		String uuidStr = contr.prepare(Constants.Procedures.INPROCESS_DBMS_ID,
				null, null, null,null).split("\\|")[0];

		assertNotNull(uuidStr);
		UUID uuid = UUID.fromString(uuidStr);
		assertNotNull(uuid);

		String result = contr.details(UUID.randomUUID().toString());
		assertTrue("Had: " + result, result.startsWith("UNKNOWN"));


		result = contr.details(uuidStr);
		assertEquals("Database running at: localhost:1527", result);
	}

	@Test
	public void testTechnology() {
		RESTProcedureControl contr = new RESTProcedureControl();
		String uuidStr = contr.prepare(Constants.Procedures.INPROCESS_DBMS_ID,
				null, null, null,null).split("\\|")[0];

		assertNotNull(uuidStr);
		UUID uuid = UUID.fromString(uuidStr);
		assertNotNull(uuid);

		String result = contr.technology(UUID.randomUUID().toString());
		assertTrue("Had: " + result, result.startsWith("UNKNOWN"));


		result = contr.technology(uuidStr);
		assertEquals("", result);
	}

	@Test
	public void testAgentFound() {
		RESTProcedureControl contr = new RESTProcedureControl();
		String uuidStr = contr.prepare(Constants.Procedures.INPROCESS_DBMS_ID,
				null, null, null,null).split("\\|")[0];

		assertNotNull(uuidStr);
		UUID uuid = UUID.fromString(uuidStr);
		assertNotNull(uuid);

		String result = contr.agentFound(UUID.randomUUID().toString());
		assertEquals("Had: " + result, "false", result);


		result = contr.agentFound(uuidStr);
		assertEquals("false", result);
	}

	@Test
	public void testIsInstrumentationSupported() {
		RESTProcedureControl contr = new RESTProcedureControl();
		String uuidStr = contr.prepare(Constants.Procedures.INPROCESS_DBMS_ID,
				null, null, null,null).split("\\|")[0];

		assertNotNull(uuidStr);
		UUID uuid = UUID.fromString(uuidStr);
		assertNotNull(uuid);

		String result = contr.isInstrumentationSupported(UUID.randomUUID().toString());
		assertEquals("Had: " + result, "false", result);

		result = contr.isInstrumentationSupported(uuidStr);
		assertEquals("false", result);
	}

	@Test
	public void testDCRUM() {
		RESTProcedureControl contr = new RESTProcedureControl();

		Response result = contr.checkDCRumAvailability("somecmd");
		TestHelpers.assertContains(result.getEntity().toString(), "not supported", "cmd=somecmd");

		result = contr.getDCRumData("somecmd");
		TestHelpers.assertContains(result.getEntity().toString(), "Cannot provide emulated DC-RUM appliance data");
	}

	@Test
	public void testVersion() {
		RESTProcedureControl contr = new RESTProcedureControl();
		assertTrue(StringUtils.isNotEmpty(contr.getVersion()));
	}

	@Test
	public void testDotNetEnabled() {
		RESTProcedureControl contr = new RESTProcedureControl();
		String dotNetEnabled = OperatingSystem.isCurrent(OperatingSystem.WINDOWS) ? "true" : "false";  
		assertEquals(dotNetEnabled, contr.isDotNetEnabled());

		CentralTechnologyActivator.getIntance().getActivator(Technology.DOTNET_20).disable();
		assertEquals("false", contr.isDotNetEnabled());

		CentralTechnologyActivator.getIntance().getActivator(Technology.DOTNET_20).enable();
		assertEquals("true", contr.isDotNetEnabled());
	}

	@Ignore("Test ignored. It seems that for testing .Net procedure integration tests would be better choice")
	@Test
	public void testDotNetFrontendIISServer() throws IOException {
		EasyTravelConfig config = null;

		RESTProcedureControl contr = new RESTProcedureControl();
		String uuidStr = contr.prepare(Constants.Procedures.B2B_FRONTEND_ID, null, null, null, null).split("\\|")[0];

		assertFalse("ISS server not should be not available", Boolean.valueOf(contr.isRunningOnIIS(uuidStr)));

		MockRESTServer server = new MockRESTServer(new MockRESTServer.HTTPResponseRunnable() {
			@Override
			public NanoHTTPD.Response run(String uri, String method, Properties header, Properties parms) {
				NanoHTTPD.Response resp = new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "ok");
				resp.addHeader("Server","IIS");
				return resp;
			}
		});

		try {
			config = EasyTravelConfig.read();
			config.b2bFrontendPageToIdentify = "";
			config.b2bFrontendPortRangeStart = server.getPort();

			assertTrue("ISS server should be available", Boolean.valueOf(contr.isRunningOnIIS(uuidStr)));

		}  finally {
			server.stop();
			EasyTravelConfig.resetSingleton();
		}
	}

	@Ignore("Test ignored. It seems that for testing .Net procedure integration tests would be better choice")
	@Test
	public void testDotNetPaymentIISServer() throws IOException {
		EasyTravelConfig config = null;

		RESTProcedureControl contr = new RESTProcedureControl();
		String uuidStr = contr.prepare(Constants.Procedures.PAYMENT_BACKEND_ID, null, null, null, null).split("\\|")[0];

		assertFalse("ISS server not should be not available", Boolean.valueOf(contr.isRunningOnIIS(uuidStr)));

		MockRESTServer server = new MockRESTServer(new MockRESTServer.HTTPResponseRunnable() {
			@Override
			public NanoHTTPD.Response run(String uri, String method, Properties header, Properties parms) {
				NanoHTTPD.Response resp = new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "ok");
				resp.addHeader("Server","IIS");
				return resp;
			}
		});

		try {
			config = EasyTravelConfig.read();
			config.paymentBackendPageToIdentify= "";
			config.paymentBackendPort = server.getPort();

			assertTrue("ISS server should be available", Boolean.valueOf(contr.isRunningOnIIS(uuidStr)));

		}  finally {
			server.stop();
			EasyTravelConfig.resetSingleton();
		}
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructorRESTConstants() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(RESTConstants.class);
	}
}
