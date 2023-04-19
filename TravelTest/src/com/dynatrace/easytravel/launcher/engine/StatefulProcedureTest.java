package com.dynatrace.easytravel.launcher.engine;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.easymock.EasyMock;
import org.easymock.internal.matchers.Any;
import org.junit.Test;

import com.dynatrace.easytravel.constants.BaseConstants.UrlType;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.utils.TestHelpers;


public class StatefulProcedureTest {

	private final class MockProcedure extends AbstractProcedure {
		boolean operatingCheckException = false;
		private List<StopListener> listeners = new ArrayList<StopListener>();

		public MockProcedure(boolean operatingCheckException) {
			super(new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID));
			this.operatingCheckException = operatingCheckException;
		}

		@Override
		public void removeStopListener(StopListener stopListener) {
		}

		@Override
		public void clearStopListeners() {
		}

		@Override
		public void addStopListener(StopListener stopListener) {
			listeners.add(stopListener);
		}

		@Override
		public Feedback stop() {
			for(StopListener listener : listeners) {
				listener.notifyProcessStopped();
			}

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
			if(operatingCheckException) {
				throw new IllegalStateException("Test Exception");
			}
			return false;
		}

		@Override
		public String getURI() {
			return "www.example.com";
		}

		@Override
		public String getDetails() {
			return "somedetails";
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
		public String getURI(UrlType urlType) {
			return null;
		}

        @Override
        public boolean agentFound() {
            return false;
        }

        @Override
        public Technology getTechnology() {
            return null;
        }
	}

	@Test
	public void testStatefulProcedure() {
		//Procedure proc = createStrictMock(Procedure.class);
		Procedure proc = new MockProcedure(false);

//		StopListener listener = createStrictMock(StopListener.class);

/*		proc.addStopListener(null);
		proc.clearStopListeners();
		proc.removeStopListener(null);
		expect(proc.getDetails()).andReturn("somedetails");
		expect(proc.getMapping()).andReturn(null);
		expect(proc.getName()).andReturn("myname");
		expect(proc.getURI()).andReturn("www.example.com");
		expect(proc.isEnabled()).andReturn(true);
		expect(proc.isOperating()).andReturn(false);
		expect(proc.isOperatingCheckSupported()).andReturn(true);
		expect(proc.isRunning()).andReturn(false);
		expect(proc.isStoppable()).andReturn(true);
		expect(proc.isSynchronous()).andReturn(false);
		expect(proc.isTransferableTo(null)).andReturn(false);
		expect(proc.run()).andReturn(Feedback.Neutral);
		expect(proc.stop()).andReturn(Feedback.Neutral);
		proc.transfer(null, null);*/

		//replay(listener,proc);

		StatefulProcedure sproc = new StatefulProcedure(proc);

		sproc.addStopListener(new AbstractStopListener() {

			@Override
			public void notifyProcessStopped() {
			}
		});
		sproc.clearStopListeners();
		sproc.removeStopListener(null);
		assertEquals("somedetails",sproc.getDetails());
		assertNotNull(sproc.getMapping());
		assertEquals(ProcedureFactory.getNameOfProcedure(new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID)),sproc.getName());
		assertEquals("www.example.com",sproc.getURI());
        assertNotEquals("WWW.EXAMPLE.COM", sproc.getURI());
		assertTrue(sproc.isEnabled());
		assertFalse(sproc.isOperating());
		assertTrue(sproc.isOperatingCheckSupported());
		assertTrue(sproc.isStartingFinished());
		assertFalse(sproc.isRunning());
		assertTrue(sproc.isStoppable());
		assertFalse(sproc.isSynchronous());
		assertFalse(sproc.isTransferableTo(null));
		assertEquals(Feedback.Neutral,sproc.run());
		assertEquals(Feedback.Neutral,sproc.stop());
		sproc.transfer(null, null);
		sproc.transfer(null);
		sproc.notifyProcedureStateChanged(sproc, State.STARTING, State.OPERATING);
		assertNull(sproc.getPropertyFile());

		// just cover some things that simply delegate
		assertEquals(0, sproc.getDependingProcedureIDs().size());
		assertNull(sproc.getLogfile());
		assertFalse(sproc.hasLogfile());
		assertNull(sproc.getURI(UrlType.APACHE_JAVA_FRONTEND));
		assertFalse(sproc.isWebProcedure());
		assertFalse(sproc.isDotNetIISProcedure());
		assertEquals(-1, sproc.getPort());
		assertNull(sproc.getPortPropertyName());
		assertNotNull(sproc.getDelegate());
		assertNull(sproc.getTechnology());
		assertFalse(sproc.isInstrumentationSupported());
		assertFalse(sproc.agentFound());

