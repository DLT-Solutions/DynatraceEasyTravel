package com.dynatrace.easytravel.launcher.baseload;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.dynatrace.diagnostics.uemload.http.base.HostAvailability;
import com.dynatrace.easytravel.config.CustomerTrafficScenarioEnum;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.constants.BaseConstants.UrlType;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.*;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.dynatrace.easytravel.utils.NanoHTTPD;

public class BaseLoadTest {
	private static final HostAvailability instance = HostAvailability.INSTANCE;

	private MockRESTServer serverC;
	private MockRESTServer serverA;

	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();

		Logger.getLogger(BaseConstants.EMPTY_STRING).setLevel(Level.FINE);
		Logger.getLogger(HostAvailability.class.getName()).setLevel(Level.FINE);
	}	

	@Test
	@Ignore("Need to fix base code before this test works!")
	public void testBaseLoadSTARTINGtoOPERATING() throws Exception {
		BaseLoad load = new CustomerBaseLoad(CustomerTrafficScenarioEnum.EasyTravel,10, 1, false);

		load.setValue(1);
		load.setValue(2);

		load.setManualVisits(1);
		load.setManualVisits(2);

		serverC = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		serverA = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");

		// all are available currently
		assertTrue(instance.isHostAvailable("http://localhost:" + serverA.getPort()));
		assertTrue(instance.isHostAvailable("http://localhost:8091"));
		assertTrue(instance.isHostAvailable("http://localhost:" + serverC.getPort()));

		assertFalse(load.hasHost());

		try {
			StatefulProcedure aproc = new StatefulProcedure(new MockProcedure(
					new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_ID), "http://localhost:" + serverA.getPort()));
			StatefulProcedure cproc = new StatefulProcedure(new MockProcedure(
					new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID), "http://localhost:" + serverC.getPort()));
			StatefulProcedure bproc = new StatefulProcedure(new MockProcedure(
					new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID), "http://localhost:8091"));

			load.notifyProcedureStateChanged(bproc, State.STOPPED, State.STARTING);
			load.notifyProcedureStateChanged(cproc, State.STOPPED, State.STARTING);
			load.notifyProcedureStateChanged(aproc, State.STOPPED, State.STARTING);

			load.notifyProcedureStateChanged(bproc, State.STARTING, State.OPERATING);
			load.notifyProcedureStateChanged(cproc, State.STARTING, State.OPERATING);
			load.notifyProcedureStateChanged(aproc, State.STARTING, State.OPERATING);

			Thread.sleep(500);	// background task!

			assertTrue(load.hasHost());

			// now check which of the urls are now "unavailable"
			assertTrue(instance.isHostAvailable("http://localhost:" + serverA.getPort()));
			assertTrue(instance.isHostAvailable("http://localhost:8091"));
			assertFalse("Direct access to Customer frontend should now be avoided and thus it should be listed as not available (expected)",
					instance.isHostAvailable("http://localhost:" + serverC.getPort()));

			// now shut down again
			load.notifyProcedureStateChanged(aproc, State.OPERATING, State.STOPPING);
			load.notifyProcedureStateChanged(cproc, State.OPERATING, State.STOPPING);
			load.notifyProcedureStateChanged(bproc, State.OPERATING, State.STOPPING);

			load.notifyProcedureStateChanged(aproc, State.STOPPING, State.STOPPED);
			load.notifyProcedureStateChanged(cproc, State.STOPPING, State.STOPPED);
			load.notifyProcedureStateChanged(bproc, State.STOPPING, State.STOPPED);

			assertTrue(load.stop(true));

			// all are unavailable now
			assertFalse(instance.isHostAvailable("http://localhost:" + serverA.getPort()));
			assertFalse(instance.isHostAvailable("http://localhost:8091"));
			assertFalse(instance.isHostAvailable("http://localhost:" + serverC.getPort()));
		} finally {
			assertNotNull(instance.setAvailable("http://localhost:" + serverA.getPort()));
			assertNotNull(instance.setAvailable("http://localhost:" + serverC.getPort()));
			serverC.stop();
			serverA.stop();
		}
	}

	@Test
	@Ignore("Need to fix base code before this test works!")
	public void testBaseLoadApacheFirstStarting() throws Exception {
		BaseLoad load = new CustomerBaseLoad(CustomerTrafficScenarioEnum.EasyTravel, 10, 2.23, false);

		load.setValue(1);
		load.setValue(2);

		load.setManualVisits(1);
		load.setManualVisits(2);

		serverC = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");
		serverA = new MockRESTServer(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_PLAINTEXT, "response");

		// all are available currently
		assertTrue(instance.isHostAvailable("http://localhost:" + serverA.getPort()));
		assertTrue(instance.isHostAvailable("http://localhost:8091"));
		assertTrue(instance.isHostAvailable("http://localhost:" + serverC.getPort()));

		assertFalse(load.hasHost());

		try {
			StatefulProcedure aproc = new StatefulProcedure(new MockProcedure(
					new DefaultProcedureMapping(Constants.Procedures.APACHE_HTTPD_ID), "http://localhost:" + serverA.getPort()));
			StatefulProcedure cproc = new StatefulProcedure(new MockProcedure(
					new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID), "http://localhost:" + serverC.getPort()));
			StatefulProcedure bproc = new StatefulProcedure(new MockProcedure(
					new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID), "http://localhost:8091"));

			load.notifyProcedureStateChanged(aproc, State.STOPPED, State.STARTING);
			load.notifyProcedureStateChanged(bproc, State.STOPPED, State.STARTING);
			load.notifyProcedureStateChanged(cproc, State.STOPPED, State.STARTING);

			load.notifyProcedureStateChanged(aproc, State.STARTING, State.OPERATING);
			load.notifyProcedureStateChanged(bproc, State.STARTING, State.OPERATING);
			load.notifyProcedureStateChanged(cproc, State.STARTING, State.OPERATING);

			Thread.sleep(500);	// background task!

			assertTrue(load.hasHost());

			// now check which of the urls are now "unavailable"
			assertTrue(instance.isHostAvailable("http://localhost:" + serverA.getPort()));
			assertTrue(instance.isHostAvailable("http://localhost:8091"));
			assertFalse("Direct access to Customer frontend should now be avoided and thus it should be listed as not available (expected)",
					instance.isHostAvailable("http://localhost:" + serverC.getPort()));

			// now shut down again
			load.notifyProcedureStateChanged(cproc, State.OPERATING, State.STOPPING);
			load.notifyProcedureStateChanged(bproc, State.OPERATING, State.STOPPING);
			load.notifyProcedureStateChanged(aproc, State.OPERATING, State.STOPPING);

			load.notifyProcedureStateChanged(cproc, State.STOPPING, State.STOPPED);
			load.notifyProcedureStateChanged(bproc, State.STOPPING, State.STOPPED);
			load.notifyProcedureStateChanged(aproc, State.STOPPING, State.STOPPED);

			assertTrue(load.stop(true));

			// all are unavailable now
			assertFalse(instance.isHostAvailable("http://localhost:" + serverA.getPort()));
			assertFalse(instance.isHostAvailable("http://localhost:8091"));
			assertFalse(instance.isHostAvailable("http://localhost:" + serverC.getPort()));
		} finally {
			assertNotNull(instance.setAvailable("http://localhost:" + serverA.getPort()));
			assertNotNull(instance.setAvailable("http://localhost:" + serverC.getPort()));
			serverA.stop();
			serverC.stop();
		}
	}

	@Test
	public void testLogging() {
		BaseLoad load = new CustomerBaseLoad(CustomerTrafficScenarioEnum.EasyTravel, 10, 2.23, false);
		assertTrue(load.isSimulatorLogging());

		load.setSimulatorLoggingOff();
		assertFalse(load.isSimulatorLogging());

		load.setSimulatorLoggingOn();
		assertTrue(load.isSimulatorLogging());
	}

	@Test
	public void testRatio() {
		BaseLoad load = new CustomerBaseLoad(CustomerTrafficScenarioEnum.EasyTravel, 10, 2.23, false);

		assertEquals(2.23, load.getRatio(), 0.01);

		load.setRatio(1.1);
		assertEquals(1.1, load.getRatio(), 0.01);
	}

	@Test
	public void testTaggedWebrequests() {
		BaseLoad load = new CustomerBaseLoad(CustomerTrafficScenarioEnum.EasyTravel, 10, 2.23, false);
		assertFalse(load.isTaggedWebRequest());

		load.setTaggeWebRequest(true);
		assertTrue(load.isTaggedWebRequest());
	}

	@Test
	public void testVarious() {
		BaseLoad load = new CustomerBaseLoad(CustomerTrafficScenarioEnum.EasyTravel, 10, 2.23, false);
		load.disableBlocking();
		assertFalse("Scheduler is blocked", load.isSchedulingBlocked());
		load.pluginsChanged();
	}
	
    @Test
    public void testGetUriDNS() {
        StatefulProcedure mock = Mockito.mock(StatefulProcedure.class);
        when(mock.getMapping()).thenReturn(new DefaultProcedureMapping(Constants.Procedures.CUSTOMER_FRONTEND_ID));
        BaseLoad load = new CustomerBaseLoad(CustomerTrafficScenarioEnum.EasyTravel,10, 1, false);
        load.notifyProcedureStateChanged(mock, State.STOPPED, State.STOPPED);
        verify(mock).getURIDNS();
    }

	private class MockProcedure extends AbstractProcedure {

		private final String uri;

		private MockProcedure(ProcedureMapping mapping, String uri) throws IllegalArgumentException {
			super(mapping);

			this.uri = uri;
		}

		@Override
		public void removeStopListener(StopListener stopListener) {

		}

		@Override
		public void clearStopListeners() {

		}

		@Override
		public void addStopListener(StopListener stopListener) {

		}

		@Override
		public Feedback stop() {
			return Feedback.Neutral;
		}

		@Override
		public Feedback run() {
			return Feedback.Neutral;
		}

		@Override
		public boolean isStoppable() {
			return true;
		}

		@Override
		public StopMode getStopMode() {
			return StopMode.SEQUENTIAL;
		}

		@Override
		public boolean isRunning() {
			return false;
		}

		@Override
		public boolean isOperatingCheckSupported() {
			return true;
		}

		@Override
		public boolean isOperating() {
			return true;
		}

		@Override
		public String getLogfile() {
			return null;
		}

		@Override
		public boolean hasLogfile() {
		    return false;
		}

		@Override
		public String getDetails() {
			return null;
		}

		@Override
		public Technology getTechnology() {
			return Technology.MYSQL;
		}

		@Override
		public boolean agentFound() {
			return false;
		}

		@Override
		public String getURI() {
			return uri;
		}

		@Override
		public String getURI(UrlType urlType) {
			return uri;
		}
	}
}
