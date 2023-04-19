package com.dynatrace.diagnostics.uemload;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.iot.IotDevice;
import com.dynatrace.diagnostics.uemload.iot.car.RentalCarParams;
import com.dynatrace.diagnostics.uemload.iot.visit.IotActionType;
import com.dynatrace.diagnostics.uemload.iot.visit.RentalCarVisit;
import com.dynatrace.diagnostics.uemload.iot.visit.RentalCarVisitWithCrash;
import com.dynatrace.diagnostics.uemload.iot.visit.RentalCarVisitWithHttpError;
import com.dynatrace.diagnostics.uemload.openkit.OpenKitParams;
import com.dynatrace.diagnostics.uemload.scenarios.IotDevicesScenario;
import com.dynatrace.openkit.CrashReportingLevel;
import com.dynatrace.openkit.DataCollectionLevel;

/**
 *
 * @author Michal.Bakula
 *
 */
public class IotDevicesSimulatorTest {
	private static final RentalCarParams car = new RentalCarParams() {
		{
			setCountry("Poland");
			setIp("127.0.0.1");
			setManufacturer("easyTravel");
			setModel("easyTravel Car");
			setOs("easyTravel");
			setAppVersion("1");
			setDeviceId(103L);
			setDataCollectionLevel(DataCollectionLevel.USER_BEHAVIOR);
			setCrashReportingLevel(CrashReportingLevel.OPT_IN_CRASHES);
		}
	};

	@Test
	@Ignore("Test ignored because it need IoT configs set")
	public void testNormalVisit() {
		IotDevicesScenario scenario = new IotCarScenario();
		RentalCarVisit visit = scenario.getRandomVisit("http://localhost:8079", car, ExtendedDemoUser.MARIA_USER);

		IotActionType[] actions = visit.getActions();
		int noOfActions = actions.length;

		assertTrue(actions[0] == IotActionType.AUTHENTICATION);
		assertTrue(actions[1] == IotActionType.UNLOCK);
		assertTrue(actions[2] == IotActionType.START);
		assertTrue(actions[noOfActions-3] == IotActionType.PARK);
		assertTrue(actions[noOfActions-2] == IotActionType.STOP);
		assertTrue(actions[noOfActions-1] == IotActionType.LOCK);
		assertTrue(noOfActions > 6);

		try {
			runWithScenario(new IotDevicesScenario());
		} catch (Exception e) {
			fail("Something is wrong with action runner creation. Exception occured: " + e.getMessage());
		}
	}

	@Test
	@Ignore("Test ignored because it need IoT configs set")
	public void testHttpErrorVisit() {
		IotDevicesScenario scenario = new IotCarErrorScenario();
		RentalCarVisit visit = scenario.getRandomVisit("http://localhost:8079", car, ExtendedDemoUser.MARIA_USER);

		IotActionType[] actions = visit.getActions();

		assertTrue(actions[0] == IotActionType.AUTHENTICATION_FAILURE);

		try {
			runWithScenario(new IotDevicesScenario());
		} catch (Exception e) {
			fail("Something is wrong with action runner creation. Exception occured: " + e.getMessage());
		}
	}

	@Test
	@Ignore("Test ignored because it need IoT configs set")
	public void testCrashVisit() {
		IotDevicesScenario scenario = new IotCarCrashScenario();
		RentalCarVisit visit = scenario.getRandomVisit("http://localhost:8079", car, ExtendedDemoUser.MARIA_USER);

		IotActionType[] actions = visit.getActions();
		int noOfActions = actions.length;

		assertTrue(actions[0] == IotActionType.AUTHENTICATION);
		assertTrue(actions[1] == IotActionType.UNLOCK);
		assertTrue(actions[2] == IotActionType.START);
		assertTrue(actions[noOfActions-3] == IotActionType.CRASH);
		assertTrue(actions[noOfActions-2] == IotActionType.EMERGENCY);
		assertTrue(actions[noOfActions-1] == IotActionType.REPORT);
		assertTrue(noOfActions > 6);

		try {
			runWithScenario(new IotDevicesScenario());
		} catch (Exception e) {
			fail("Something is wrong with action runner creation. Exception occured: " + e.getMessage());
		}
	}

	@Test
	@Ignore("Test ignored because it need IoT configs set")
	public void testWrongActionExecutor() throws Exception {
		try {
			runWithScenario(new IotDevicesScenario());
		} catch (Exception e) {
			fail("Something is wrong with action runner creation. Exception occured: " + e.getMessage());
		}
	}

	protected void runWithScenario(UEMLoadScenario scenario) throws Exception {
		scenario.init();

		IotDevicesSimulator simulator = new IotDevicesSimulator(scenario);

		Runnable runner = simulator.createActionRunnerForVisit();
		assertNotNull(runner);
		runner.run();
	}

	public class IotCarScenario extends IotDevicesScenario {
		@Override
		public RentalCarVisit getRandomVisit(String host, OpenKitParams params, ExtendedCommonUser user) {
			return new RentalCarVisit(host, car, user);
		}
	}

	public class IotCarErrorScenario extends IotDevicesScenario {
		@Override
		public RentalCarVisit getRandomVisit(String host, OpenKitParams params, ExtendedCommonUser user) {
			return new RentalCarVisitWithHttpError(host, car, user);
		}
	}

	public class IotCarCrashScenario extends IotDevicesScenario {
		@Override
		public RentalCarVisit getRandomVisit(String host, OpenKitParams params, ExtendedCommonUser user) {
			return new RentalCarVisitWithCrash(host, car, user);
		}
	}
}
