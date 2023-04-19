package com.dynatrace.easytravel.launcher.procedures;

import static org.junit.Assert.*;

import org.junit.Test;

import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;


public class HostAgentControlProcedureTest {

	@Test
	public void testHostAgentRestartProcedure() throws Exception {
		HostAgentControlProcedure proc = new HostAgentControlProcedure(new DefaultProcedureMapping(Constants.Procedures.HOST_AGENT_RESTART_ID));

		// NOOPs
		proc.addStopListener(null);
		proc.removeStopListener(null);
		proc.clearStopListeners();

		assertFalse(proc.agentFound());
		assertNull(proc.getTechnology());
		assertFalse(proc.hasLogfile());
		assertNull(proc.getLogfile());
		assertNotNull(proc.getDetails());
		assertFalse(proc.isSynchronous());
		assertTrue(proc.isOperatingCheckSupported());
		assertFalse(proc.isOperating());

		assertEquals(Feedback.Success, proc.stop());
		assertTrue(proc.isStoppable());
		assertEquals(StopMode.PARALLEL, proc.getStopMode());
		assertFalse(proc.isRunning());

		// simply run it to make sure it does not throw an Exception to the outside even if it fails
		proc.run();

		Thread.sleep(10000);

		assertTrue(proc.isOperating());
		proc.stop();
		assertFalse(proc.isOperating());

		// cannot verify if the correct agent is installed
		/*if(OperatingSystem.WINDOWS.equals(OperatingSystem.pickUp())) {
			assertEquals(Feedback.Success, proc.run());
		}*/
	}

	@Test
	public void testHostAgentRestartProcedureStopTwice() {
		HostAgentControlProcedure proc = new HostAgentControlProcedure(new DefaultProcedureMapping(Constants.Procedures.HOST_AGENT_RESTART_ID));

		assertFalse(proc.isOperating());
		proc.stop();
		assertFalse(proc.isOperating());
		proc.stop();
		assertFalse(proc.isOperating());
	}

	@Test
	public void testHostAgentStartWithServiceStarted() throws Exception {
		HostAgentControlProcedure proc = new HostAgentControlProcedure(new DefaultProcedureMapping(Constants.Procedures.HOST_AGENT_RESTART_ID));
		HostAgentControlProcedure proc2 = new HostAgentControlProcedure(new DefaultProcedureMapping(Constants.Procedures.HOST_AGENT_RESTART_ID));

		// use proc to make sure it is started in the test
		proc.run();

		try {
			Thread.sleep(10000);

			// now start the second process, this way we should at least cover some of the other cases as well
			proc2.run();
			proc2.stop();
		} finally {
			proc.stop();
		}
	}
}
