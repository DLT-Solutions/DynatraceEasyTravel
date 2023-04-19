package com.dynatrace.easytravel.launcher.engine;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.DummyProcedure;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;


public class AbstractStopperTest {

	@Test
	public void testNoProcedure() {
		Collection<Procedure> list = Collections.emptyList();
		AbstractStopper stopper = new AbstractStopper(list) {};

		// works without any procedure
		assertTrue(stopper.execute());
	}

	@Test
	public void testOneStoppedProcedure() {
		Procedure dbmsProc = new ProcedureFactory().create(new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID));

		Collection<Procedure> list = Collections.singleton(dbmsProc);
		AbstractStopper stopper = new AbstractStopper(list) {};
		assertTrue(stopper.execute());
	}

	@Test
	public void testTwoStoppedProcedures() {
		Procedure dbmsProc = new ProcedureFactory().create(new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID));
		Procedure createProc = new ProcedureFactory().create(new DefaultProcedureMapping(Constants.Procedures.DATABASE_CONTENT_CREATOR_ID));

		Collection<Procedure> list = new ArrayList<Procedure>();
		list.add(dbmsProc);
		list.add(createProc);
		AbstractStopper stopper = new AbstractStopper(list) {};
		assertTrue(stopper.execute());
	}

	@Test
	public void testStopFails() {
		Procedure dummyProc = new DummyProcedure(new DefaultProcedureMapping(Constants.Procedures.HBASE_ID), Feedback.Neutral, null) {

			@Override
			public StopMode getStopMode() {
				return StopMode.PARALLEL;
			}

			@Override
			public boolean isRunning() {
				return true;
			}

			@Override
			public boolean isStoppable() {
				return true;
			}

			@Override
			public Feedback stop() {
				return Feedback.Failure;
			}
		};
		assertTrue(dummyProc.isRunning());

		Collection<Procedure> list = new ArrayList<Procedure>();
		list.add(dummyProc);
		AbstractStopper stopper = new AbstractStopper(list) {};
		assertFalse("Stopping should fail because on procedure fails to stop", stopper.execute());
	}

	@Test
	public void testStartedProcedures() {
		Procedure dbmsProc = new ProcedureFactory().create(new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID));
		assertEquals(Feedback.Success, dbmsProc.run());
		assertTrue("Procedure is now running", dbmsProc.isRunning());
		assertTrue(dbmsProc.isStoppable());

		Procedure createProc = new ProcedureFactory().create(new DefaultProcedureMapping(Constants.Procedures.DATABASE_CONTENT_CREATOR_ID));

		final AtomicBoolean running = new AtomicBoolean(false);
		Procedure dummyProc = new DummyProcedure(new DefaultProcedureMapping(Constants.Procedures.HBASE_ID), Feedback.Neutral, null) {

			@Override
			public StopMode getStopMode() {
				return StopMode.PARALLEL;
			}

			@Override
			public boolean isRunning() {
				return running.get();
			}

			@Override
			public boolean isStoppable() {
				return true;
			}

			@Override
			public Feedback stop() {
				running.set(false);
				return Feedback.Success;
			}
		};
		assertFalse(dummyProc.isRunning());
		running.set(true);

		Collection<Procedure> list = new ArrayList<Procedure>();
		list.add(dbmsProc);
		list.add(createProc);
		list.add(dummyProc);
		AbstractStopper stopper = new AbstractStopper(list) {};
		assertTrue(stopper.execute());

		assertFalse("DBMSProcedure is not running any more", dbmsProc.isRunning());
		assertFalse("DummyProcedure.stop() was called", running.get());
		assertFalse("DummyProcedure is not running any more", dummyProc.isRunning());
	}
}
