package com.dynatrace.easytravel.launcher.procedures;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;
import com.dynatrace.easytravel.util.DtVersionDetector;


public class WindowsServiceControlProcedureTest {
	private ProcedureMapping mapping = new DefaultProcedureMapping(Constants.Procedures.WEBSERVER_AGENT_RESTART_ID);

	private WindowsServiceControlProcedure proc = new WindowsServiceControlProcedure(mapping) {
		@Override
		String getServiceNamePattern() {
			return "dynaTrace Web Server Agent ${version}";
		}
	};

	@After
	public void tearDown() {
		// reset at end of test
		DtVersionDetector.enforceInstallationType(null);
	}

	@Test
	public void test() throws InterruptedException {
		EasyTravelConfig.read().apmServerDefault = InstallationType.Classic;
		try {
			// cover some methods
			assertFalse(proc.isRunning());
			assertEquals(StopMode.PARALLEL, proc.getStopMode());
			assertTrue(proc.isStoppable());
			assertEquals(Feedback.Success, proc.stop());
			assertTrue(proc.isOperatingCheckSupported());
			assertFalse(proc.isOperating());
			String details = proc.getDetails();
			assertNotNull(details);
			assertNull(proc.getLogfile());
			assertFalse(proc.hasLogfile());
			assertNull(proc.getTechnology());
			assertFalse(proc.agentFound());
			proc.addStopListener(null);
			proc.removeStopListener(null);
			proc.clearStopListeners();

			// verify getDetails() some more
			assertTrue("Had: " + details, details.contains(BaseConstants.Version.DEFAULT_DYNATRACE_VERSION));

			// expect classic mode here
			assertTrue(DtVersionDetector.isClassic());

			assertEquals(Feedback.Neutral, proc.run());

			// wait for thread to finish
			while(!proc.isDoneRunning.get()) {
				Thread.sleep(100);
			}

			assertEquals(Feedback.Success, proc.stop());
			assertTrue("Had: " + details, details.contains(BaseConstants.Version.DEFAULT_DYNATRACE_VERSION));
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testAPM() throws InterruptedException {
		DtVersionDetector.enforceInstallationType(InstallationType.APM);
		assertTrue(DtVersionDetector.isAPM());

		assertEquals(Feedback.Neutral, proc.run());

		// wait for thread to finish
		while(!proc.isDoneRunning.get()) {
			Thread.sleep(100);
		}

		assertEquals(Feedback.Success, proc.stop());
	}
}
