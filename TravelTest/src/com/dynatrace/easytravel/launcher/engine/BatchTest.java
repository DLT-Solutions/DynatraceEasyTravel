package com.dynatrace.easytravel.launcher.engine;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.procedures.DummyProcedure;
import com.dynatrace.easytravel.launcher.procedures.RemoteProcedure;
import com.dynatrace.easytravel.launcher.scenarios.*;
import com.dynatrace.easytravel.launcher.scenarios.builder.SettingBuilder;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.TestHelpers;
import com.dynatrace.easytravel.utils.ThreadTestHelper;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.net.httpserver.HttpServer;


public class BatchTest {
	private static final Logger LOGGER = LoggerFactory.make();

	private final AtomicReference<AssertionError> error = new AtomicReference<AssertionError>(null);
	private final static int oldTimeout = EasyTravelConfig.read().syncProcessTimeoutMs;

	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();
	}

	@After
	public void tearDown() {
		if(error.get() != null) {
			throw error.get();
		}
		EasyTravelConfig.read().syncProcessTimeoutMs=oldTimeout;
	}

	private static final int NUMBER_OF_THREADS = 4;
	private static final int NUMBER_OF_TESTS = 3000;

	@Test
	public void testBatch() {
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);
		assertNotNull(batch);
	}

	@Test
	public void testBatchNullMapping() {
		List<ProcedureStateListener> list = Collections.emptyList();
		DefaultScenario scenario = new DefaultScenario();
		scenario.addProcedureMapping(null);
		Batch batch = new Batch(scenario, list);
		batch.start();
		batch.stop();
	}

	@Test
	public void testTransfer() {
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);
		batch.transfer(new DefaultScenario(), list);
	}

	@Test
	public void testStart() {
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);
		batch.start();

		try {
			batch.start();
			fail("Should not allow to start twice");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "STOPPED", "OPERATING");
		}

		// stopping and then starting again works
		batch.stop();
		batch.start();
	}

	@Test
	public void testStop() {
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);

		try {
			batch.stop();
			fail("Should not allow stop without start()");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "OPERATING, STARTING or TIMEOUT", "STOPPED");
		}

		batch.start();

		batch.stop();
	}

	@Test
	public void testGetScenario() {
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);
		assertNotNull(batch.getScenario());
	}

	@Test
	public void testAddBatchStateListeners() {
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);
		Collection<BatchStateListener> batchStateListeners = new ArrayList<BatchStateListener>();
		batch.addBatchStateListeners(batchStateListeners);
	}

	@Test
	public void testGetState() {
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);
		assertEquals(State.STOPPED, batch.getState());
		batch.start();
		assertEquals(State.OPERATING, batch.getState());
	}

	@Test
	public void testGetProcedures() {
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);
		assertEquals(0, batch.getProcedures().size());
	}

	@Test
	public void testGetBatchStateListeners() {
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);
		assertEquals(0, batch.getBatchStateListeners().size());

		Collection<BatchStateListener> batchStateListeners = new ArrayList<BatchStateListener>();
		batchStateListeners.add(new BatchStateListener() {

			@Override
			public void notifyBatchStateChanged(Scenario scenario, State oldState, State newState) {
			}
		});
		batch.addBatchStateListeners(batchStateListeners);
		assertEquals(1, batch.getBatchStateListeners().size());
	}

	@Test
	public void testGetProcedureStateListeners() {
		List<ProcedureStateListener> list = new ArrayList<ProcedureStateListener>();
		Batch batch = new Batch(new DefaultScenario(), list);
		assertEquals(0, batch.getProcedureStateListeners().size());

		list.add(new ProcedureStateListener() {

			@Override
			public void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState) {
			}
		});
		batch = new Batch(new DefaultScenario(), list);
		assertEquals(1, batch.getProcedureStateListeners().size());
	}

	boolean called = false;
	State lastState = State.STOPPED;

	@Test
	public void testBatchBatchStateChanged() {
		List<ProcedureStateListener> list = new ArrayList<ProcedureStateListener>();
		Batch batch = new Batch(new DefaultScenario(), list);

		Collection<BatchStateListener> batchStateListeners = new ArrayList<BatchStateListener>();
		batchStateListeners.add(new BatchStateListener() {

			@Override
			public void notifyBatchStateChanged(Scenario scenario, State oldState, State newState) {
				// first from STOPPED to STARTING, to OPERATING, to STOPPING, to STOPPED
				if (State.STOPPED.equals(lastState)) {
					assertEquals(State.STOPPED, oldState);
					assertEquals(State.STARTING, newState);
				} else if (State.STARTING.equals(lastState)) {
					assertEquals(State.STARTING, oldState);
					assertEquals(State.OPERATING, newState);
				} else if (State.OPERATING.equals(lastState)) {
					assertEquals(State.OPERATING, oldState);
					assertEquals(State.STOPPING, newState);
				} else if (State.STOPPING.equals(lastState)) {
					assertEquals(State.STOPPING, oldState);
					assertEquals(State.STOPPED, newState);
				} else {
					fail("Unexpected states: " + lastState + "/" + oldState + "/" + newState);
				}

				lastState = newState;
				called = true;
			}
		});
		batch.addBatchStateListeners(batchStateListeners);
		assertEquals(1, batch.getBatchStateListeners().size());

		batch.start();
		assertTrue("Batch State Listener should have been called", called);

		called = false;

		batch.stop();
		assertTrue("Batch State Listener should have been called", called);
	}

	private int count = 0;
	private final static State[] statesFailedToStartStop = {
		// initial
		State.STOPPED,
		// starting
		State.STARTING, State.STOPPED, State.FAILED,
		// stopping
		State.STOPPED, State.FAILED };

	@Test
	public void testStartWithRemoteProcedureFails() throws IOException {
		List<ProcedureStateListener> list = new ArrayList<ProcedureStateListener>();
		list.add(new ProcedureStateListener() {

			@Override
			public void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState) {
				try {
					assertEquals(MessageConstants.MODULE_BUSINESS_BACKEND + " (127.0.0.2)", subject.getName());
					assertEquals(statesFailedToStartStop[count], newState);

					called = true;
					count ++;
				} catch (AssertionError e) {
					error.set(e);
				}
			}
		});
		DefaultScenario scenario = new DefaultScenario();
		ProcedureMapping procedureMapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);
		scenario.addProcedureMapping(procedureMapping);
		Batch batch = new Batch(scenario, list);

		assertFalse(called);

		int port = SocketUtils.reserveNextFreePort(10000, 110000, "127.0.0.1");
		int oldPort = EasyTravelConfig.read().launcherHttpPort;
		EasyTravelConfig.read().launcherHttpPort = port;
		try {
			System.setProperty("com.dynatrace.easytravel.host.business_backend", "127.0.0.2");
			try {
				batch.start();
				assertEquals("OPERATING even if the remote procedure did not start",
						State.OPERATING, batch.getState());

				batch.stop();
				assertEquals("STOPPED even if the remote procedure did not stop",
						State.STOPPED, batch.getState());
			} finally {
				System.clearProperty("com.dynatrace.easytravel.host.business_backend");
			}
		} finally {
			EasyTravelConfig.read().launcherHttpPort = oldPort;
			SocketUtils.freePort(port);
		}

		assertTrue(called);
	}

	@Test
	public void testStartWithDisabledProcedure() {
		List<ProcedureStateListener> list = new ArrayList<ProcedureStateListener>();
		list.add(new ProcedureStateListener() {

			@Override
			public void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState) {
				try {
					assertEquals("Only the initial STOPPED is received here", State.STOPPED, newState);
					count++;
					called = true;
				} catch (AssertionError e) {
					error.set(e);
				}
			}
		});
		DefaultScenario scenario = new DefaultScenario();
		DefaultProcedureMapping procedureMapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);
		procedureMapping.addSetting(new DefaultProcedureSetting(Constants.Misc.SETTING_ENABLED, Constants.Misc.SETTING_VALUE_OFF));
		scenario.addProcedureMapping(procedureMapping);
		Batch batch = new Batch(scenario, list);

		assertEquals(0, count);
		assertFalse(called);

		System.setProperty("com.dynatrace.easytravel.host.business_backend", "127.0.0.1");
		try {
			batch.start();
			assertEquals("OPERATING even if the remote procedure did not start",
					State.OPERATING, batch.getState());

			batch.stop();
			assertEquals("STOPPED even if the remote procedure did not stop",
					State.STOPPED, batch.getState());
		} finally {
			System.clearProperty("com.dynatrace.easytravel.host.business_backend");
		}

		assertTrue("Called only once", called);
		assertEquals("Called only once for the initial state", 1, count);
	}

	@Test
	public void testStartWithRemoteProcedureSuccess() throws Exception {
		// here we have different states because of the MockRESTProcedureControl
		final State[] states = { State.STOPPED, State.STARTING, State.STOPPED };

		List<ProcedureStateListener> list = new ArrayList<ProcedureStateListener>();
		list.add(new ProcedureStateListener() {

			@Override
			public void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState) {
				try {
					assertTrue(subject.getDelegate() instanceof RemoteProcedure);
					assertEquals(MessageConstants.MODULE_BUSINESS_BACKEND + " (127.0.0.2)", subject.getName());
					assertTrue("Had more states than expected, additional: " + newState,
							count < states.length);
					assertEquals("Count: " + count,
							states[count], newState);
					LOGGER.info("newState: " + newState);

					count ++;
					called = true;
				} catch (AssertionError e) {
					error.set(e);
				}
			}
		});

		DefaultScenario scenario = new DefaultScenario();
		ProcedureMapping procedureMapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);
		scenario.addProcedureMapping(procedureMapping);

		Batch batch = new Batch(scenario, list);

		assertFalse("Not called yet", called);

		// start REST service by invoking a dummy HTTP REST Service
		int port = SocketUtils.reserveNextFreePort(10000, 110000, "127.0.0.1");
		int oldPort = EasyTravelConfig.read().launcherHttpPort;
		EasyTravelConfig.read().launcherHttpPort = port;
		try {
			MockHttpServiceThread thread = new MockHttpServiceThread(port);
			thread.start();

			System.setProperty("com.dynatrace.easytravel.host.business_backend", "127.0.0.2");
			try {
				batch.start();
				assertEquals("OPERATING after the remote procedure started",
						State.OPERATING, batch.getState());

				batch.stop();
				assertEquals("STOPPED after the remote procedure stopped",
						State.STOPPED, batch.getState());
			} finally {
				System.clearProperty("com.dynatrace.easytravel.host.business_backend");
				thread.stopService();
			}
		} finally {
			EasyTravelConfig.read().launcherHttpPort = oldPort;
			SocketUtils.freePort(port);
		}

		assertTrue(called);
	}

	/**
	 * See HttpServiceThread for details
	 *
	 * @author dominik.stadler
	 */
	private class MockHttpServiceThread {
		private HttpServer server;
		private int port;

		public MockHttpServiceThread(int port) {
			this.port = port;
		}

		public void start() throws IOException {
			final String baseUri = "http://localhost:" + port + "/";

			LOGGER.info("Starting REST server on URI: '" + baseUri + "'...");
			ResourceConfig config = new PackagesResourceConfig("com.dynatrace.easytravel.launcher.engine");	// for test REST impls.
	        server = HttpServerFactory.create(baseUri, config);
	        server.start();
		}

		public void stopService() {
			if(server == null) {
				return;
			}

			server.stop(0);

			// Workaround for Jersey shortcoming: It adds a ThreadPoolExecutor with 60 seconds Worker-timeout
			// which causes the application to wait up to 60 seconds before actually shutting down
			// because there is still a non-daemon thread alive...
			Executor executor = server.getExecutor();
			if(executor instanceof ExecutorService) {
				((ExecutorService)executor).shutdown();
			}

			// indicate that we do not run the service any more by setting this to null
			server = null;
		}
	}

	@Test
	public void testStartWithMultipleProceduresSuccess() throws Exception {
		List<ProcedureStateListener> list = new ArrayList<ProcedureStateListener>();
		DefaultScenario scenario = new DefaultScenario();

		ProcedureMapping procedureMapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);
		scenario.addProcedureMapping(procedureMapping);
		procedureMapping = new DefaultProcedureMapping(Constants.Procedures.DATABASE_CONTENT_CREATOR_ID);
		scenario.addProcedureMapping(procedureMapping);
		procedureMapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);
		scenario.addProcedureMapping(procedureMapping);
		Batch batch = new Batch(scenario, list);

		// start REST service by invoking a dummy HTTP REST Service
		int port = SocketUtils.reserveNextFreePort(10000, 110000, "127.0.0.1");
		int oldPort = EasyTravelConfig.read().launcherHttpPort;
		EasyTravelConfig.read().launcherHttpPort = port;
		EasyTravelConfig.read().syncProcessTimeoutMs=1000;
		try {
			MockHttpServiceThread thread = new MockHttpServiceThread(port);
			thread.start();

			System.setProperty("com.dynatrace.easytravel.host.business_backend", "127.0.0.1");
			try {
				batch.start();
				assertEquals("OPERATING after the remote procedure started",
						State.OPERATING, batch.getState());

				batch.stop();
				assertEquals("STOPPED after the remote procedure stopped",
						State.STOPPED, batch.getState());
			} finally {
				System.clearProperty("com.dynatrace.easytravel.host.business_backend");
				thread.stopService();
			}
		} finally {
			EasyTravelConfig.read().launcherHttpPort = oldPort;
			SocketUtils.freePort(port);
		}
	}


	@Test
	public void testTransferToSameScenario() throws Exception {
		List<ProcedureStateListener> list = new ArrayList<ProcedureStateListener>();
		DefaultScenario scenario = new DefaultScenario();

		ProcedureMapping procedureMapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);
		scenario.addProcedureMapping(procedureMapping);
		procedureMapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);
		scenario.addProcedureMapping(procedureMapping);
		Batch batch = new Batch(scenario, list);

		// start REST service by invoking a dummy HTTP REST Service
		int port = SocketUtils.reserveNextFreePort(10000, 110000, "127.0.0.1");
		int oldPort = EasyTravelConfig.read().launcherHttpPort;
		EasyTravelConfig.read().launcherHttpPort = port;
		EasyTravelConfig.read().syncProcessTimeoutMs=1000;
		try {
			MockHttpServiceThread thread = new MockHttpServiceThread(port);
			thread.start();

			System.setProperty("com.dynatrace.easytravel.host.business_backend", "127.0.0.2");
			try {
				batch.start();
				assertEquals("OPERATING after the remote procedure started",
						State.OPERATING, batch.getState());

				batch.transfer(scenario.copy(), list);
				Batch newBatch = batch.transfer(scenario.copy(), list);

				List<StatefulProcedure> procedures = newBatch.getProcedures();
				assertEquals(2, procedures.size());
				assertEquals(MessageConstants.MODULE_DERBY_DATABASE_MANAGEMENT_SYSTEM, procedures.get(0).getName());
				assertEquals(MessageConstants.MODULE_BUSINESS_BACKEND + " (127.0.0.2)", procedures.get(1).getName());

				batch.stop();
				assertEquals("STOPPED after the remote procedure stopped",
						State.STOPPED, batch.getState());

				newBatch.start();
				assertEquals("OPERATING after the remote procedure started",
						State.OPERATING, newBatch.getState());

				newBatch.stop();
				assertEquals("STOPPED after the remote procedure stopped",
						State.STOPPED, newBatch.getState());
			} finally {
				System.clearProperty("com.dynatrace.easytravel.host.business_backend");
				thread.stopService();
			}
		} finally {
			EasyTravelConfig.read().launcherHttpPort = oldPort;
			SocketUtils.freePort(port);
		}
	}

	@Test
	public void testTransferToDifferentScenario() throws Exception {
		List<ProcedureStateListener> list = new ArrayList<ProcedureStateListener>();
		DefaultScenario scenario = new DefaultScenario();

		ProcedureMapping procedureMapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);
		scenario.addProcedureMapping(procedureMapping);
		procedureMapping = new DefaultProcedureMapping(Constants.Procedures.DATABASE_CONTENT_CREATOR_ID);
		scenario.addProcedureMapping(procedureMapping);
		procedureMapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);
		scenario.addProcedureMapping(procedureMapping);
		Batch batch = new Batch(scenario, list);

		// start REST service by invoking a dummy HTTP REST Service
		int port = SocketUtils.reserveNextFreePort(10000, 110000, "127.0.0.1");
		int oldPort = EasyTravelConfig.read().launcherHttpPort;
		EasyTravelConfig.read().launcherHttpPort = port;
		EasyTravelConfig.read().syncProcessTimeoutMs=1000;
		try {
			MockHttpServiceThread thread = new MockHttpServiceThread(port);
			thread.start();

			System.setProperty("com.dynatrace.easytravel.host.business_backend", "127.0.0.1");
			try {
				batch.start();
				assertEquals("OPERATING after the remote procedure started",
						State.OPERATING, batch.getState());

				DefaultScenario newScenario = new DefaultScenario();

				procedureMapping = new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID);
				newScenario.addProcedureMapping(procedureMapping);

				Batch newBatch = batch.transfer(newScenario, list);

				List<StatefulProcedure> procedures = newBatch.getProcedures();
				assertEquals("Should have one procedure in new Batch", 1, procedures.size());
				assertEquals(MessageConstants.MODULE_DERBY_DATABASE_MANAGEMENT_SYSTEM, procedures.get(0).getName());

				batch.stop();
				assertEquals("STOPPED after the remote procedure stopped",
						State.STOPPED, batch.getState());

				newBatch.start();
				assertEquals("OPERATING after the remote procedure started",
						State.OPERATING, newBatch.getState());

				newBatch.stop();
				assertEquals("STOPPED after the remote procedure stopped",
						State.STOPPED, newBatch.getState());
			} finally {
				System.clearProperty("com.dynatrace.easytravel.host.business_backend");
				thread.stopService();
			}
		} finally {
			EasyTravelConfig.read().launcherHttpPort = oldPort;
			SocketUtils.freePort(port);
		}
	}

	@Test
	public void testAddScenarioListeners() {
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);
		Collection<ScenarioListener> scenarioListeners = new ArrayList<ScenarioListener>();
		batch.addScenarioListeners(scenarioListeners);

		scenarioListeners.add(new ScenarioListener() {

			@Override
			public void notifyScenarioChanged(Scenario scenario) {
			}
		});
		batch.addScenarioListeners(scenarioListeners);
	}

	@Test
	public void testAddProcedure() {
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);

		batch.addProcedure(new StatefulProcedure(new MockProcedure(new DefaultProcedureMapping("test"), true, Technology.JAVA)));

		assertEquals(State.STOPPED, batch.getState());

		batch.start();

		assertEquals("Batch is up", State.OPERATING, batch.getState());
		assertEquals("Procedure is up", State.OPERATING, batch.getProcedures().iterator().next().getState());
	}

	@Test
	public void testTimeout() {
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);

		batch.addProcedure(new StatefulProcedure(new MockProcedure(new DefaultProcedureMapping("test"), false, Technology.JAVA)));

		assertEquals(State.STOPPED, batch.getState());

		EasyTravelConfig.read().syncProcessTimeoutMs=100;
		// will timeout
		batch.start();
		EasyTravelConfig.resetSingleton();

		assertEquals("Batch is up", State.OPERATING, batch.getState());
		assertEquals("Procedure is timed out", State.TIMEOUT, batch.getProcedures().iterator().next().getState());

	}

	@Test
	public void testNotifyTechnologyStateChangedEnabled() {
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);

		batch.addProcedure(new StatefulProcedure(new MockProcedure(new DefaultProcedureMapping("test"), true, Technology.JAVA)));

		assertEquals(State.STOPPED, batch.getState());

		batch.start();

		batch.notifyTechnologyStateChanged(Technology.JAVA, true, null, null);

		assertEquals("Batch is up", State.OPERATING, batch.getState());
		assertEquals("Procedure is up", State.OPERATING, batch.getProcedures().iterator().next().getState());
	}

	@Test
	public void testNotifyTechnologyStateChangedEnabled2() {
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);

		batch.addProcedure(new StatefulProcedure(new MockProcedure(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID), true, Technology.JAVA)));
		batch.addProcedure(new StatefulProcedure(new MockProcedure(new DefaultProcedureMapping(Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID), true, Technology.ADK)));

		assertEquals(State.STOPPED, batch.getState());

		batch.start();

		batch.notifyTechnologyStateChanged(Technology.JAVA, true, null, null);

		assertEquals("Batch is up", State.OPERATING, batch.getState());
		assertEquals("Procedure 1 is up", State.OPERATING, batch.getProcedures().get(0).getState());
		assertEquals("Procedure 2 is up", State.OPERATING, batch.getProcedures().get(1).getState());
	}

	@Test
	public void testNotifyTechnologyStateChangedEnabledStartup() {
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);

		batch.addProcedure(new StatefulProcedure(new MockProcedure(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID), true, Technology.JAVA)));
		batch.addProcedure(new StatefulProcedure(new MockProcedure(new DefaultProcedureMapping(Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID), true, Technology.ADK)));

		assertEquals(State.STOPPED, batch.getState());
		assertEquals("Procedure 1 is down", State.STOPPED, batch.getProcedures().get(0).getState());
		assertEquals("Procedure 2 is down", State.STOPPED, batch.getProcedures().get(1).getState());

		batch.notifyTechnologyStateChanged(Technology.JAVA, true, null, null);

		assertEquals("Batch is up", State.STOPPED, batch.getState());
		assertEquals("Procedure 1 is still down because Batch is stopped", State.STOPPED, batch.getProcedures().get(0).getState());
		assertEquals("Procedure 2 is still down", State.STOPPED, batch.getProcedures().get(1).getState());

		// start batch
		batch.start();

		// both are started now
		assertEquals(State.OPERATING, batch.getState());
		assertEquals("Procedure 1 is up now", State.OPERATING, batch.getProcedures().get(0).getState());
		assertEquals("Procedure 2 is up now", State.OPERATING, batch.getProcedures().get(1).getState());

		// shut down Java
		batch.notifyTechnologyStateChanged(Technology.JAVA, false, null, null);
		assertEquals(State.OPERATING, batch.getState());
		assertEquals("Procedure 1 is down now", State.STOPPED, batch.getProcedures().get(0).getState());
		assertEquals("Procedure 2 is up now", State.OPERATING, batch.getProcedures().get(1).getState());

		// and start it again
		batch.notifyTechnologyStateChanged(Technology.JAVA, true, null, null);
		assertEquals(State.OPERATING, batch.getState());
		assertEquals("Procedure 1 is up now again", State.OPERATING, batch.getProcedures().get(0).getState());
		assertEquals("Procedure 2 is up now", State.OPERATING, batch.getProcedures().get(1).getState());
	}


	@Test
	public void testNotifyTechnologyStateChangedDisabled() {
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);

		batch.addProcedure(new StatefulProcedure(new MockProcedure(new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID), true, Technology.JAVA)));
		batch.addProcedure(new StatefulProcedure(new MockProcedure(new DefaultProcedureMapping(Constants.Procedures.CREDIT_CARD_AUTH_UNIT_ID), true, Technology.ADK)));

		assertEquals(State.STOPPED, batch.getState());

		batch.start();

		batch.notifyTechnologyStateChanged(Technology.JAVA, false, null, null);

		assertEquals("Batch is up", State.OPERATING, batch.getState());
		assertEquals("Procedure 1 is now down", State.STOPPED, batch.getProcedures().get(0).getState());
		assertEquals("Procedure 2 is up", State.OPERATING, batch.getProcedures().get(1).getState());
	}
	
	@Test
	public void testRunWithStoppedProcedure(){
		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);
		ProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.VAGRANT_ID);
		mapping.addSetting(SettingBuilder.config("config.disableProcedureStartup").value("yes").create());
		
		batch.addProcedure(new StatefulProcedure(new MockProcedure(mapping, true, Technology.VAGRANT)));
		
		assertEquals(State.STOPPED, batch.getState());

		batch.start();
		
		for(StatefulProcedure proc : batch.getProcedures()){
			if(proc.getTechnology().equals(Technology.VAGRANT)){
				assertEquals(State.STOPPED, proc.getState());
			}
		}

		assertEquals("Only one procedure should be in batch.", 1, batch.getProcedures().size());
	}

    @Test
    public void testMultipleThreads() throws Throwable {
		List<ProcedureStateListener> list = Collections.emptyList();
		final Batch batch = new Batch(new DefaultScenario(), list);

		ThreadTestHelper helper =
            new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

        helper.executeTest(new ThreadTestHelper.TestRunnable() {
            @Override
            public void doEnd(int threadnum) throws Exception {
                // do stuff at the end ...
            }

            @Override
            public void run(int threadnum, int iter) throws Exception {
                if(threadnum == 0) {
            		// one thread is only starting/stopping the scenario
            		batch.start();
            		batch.stop();
                } else {
                	// the other threads are constantly adding ProcedureStateListeners
                	batch.getProcedureStateListeners().add(new ProcedureStateListener() {

						@Override
						public void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState) {
							// nothing to do here
						}
					});
                }
            }
        });
    }

	private class MockProcedure extends DummyProcedure {
		private final boolean operating;
		private final Technology technology;

		private MockProcedure(ProcedureMapping mapping, boolean operating, Technology technology) throws IllegalArgumentException {
			super(mapping, Feedback.Neutral, null);

			this.operating = operating;
			this.technology = technology;
		}

		@Override
		public Feedback stop() {
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
		public boolean isOperatingCheckSupported() {
			return true;
		}

		@Override
		public boolean isOperating() {
			return operating;
		}

		@Override
		public Technology getTechnology() {
			return technology;
		}
	}
}
