package com.dynatrace.easytravel.launcher.procedures;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration.tree.ConfigurationNode;
import org.junit.After;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.agent.DtAgentConfig;
import com.dynatrace.easytravel.launcher.config.ConfigurationReader;
import com.dynatrace.easytravel.launcher.config.NodeFactory;
import com.dynatrace.easytravel.launcher.engine.CorruptInstallationException;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.*;
import com.dynatrace.easytravel.utils.MockRESTServer.HTTPResponseRunnable;
import com.dynatrace.easytravel.utils.NanoHTTPD.Response;

import ch.qos.logback.classic.Level;

public class AbstractPluginServiceProcedureTest {
	private static final Logger LOGGER = LoggerFactory.make();

	private final AtomicReference<String> failed = new AtomicReference<String>("");

    static {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

		LOGGER.info("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

    @After
    public void tearDown() throws InterruptedException {
    	// ensure we do not leave a thread running in one of the tests
    	ThreadTestHelper.waitForThreadToFinish("Cyclic-Plugin-Manager-Thread");

    	// make sure we undo any changes to the in-memory config-settings
		EasyTravelConfig.resetSingleton();
	}


	@Test
	public void testNotifyStateChangedSimple() throws CorruptInstallationException {
		ProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);
		AbstractPluginServiceProcedure proc = new MockPluginServiceProcedure(mapping);

		// no-action state-changes
		proc.notifyProcedureStateChanged(null, null, State.STARTING);
		proc.notifyProcedureStateChanged(null, null, State.STOPPING);
	}

	private Response expectUriContains(String uri, String params, String contains, String paramsContain) {
		try {
			LOGGER.info("Had: " + uri + " and params " + params + ", expecting " + contains + " and " + paramsContain);
			assertTrue("Expected string '" + contains + "' not found in uri: " + uri,
					uri.contains(contains));
			assertTrue("Expected params '" + paramsContain + "' not found in params: " + params,
					paramsContain == null || params.contains(paramsContain));

			return new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "ok");
		} catch (Throwable e) {
			failed.set(failed.get() + "\n" + e.toString());
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testNotifyStateChanged() throws CorruptInstallationException, IOException, InterruptedException {
		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			int count = 0;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				count++;
				if(count == 1) {
					return expectUriContains(uri, parms.toString(), "/services/ConfigurationService/setPluginHosts", "host2"); // NanoHTTPD only returns the last param...
				} else if(count == 2) {
					return expectUriContains(uri, parms.toString(), "/services/ConfigurationService/getAllPluginNames", null);
				} else if (count == 3) {
					return expectUriContains(uri, parms.toString(), "/services/ConfigurationService/registerPlugins", "Setting1");
				} else if (count == 4) {
					return expectUriContains(uri, parms.toString(), "/services/ConfigurationService/setPluginEnabled", "Setting1");
				} else if (count == 5) {
					return expectUriContains(uri, parms.toString(), "/services/ConfigurationService/setPluginHosts", "host2"); // NanoHTTPD only returns the last param...
				} else {
					failed.set(failed.get() + "\n" + count + "-Unexpected uri: " + uri);
					return null;
				}
			}
		});