		try {
			sproc.setContinuously(true);
			fail("Should not be supported here");
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "must not set continuously");
		}

		//verify(listener,proc);

		assertEquals(State.STOPPED, sproc.getState());
		sproc.setState(State.OPERATING);
		assertEquals(State.OPERATING, sproc.getState());
		proc.stop();
		assertEquals("Should be state STOPPED again, because we triggered the stop listener here",
				State.STOPPED, sproc.getState());

		sproc.setState(State.TIMEOUT);
		assertEquals(State.TIMEOUT, sproc.getState());
		assertEquals(Feedback.Neutral, sproc.run());
		assertEquals(MessageConstants.STATE_TIMEOUT, sproc.getStateLabel());

		assertFalse(sproc.isTransferableTo(null));
	}

	@Test
	public void testStatefulProcedureOperatingException() {
		Procedure proc = new MockProcedure(true);

		StatefulProcedure sproc = new StatefulProcedure(proc);

		sproc.addStopListener(new AbstractStopListener() {
			@Override
			public void notifyProcessStopped() {
			}
		});
		sproc.clearStopListeners();
		sproc.removeStopListener(null);
		assertEquals("somedetails",sproc.getDetails());
		assertNotNull(sproc.getMapping());
		assertEquals(ProcedureFactory.getNameOfProcedure(new DefaultProcedureMapping(Constants.Procedures.INPROCESS_DBMS_ID)),sproc.getName());
		assertEquals("www.example.com",sproc.getURI());
		assertTrue(sproc.isEnabled());
		assertFalse(sproc.isOperating());
		assertTrue(sproc.isOperatingCheckSupported());
		assertFalse(sproc.isRunning());
		assertTrue(sproc.isStoppable());
		assertFalse(sproc.isSynchronous());
		assertFalse(sproc.isTransferableTo(null));
		assertEquals(Feedback.Neutral,sproc.run());
		assertEquals(Feedback.Neutral,sproc.stop());
		sproc.transfer(null, null);
	}

	@Test
	public void testGetStateLabelInitial() {
		Procedure proc = EasyMock.createStrictMock(Procedure.class);

		proc.addStopListener(anyStopListener());

		EasyMock.expect(proc.isSynchronous()).andReturn(true);
		EasyMock.expect(proc.isSynchronous()).andReturn(false);

		EasyMock.replay(proc);

		StatefulProcedure stateful = new StatefulProcedure(proc);

		// sync
		assertEquals(MessageConstants.STATE_FINISHED, stateful.getStateLabel());

		// non-sync
		assertEquals(MessageConstants.STATE_NOT_RUNNING, stateful.getStateLabel());

		EasyMock.verify(proc);
	}

	@Test
	public void testGetStateLabelStopped() {
		Procedure proc = EasyMock.createStrictMock(Procedure.class);

		proc.addStopListener(anyStopListener());

		EasyMock.expect(proc.isSynchronous()).andReturn(true);
		EasyMock.expect(proc.isSynchronous()).andReturn(false);

		EasyMock.replay(proc);

		StatefulProcedure stateful = new StatefulProcedure(proc);
		stateful.setState(State.STOPPED);

		// sync
		assertEquals(MessageConstants.STATE_FINISHED, stateful.getStateLabel());

		// non-sync
		assertEquals(MessageConstants.STATE_NOT_RUNNING, stateful.getStateLabel());

		EasyMock.verify(proc);
	}

	@Test
	public void testGetStateLabelStarting() {
		Procedure proc = EasyMock.createStrictMock(Procedure.class);
		StatefulProcedure stateful = createStatefulProcedure(proc);

		proc.notifyProcedureStateChanged(stateful, State.STOPPED, State.STARTING);

		EasyMock.expect(proc.isSynchronous()).andReturn(true);
		EasyMock.expect(proc.isSynchronous()).andReturn(false);

		EasyMock.replay(proc);

		stateful.setState(State.STARTING);

		// sync
		assertEquals(MessageConstants.STATE_RUNNING, stateful.getStateLabel());

		// non-sync
		assertEquals(MessageConstants.STATE_STARTING, stateful.getStateLabel());

		EasyMock.verify(proc);
	}

	private StatefulProcedure createStatefulProcedure(Procedure proc) {
		proc.addStopListener(anyStopListener());
		EasyMock.replay(proc);
		StatefulProcedure stateful = new StatefulProcedure(proc);
		EasyMock.verify(proc);
		EasyMock.reset(proc);
		return stateful;
	}

	@Test
	public void testGetStateLabelOperating() {
		Procedure proc = EasyMock.createStrictMock(Procedure.class);
		StatefulProcedure stateful = createStatefulProcedure(proc);

		proc.notifyProcedureStateChanged(stateful, State.STOPPED, State.OPERATING);

		EasyMock.replay(proc);

		stateful.setState(State.OPERATING);

		// sync
		assertEquals(MessageConstants.STATE_OPERATING, stateful.getStateLabel());

		// non-sync
		assertEquals(MessageConstants.STATE_OPERATING, stateful.getStateLabel());

		EasyMock.verify(proc);
	}

	@Test
	public void testGetStateLabelStopping() {
		Procedure proc = EasyMock.createStrictMock(Procedure.class);
		StatefulProcedure stateful = createStatefulProcedure(proc);

		proc.notifyProcedureStateChanged(stateful, State.STOPPED, State.STOPPING);

		EasyMock.replay(proc);

		stateful.setState(State.STOPPING);

		// sync
		assertEquals(MessageConstants.STATE_STOPPING, stateful.getStateLabel());

		// non-sync
		assertEquals(MessageConstants.STATE_STOPPING, stateful.getStateLabel());

		EasyMock.verify(proc);
	}

	@Test
	public void testGetStateLabelFailed() {
		Procedure proc = EasyMock.createStrictMock(Procedure.class);
		StatefulProcedure stateful = createStatefulProcedure(proc);

		proc.notifyProcedureStateChanged(stateful, State.STOPPED, State.FAILED);

		EasyMock.replay(proc);

		stateful.setState(State.FAILED);

		// sync
		assertEquals(MessageConstants.STATE_FAILED, stateful.getStateLabel());

		// non-sync
		assertEquals(MessageConstants.STATE_FAILED, stateful.getStateLabel());

		EasyMock.verify(proc);
	}

	@Test
	public void testGetStateLabelUnknown() {
		Procedure proc = EasyMock.createStrictMock(Procedure.class);
		StatefulProcedure stateful = createStatefulProcedure(proc);

		proc.notifyProcedureStateChanged(stateful, State.STOPPED, State.UNKNOWN);

		EasyMock.replay(proc);

		stateful.setState(State.UNKNOWN);

		// sync
		assertEquals(MessageConstants.STATE_UNKNOWN, stateful.getStateLabel());

		// non-sync
		assertEquals(MessageConstants.STATE_UNKNOWN, stateful.getStateLabel());

		EasyMock.verify(proc);
	}

	private AbstractStopListener anyStopListener() {
		EasyMock.reportMatcher(Any.ANY);
		return null;
	}

	@Test
	public void testStatefulProcedureListener() {
		Procedure proc = new MockProcedure(false);
		StatefulProcedure sproc = new StatefulProcedure(proc);

		final AtomicBoolean called = new AtomicBoolean(false);
		ProcedureStateListener listener = new ProcedureStateListener() {

			@Override
			public void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState) {
				called.set(true);
			}
		};
		sproc.addListener(listener);

		assertTrue("Is immediately called during adding the listener itself", called.get());

		called.set(false);
		sproc.setState(State.OPERATING);
		assertTrue("Is called when state changes", called.get());

		called.set(false);
		sproc.setState(State.OPERATING);
		assertFalse("Is not called when state does not actually change", called.get());

		sproc.removeListener(listener);

		called.set(false);
		sproc.setState(State.STOPPING);
		assertFalse("Not called any more after remove", called.get());

		sproc.addListener(listener);

		called.set(false);
		sproc.setState(State.OPERATING);
		assertTrue("Called again when state changes", called.get());

		sproc.clearListeners();

		called.set(false);
		sproc.setState(State.STOPPING);
		assertFalse("Not called any more after clear", called.get());

		sproc.addListeners(Collections.singleton(listener));

		called.set(false);
		sproc.setState(State.OPERATING);
		assertTrue("Called again when state changes", called.get());

	}
}
