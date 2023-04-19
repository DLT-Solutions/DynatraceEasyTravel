package com.dynatrace.easytravel.launcher.engine;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.database.DatabaseBase;
import com.dynatrace.easytravel.ipc.SocketUtils;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureSetting;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.TestHelpers;


public class SingleProcedureBatchTest {
    private static final Logger LOGGER = LoggerFactory.make();

    static {
		// set system properties for installation location
		System.setProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION, "../Distribution/dist");

		LOGGER.info("Using files from: " + System.getProperty(BaseConstants.SystemProperties.INSTALL_DIR_CORRECTION));
    }

	@Before
	public void setUp() {
		// remove possible leftovers from other tests
		LaunchEngine.stop();

		// ensure database was stopped correctly in previous tests
		assertTrue(SocketUtils.isPortAvailable(EasyTravelConfig.read().internalDatabasePort, null));
		assertTrue(SocketUtils.isPortAvailable(EasyTravelConfig.read().internalDatabasePort, null));
	}

	@After
	public void tearDown() throws InterruptedException {
		while(!SocketUtils.isPortAvailable(EasyTravelConfig.read().internalDatabasePort, null) ||
				!SocketUtils.isPortAvailable(EasyTravelConfig.read().internalDatabasePort, null)) {
			Thread.sleep(100);
		}
	}

	@Test
	public void test() throws InterruptedException {
		List<ProcedureStateListener> emptyList = Collections.emptyList();
		SingleProcedureBatch batch = new SingleProcedureBatch(
				new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID), emptyList);

		Collection<BatchStateListener> emptyList2 = Collections.emptyList();
		batch.addBatchStateListeners(emptyList2);

		assertNotNull(batch.getBatchStateListeners());

		assertEquals(0, batch.getBatchStateListeners().size());

		assertEquals("only used for web procedures", -1, batch.getPort());

		assertNotNull(batch.getProcedure());

		assertEquals(0, batch.getProcedureStateListeners().size());

		assertEquals(State.STOPPED, batch.getState());

		batch.start();

		assertEquals(State.OPERATING, batch.getState());

		// start again fails
		try {
			batch.start();
			fail("Should catch Exception here");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "The batch can only be started if it is in state STOPPED", "Current state is OPERATING");
		}

		batch.stop();

		assertEquals(State.STOPPED, batch.getState());

		// stop again fails
		try {
			batch.stop();
			fail("Should catch Exception here");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "The batch can only be stopped if it is in state OPERATING, STARTING or TIMEOUT",
					"Current state is STOPPED");
		}

		assertEquals(State.STOPPED, batch.getState());

		batch.start();

		assertEquals(State.STARTING, batch.getProcedure().getState());

		assertTrue("Procedure Should be operating now", batch.getProcedure().isOperating());

		// stop with "STARTING" works
		batch.stop();

		assertFalse("Procedure Should be operating now", batch.getProcedure().isOperating());

		batch.start();
		EasyTravelConfig.read().syncProcessTimeoutMs = 0;
		try {
			assertEquals(State.TIMEOUT, batch.getState());
		} finally {
			EasyTravelConfig.read().syncProcessTimeoutMs = 30000;
		}

		// stop with "TIMEOUT" works
		batch.stop();
	}

	@Test
	public void testNull() {
		List<ProcedureStateListener> emptyList = Collections.emptyList();
		try {
			new SingleProcedureBatch(null, emptyList);
			fail("Should catch Exception here");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "Could not create procedure");
		}

		try {
			new SingleProcedureBatch(new DefaultProcedureMapping("notexisting"), emptyList);
			fail("Should catch Exception here");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "Could not create procedure", "notexisting");
		}
	}

	@Test
	public void testSyncProcedure() throws IOException {
		DatabaseBase.setUpClass();

		try {
			List<ProcedureStateListener> emptyList = Collections.emptyList();
			SingleProcedureBatch batch = new SingleProcedureBatch(new DefaultProcedureMapping(
					Constants.Procedures.DATABASE_CONTENT_CREATOR_ID), emptyList);

			Collection<BatchStateListener> emptyList2 = Collections.emptyList();
			batch.addBatchStateListeners(emptyList2);

			assertNotNull(batch.getBatchStateListeners());

			assertEquals(0, batch.getBatchStateListeners().size());

			assertEquals("only used for web procedures", -1, batch.getPort());

			assertNotNull(batch.getProcedure());

			assertEquals(0, batch.getProcedureStateListeners().size());

			assertEquals(State.STOPPED, batch.getState());

			batch.start();

			assertEquals(State.STOPPED, batch.getState());
		} finally {
			DatabaseBase.tearDownClass();
		}
	}

	@Test
	public void testFailed() {
		EasyTravelConfig.read().syncProcessTimeoutMs = 0;
		try {
			List<ProcedureStateListener> emptyList = Collections.emptyList();
			SingleProcedureBatch batch = new SingleProcedureBatch(
					new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID), emptyList);

			Collection<BatchStateListener> emptyList2 = Collections.emptyList();
			batch.addBatchStateListeners(emptyList2);

			assertNotNull(batch.getBatchStateListeners());

			assertEquals(0, batch.getBatchStateListeners().size());

			assertEquals("only used for web procedures", -1, batch.getPort());

			assertNotNull(batch.getProcedure());

			assertEquals(0, batch.getProcedureStateListeners().size());

			assertEquals(State.STOPPED, batch.getState());

			batch.start();

			assertEquals(State.FAILED, batch.getState());

			// ensure that we correctly stop the DBMSProcedure before leaving
			batch.getProcedure().stop();
		} finally {
			EasyTravelConfig.read().syncProcessTimeoutMs = 30000;
		}
	}

	@Test
	public void testTimeout() {
		List<ProcedureStateListener> emptyList = Collections.emptyList();
		SingleProcedureBatch batch = new SingleProcedureBatch(
				new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID), emptyList);

		Collection<BatchStateListener> emptyList2 = Collections.emptyList();
		batch.addBatchStateListeners(emptyList2);

		assertNotNull(batch.getBatchStateListeners());

		assertEquals(0, batch.getBatchStateListeners().size());

		assertEquals("only used for web procedures", -1, batch.getPort());

		assertNotNull(batch.getProcedure());

		assertEquals(0, batch.getProcedureStateListeners().size());

		assertEquals(State.STOPPED, batch.getState());

		batch.start();

		EasyTravelConfig.read().syncProcessTimeoutMs = 0;
		try {
			assertEquals(State.TIMEOUT, batch.getState());
		} finally {
			EasyTravelConfig.read().syncProcessTimeoutMs = 30000;
		}

		batch.stop();

		assertEquals(State.STOPPED, batch.getState());
	}

	@Test
	public void testCustomSettings() throws IOException {
		List<ProcedureStateListener> emptyList = Collections.emptyList();
		DefaultProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);

		mapping.addSetting(new DefaultProcedureSetting(Scenario.TYPE_SCENARIO_PROCEDURE_CONFIG, "config.backendAgent", "testvalue2"));

		SingleProcedureBatch batch = new SingleProcedureBatch(mapping, emptyList);

		assertEquals("Changed config value should be applied now",
				"testvalue2", EasyTravelConfig.read().backendAgent);

		File propertyFile = batch.getProcedure().getPropertyFile();
		assertNotNull("Did not get property file", propertyFile);
		assertTrue("Did not find: " + propertyFile, propertyFile.exists());

		String properties = FileUtils.readFileToString(propertyFile);
		assertTrue("File " + propertyFile + " (" + properties.length() + ") did not contain expected value 'testvalue2'", properties.contains("config.backendAgent=testvalue2"));
	}
}