		try {
			EasyTravelConfig.read().backendPort = server.getPort();
			EasyTravelConfig.read().webServiceBaseDir="http://localhost:" + server.getPort() + "/services/";
			// ensure that PluginService is not used here
			EasyTravelConfig.read().pluginServiceHost = null;

			ProcedureMapping mapping = getMappingWithDefaultSettings();

			addNullHostSettings(mapping);

			AbstractPluginServiceProcedure proc = new MockPluginServiceProcedure(mapping);

			runStateChanges(proc);

			assertTrue("Should not have failed in the HTTPResponseRunnable, but did:" + failed.get(), failed.get().isEmpty());
		} finally {
			server.stop();
		}
	}

	private void runStateChanges(AbstractPluginServiceProcedure proc) throws InterruptedException {
		// no-action state-changes
		proc.notifyProcedureStateChanged(null, null, State.OPERATING);
		proc.notifyProcedureStateChanged(null, null, State.STOPPING);

		// ensure that the thread stops in between
		ThreadTestHelper.waitForThreadToFinish("Cyclic-Plugin-Manager-Thread");

		proc.notifyProcedureStateChanged(null, null, State.TIMEOUT);
		proc.notifyProcedureStateChanged(null, null, State.STOPPING);
	}

	private void addNullHostSettings(ProcedureMapping mapping) {
		// these should not cause an error
		mapping.addSetting(new MockProcedureSetting(Constants.Misc.SETTING_TYPE_PLUGIN_HOSTS, "plugin2", null));
		mapping.addSetting(new MockProcedureSetting(Constants.Misc.SETTING_TYPE_PLUGIN_HOSTS, null, "value"));
	}

	@Test
	public void testNotifyStateChangedWithPluginHost() throws CorruptInstallationException, IOException, InterruptedException {
		MockRESTServer server = new MockRESTServer(new HTTPResponseRunnable() {
			int count = 0;
			@Override
			public Response run(String uri, String method, Properties header, Properties parms) {
				count++;
				System.out.println("HEADERS: "+header.toString());
				System.out.println("PARMS: "+parms.toString());
				if(count == 1) {
					return expectUriContains(uri, parms.toString(), "/PluginService/setPluginHosts/plugin1", "host1");
				} else if(count == 2) {
					return expectUriContains(uri, parms.toString(), "/PluginService/setPluginHosts/plugin1", "host1");
				} else {
					failed.set(failed.get() + "\nUnexpected uri: " + uri);
					return null;
				}
			}


		});

		try {
			EasyTravelConfig.read().pluginServiceHost = "localhost";
			EasyTravelConfig.read().pluginServicePort = server.getPort();

			ProcedureMapping mapping = getMappingWithDefaultSettings();

			addNullHostSettings(mapping);

			AbstractPluginServiceProcedure proc = new MockPluginServiceProcedure(mapping);

			runStateChanges(proc);

			assertTrue("Should not have failed in the HTTPResponseRunnable, but did:" + failed.get(), failed.get().isEmpty());
		} finally {
			server.stop();
		}
	}

	private ProcedureMapping getMappingWithDefaultSettings() {
		ProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);

		mapping.addSetting(new DefaultProcedureSetting(Constants.Misc.SETTING_TYPE_PLUGIN, "Setting1", Constants.Misc.SETTING_VALUE_ON));
		mapping.addSetting(new DefaultProcedureSetting(Constants.Misc.SETTING_TYPE_PLUGIN, "Setting2", Constants.Misc.SETTING_VALUE_OFF));
		mapping.addSetting(new DefaultProcedureSetting(Constants.Misc.SETTING_TYPE_PLUGIN_HOSTS, "plugin1", "host1,host2"));
		return mapping;
	}

	@Test
	public void testNotifyStateChangedNoSettings() throws CorruptInstallationException, IOException, InterruptedException {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "ok");

		try {
			EasyTravelConfig.read().backendPort = server.getPort();
			EasyTravelConfig.read().webServiceBaseDir="http://localhost:" + server.getPort() + "/services/";

			ProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);

			AbstractPluginServiceProcedure proc = new MockPluginServiceProcedure(mapping);

			runStateChanges(proc);
		} finally {
			server.stop();
		}
	}

	@Test
	public void testNotifyStateChangedException() throws CorruptInstallationException, IOException, InterruptedException {
		MockRESTServer server = new MockRESTServer(NanoHTTPD.HTTP_BADREQUEST, NanoHTTPD.MIME_PLAINTEXT, "ok");

		try {
			EasyTravelConfig.read().backendPort = server.getPort();
			EasyTravelConfig.read().webServiceBaseDir="http://localhost:" + server.getPort() + "/services/";

			ProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);

			mapping.addSetting(new DefaultProcedureSetting(Constants.Misc.SETTING_TYPE_PLUGIN_HOSTS, "plugin1", "host1,host2"));

			AbstractPluginServiceProcedure proc = new MockPluginServiceProcedure(mapping);

			runStateChanges(proc);
		} finally {
			server.stop();
		}
	}

	@Test
	public void testIsTransferable() throws CorruptInstallationException {
		ProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);
		AbstractPluginServiceProcedure proc = new MockPluginServiceProcedure(mapping);

		assertFalse(proc.isTransferable(new DefaultProcedureSetting(null, "name", "value")));
		assertTrue(proc.isTransferable(new DefaultProcedureSetting(Constants.Misc.SETTING_TYPE_PLUGIN, "name", "value")));

        DefaultProcedureSetting procedureSetting = new DefaultProcedureSetting(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG, "RUXIT_CLUSTER_ID", "eT_PluginService");

        AbstractPluginServiceProcedure proc2 = new MockPluginServiceProcedure(new DefaultProcedureMapping(Constants.Procedures.PLUGIN_SERVICE).addSetting(procedureSetting));

        assertTrue(proc2.isTransferable(new DefaultProcedureSetting(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG, "RUXIT_CLUSTER_ID", "eT_PluginService")));

        assertFalse(proc2.isTransferable(new DefaultProcedureSetting(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG, "RUXIT_CLUSTER_ID", "eT_PluginService_xxx")));

        assertFalse(proc2.isTransferable(new DefaultProcedureSetting("xxx")));

        AbstractPluginServiceProcedure proc3 = new MockPluginServiceProcedure(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID).addSetting(procedureSetting));

        assertFalse(proc3.isTransferableTo(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID)));
	}

	@Test
	public void testTransfer() throws CorruptInstallationException {
		ProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);
		AbstractPluginServiceProcedure proc = new MockPluginServiceProcedure(mapping);

		proc.transfer(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID), State.STARTING);
		proc.transfer(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID), State.OPERATING);
		proc.transfer(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID), State.TIMEOUT);
	}

	@Test
	public void testTransferWithPlugins() throws CorruptInstallationException {
		ProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);
		mapping.addSetting(new DefaultProcedureSetting(Constants.Misc.SETTING_TYPE_PLUGIN, "Setting1", Constants.Misc.SETTING_VALUE_ON));
		mapping.addSetting(new DefaultProcedureSetting(Constants.Misc.SETTING_TYPE_PLUGIN, "Setting2", Constants.Misc.SETTING_VALUE_OFF));
		mapping.addSetting(new DefaultProcedureSetting(Constants.Misc.SETTING_TYPE_PLUGIN, "DotNetPaymentService", Constants.Misc.SETTING_VALUE_OFF));
		mapping.addSetting(new DefaultProcedureSetting(Constants.Misc.SETTING_TYPE_PLUGIN, "Setting3", "SomeOtherValue"));

		AbstractPluginServiceProcedure proc = new MockPluginServiceProcedure(mapping);

		proc.transfer(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID), State.STARTING);
		proc.transfer(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID), State.OPERATING);


		DefaultProcedureMapping mapping2 = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);
		mapping2.addSetting(new DefaultProcedureSetting(Constants.Misc.SETTING_TYPE_PLUGIN, "DotNetPaymentService", Constants.Misc.SETTING_VALUE_ON));
		proc.transfer(mapping2, State.TIMEOUT);

		proc.notifyProcedureStateChanged(null, null, State.STOPPING);
	}

	@Test
	public void testWithDifferentLoglevel() {
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {
			@Override
			public void run() {
				try {
					testTransfer();
					tearDown();
					testTransferWithPlugins();
					tearDown();
					testNotifyStateChangedSimple();
				} catch (CorruptInstallationException e) {
					throw new IllegalStateException(e);
				} catch (InterruptedException e) {
					throw new IllegalStateException(e);
				}
			}
		}, AbstractPluginServiceProcedure.class.getName(), Level.DEBUG);
	}

	private final class MockProcedureSetting implements ProcedureSetting {
		private final String type;
		private final String name;
		private final String value;

		public MockProcedureSetting(String type, String name, String value) {
			super();
			this.type = type;
			this.name = name;
			this.value = value;
		}

		@Override
		public ProcedureSetting copy() {
			return new MockProcedureSetting(type, name, value);
		}

		@Override
		public void write(ConfigurationNode node, NodeFactory factory) {
		}

		@Override
		public void read(ConfigurationNode node, ConfigurationReader reader) throws ConfigurationException {
		}

		@Override
		public String toREST() {
			return null;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public String getType() {
			return type;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int getStayOnDuration() {
			return 0;
		}

		@Override
		public int getStayOffDuration() {
			return 0;
		}
	}

	private final class MockPluginServiceProcedure extends AbstractPluginServiceProcedure {

		private MockPluginServiceProcedure(ProcedureMapping mapping) throws CorruptInstallationException {
			super(mapping);
		}

		@Override
		public boolean isOperatingCheckSupported() {
			return false;
		}

		@Override
		public boolean isOperating() {
			return false;
		}

		@Override
		public boolean hasLogfile() {
			return false;
		}

		@Override
		public String getLogfile() {
			return null;
		}

		@Override
		protected DtAgentConfig getAgentConfig() {
			return null;
		}

		@Override
		protected String getWorkingDir() {
			return null;
		}

		@Override
		protected String getModuleJar() {
			return Constants.Modules.PLUGIN_SERVICE;
		}

		@Override
		protected String[] getJavaOpts() {
			return null;
		}
	}
}
