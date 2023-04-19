package com.dynatrace.easytravel.launcher.baseload;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelCustomer;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelFixedCustomer;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelLauncherScenario;
import com.dynatrace.diagnostics.uemload.scenarios.EasyTravelPredictableCustomer;
import com.dynatrace.diagnostics.uemload.utils.UserFileGenerator;
import com.dynatrace.easytravel.config.CustomerTrafficScenarioEnum;
import com.dynatrace.easytravel.config.Directories;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultScenario;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.util.ResourceFileReader;
import com.dynatrace.easytravel.utils.TestHelpers;

import ch.qos.logback.classic.Level;

public class BaseLoadManagerTest {

	public static final String TEST_DATA_PATH = "../TravelTest/testdata";

	@BeforeClass
	public static void setupUsersFile() throws IOException{
		File source = new File(TEST_DATA_PATH, "Users.txt");
		File dest = new File(Directories.getConfigDir(), "Users.txt");
		FileUtils.copyFile(source, dest);
		UserFileGenerator generator = new UserFileGenerator();
		generator.generateUserFile();
	}

	@AfterClass
	public static void tearDownClass() {
		File f = new File(Directories.getConfigDir(), ResourceFileReader.EXTENDEDUSERS);
		f.delete();
		f = new File(Directories.getConfigDir(), ResourceFileReader.USERS);
		f.delete();
	}

	@Before
	public void setup() {
		BaseLoadManager.getInstance().reset();
	}

	@After
	public void teardown() {
		BaseLoadManager.getInstance().reset();
	}

	@Test
	public void test() {
		BaseLoadManager manager = BaseLoadManager.getInstance();

		// first test a few things without actual load controller
		check(manager);

		// now create the load-controllers
		assertSame(manager.getB2BBaseLoadInstance(23, 1.0, false), manager.getB2BBaseLoadInstance(23, 1.0, false));
		assertSame(manager.getCustomerBaseLoadInstance(CustomerTrafficScenarioEnum.EasyTravel, 4, 1.2, true), manager.getCustomerBaseLoadInstance(CustomerTrafficScenarioEnum.EasyTravel, 4, 1.2, true));
		assertSame(manager.getMobileNativeBaseLoadInstance(99, 0.4, false), manager.getMobileNativeBaseLoadInstance(99, 0.4, false));
		assertSame(manager.getMobileBrowserBaseLoadInstance(50, 0.2, false), manager.getMobileBrowserBaseLoadInstance(50, 0.2, false));

		check(manager);
	}


	@Test
	public void testBaseLoadInstance() {
		BaseLoadManager manager = BaseLoadManager.getInstance();
		manager.updateCustomerBaseLoadScenario(CustomerTrafficScenarioEnum.EasyTravel);
		BaseLoad baseLoadInstance = manager.getCustomerBaseLoadInstance(CustomerTrafficScenarioEnum.EasyTravel, 4, 1.2, true);

		assertTrue("CustomerBaseLoad expected", baseLoadInstance instanceof CustomerBaseLoad);
	}

	protected void check(BaseLoadManager manager) {
		manager.setCustomerBaseLoad(12);
		manager.setB2bBaseLoad(43);
		manager.setMobileNativeBaseLoad(254);
		manager.setMobileBrowserBaseLoad(255);
		manager.setCustomerVisits(23);
		manager.setB2bVisits(531);
		manager.setMobileNativeVisits(213);
		manager.setMobileBrowserVisit(215);

		manager.setHeaderPanelInterface(null);
		assertNull(manager.getHeaderPanel());

		manager.removeScheduleBlocking();

		assertTrue(manager.stopAll());
		assertTrue(manager.stopB2bLoad());
		assertTrue(manager.stopCustomerLoad());
		assertTrue(manager.stopMobileNativeLoad());
		assertTrue(manager.stopB2bLoadAndBlock());
		assertTrue(manager.stopCustomerLoadAndBlock());
		assertTrue(manager.stopMobileNativeLoadAndBlock());
		assertTrue(manager.stopMobileBrowserLoadAndBlock());
		assertTrue(manager.stopHeadlessCustomerLoadAndBlock());

		BaseLoad baseLoad = manager.getCustomerLoadController();
		assertFalse(baseLoad.hasHost());
		baseLoad.addHost2Scenario("somehost");
		baseLoad.removeHostFromScenario("somehost");
		assertFalse(baseLoad.hasHost());

		baseLoad = manager.getB2bLoadController();
		assertFalse(baseLoad.hasHost());
		baseLoad.addHost2Scenario("somehost");
		baseLoad.removeHostFromScenario("somehost");
		assertFalse(baseLoad.hasHost());

		baseLoad = manager.getMobileNativeLoadController();
		assertFalse(baseLoad.hasHost());
		baseLoad.addHost2Scenario("somehost");
		baseLoad.removeHostFromScenario("somehost");
		assertFalse(baseLoad.hasHost());

		baseLoad = manager.getMobileBrowserLoadController();
		assertFalse(baseLoad.hasHost());
		baseLoad.addHost2Scenario("somehost");
		baseLoad.removeHostFromScenario("somehost");
		assertFalse(baseLoad.hasHost());

		baseLoad = manager.getHeadlessCustomerLoadController();
		assertFalse(baseLoad.hasHost());
		baseLoad.addHost2Scenario("somehost");
		baseLoad.removeHostFromScenario("somehost");
		assertFalse(baseLoad.hasHost());
		
		baseLoad = manager.getHeadlessAngularLoadController();
		assertFalse(baseLoad.hasHost());
		baseLoad.addHost2Scenario("somehost");
		baseLoad.removeHostFromScenario("somehost");
		assertFalse(baseLoad.hasHost());

		manager.notifyScenarioChanged(null);
		manager.notifyScenarioChanged(new DefaultScenario());
		manager.notifyScenarioChanged(new DefaultScenario(MessageConstants.TESTCENTER_SCENARIO_INCREASING_LOAD_TITLE, ""));
		manager.notifyScenarioChanged(new DefaultScenario(MessageConstants.TESTCENTER_SCENARIO_INCREASING_LOAD_TITLE, ""));
		manager.notifyScenarioChanged(new DefaultScenario("someother", ""));
		manager.notifyScenarioChanged(new DefaultScenario("someother", ""));


		// first with increasing load
		manager.notifyScenarioChanged(new DefaultScenario(MessageConstants.DEVTEAM_SCENARIO_LOADTESTING_TITLE, ""));
		checkConfigChanged(manager);

		// then with normal scenario
		manager.notifyScenarioChanged(new DefaultScenario("someother", ""));
		checkConfigChanged(manager);
	}

	protected void checkConfigChanged(BaseLoadManager manager) {
		manager.notifyConfigLoaded(null, EasyTravelConfig.read());

		manager.notifyConfigLoaded(EasyTravelConfig.read(), EasyTravelConfig.read());

		EasyTravelConfig config = EasyTravelConfig.read();
		config.baseLoadDefault = config.baseLoadDefault + 12;
		EasyTravelConfig.resetSingleton();
		assertTrue(config.baseLoadDefault != EasyTravelConfig.read().baseLoadDefault);
		manager.notifyConfigLoaded(config, EasyTravelConfig.read());

		manager.notifyBatchStateChanged(null, State.getDefault(), State.OPERATING);
		manager.notifyBatchStateChanged(new DefaultScenario(), State.getDefault(), State.OPERATING);
	}

	@Test
	public void testWithDifferentLogLevel() {
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				BaseLoadManager manager = BaseLoadManager.getInstance();
				manager.reset();
				test();
				manager.reset();
				testBaseLoadInstance();
				manager.reset();
				testGetCustomerLoadScenarioWithConfigWatcher();
				manager.reset();
				testGetCustomerLoadScenarioWithLoadIncreaser();
				manager.reset();
				testIsLoadIncreaseScenario();
			}
		}, UEMLoadConfigWatcher.class.getName(), Level.DEBUG);
	}

	private void checkgetCustomerLoadScenario(boolean useLoadIncScenario) {
		try {
			BaseLoadManager manager = BaseLoadManager.getInstance();
			EasyTravelConfig config = EasyTravelConfig.read();
			//make sure that current instance of config is loaded
			manager.notifyConfigLoaded(null, config);
			config.customerLoadScenario = CustomerTrafficScenarioEnum.EasyTravel;

			Scenario scenario1 = EasyMock.createNiceMock(Scenario.class);
			if (useLoadIncScenario) {
				EasyMock.expect(scenario1.getTitle()).andReturn(MessageConstants.TESTCENTER_SCENARIO_INCREASING_LOAD_TITLE).anyTimes();
			} else {
				EasyMock.expect(scenario1.getTitle()).andReturn(MessageConstants.UEM_SCENARIO_DEFAULT_TITLE).anyTimes();
			}

			EasyMock.replay(scenario1);

			BaseLoad customerBaseLoadInstance = manager.getCustomerBaseLoadInstance(CustomerTrafficScenarioEnum.EasyTravel, 100, 0.5, false);
			if (!useLoadIncScenario) {
				assertEquals("Load should be set to 50", 50, customerBaseLoadInstance.getValue());
			}

			EasyTravelConfig.applyCustomSettings(Collections.singletonMap("config.customerLoadScenario", "EasyTravelPredictable"));
			assertEquals("EasyTravelConfig.read().customerLoadScenario should be EasyTravelPredictable, but was " + EasyTravelConfig.read().customerLoadScenario,CustomerTrafficScenarioEnum.EasyTravelPredictable, EasyTravelConfig.read().customerLoadScenario);
			EasyTravelLauncherScenario scenario = customerBaseLoadInstance.getScenario();
			assertTrue("Traffic scenario should be set to EasyTravelPredictable but was: " + scenario.getClass().getName(), (scenario instanceof EasyTravelPredictableCustomer));
			//check if load value is the same
			if (!useLoadIncScenario) {
				assertEquals("Load should be set to 50", 50, customerBaseLoadInstance.getValue());
			}

			EasyTravelConfig.applyCustomSettings(Collections.singletonMap("config.customerLoadScenario", "EasyTravelFixed"));
			assertEquals(CustomerTrafficScenarioEnum.EasyTravelFixed, EasyTravelConfig.read().customerLoadScenario);
			scenario = customerBaseLoadInstance.getScenario();
			assertTrue("Traffic scenario should be set to EasyTravelFixedCustomer but was: " + scenario.getClass().getName(), (scenario instanceof EasyTravelFixedCustomer));
			//check if load value is the same
			if (!useLoadIncScenario) {
				assertEquals("Load should be set to 50", 50, customerBaseLoadInstance.getValue());
			}


			EasyTravelConfig.applyCustomSettings(Collections.singletonMap("config.customerLoadScenario", "EasyTravel"));
			assertEquals(CustomerTrafficScenarioEnum.EasyTravel, EasyTravelConfig.read().customerLoadScenario);
			scenario = customerBaseLoadInstance.getScenario();
			assertTrue("Traffic scenario should be set to EasyTravelCustomer but was: " + scenario.getClass().getName(), (scenario instanceof EasyTravelCustomer));
			//check if load value is the same
			if (!useLoadIncScenario) {
				assertEquals("Load should be set to 50", 50, customerBaseLoadInstance.getValue());
			}
		} finally {
			EasyTravelConfig.resetSingleton();
		}
	}


	@Test
	public void testGetCustomerLoadScenarioWithConfigWatcher() {
		checkgetCustomerLoadScenario(false);
	}

	@Test
	public void testGetCustomerLoadScenarioWithLoadIncreaser() {
		checkgetCustomerLoadScenario(true);
	}

	@Test
	public void testIsLoadIncreaseScenario() {
		Scenario scenario = EasyMock.createNiceMock(Scenario.class);
		EasyMock.expect(scenario.getTitle()).andReturn(MessageConstants.TESTCENTER_SCENARIO_INCREASING_LOAD_TITLE).anyTimes();
		EasyMock.replay(scenario);
		assertTrue("Is load increasing scenario", BaseLoadManager.isLoadIncreasingScenario(scenario));

		EasyMock.reset(scenario);
		EasyMock.expect(scenario.getTitle()).andReturn(MessageConstants.DEVTEAM_SCENARIO_LOADTESTING_TITLE).anyTimes();
		EasyMock.replay(scenario);
		assertTrue("Is load increasing scenario", BaseLoadManager.isLoadIncreasingScenario(scenario));

		EasyMock.reset(scenario);
		EasyMock.expect(scenario.getTitle()).andReturn(MessageConstants.UEM_SCENARIO_DEFAULT_TITLE).anyTimes();
		EasyMock.replay(scenario);
		assertFalse("Is load increasing scenario", BaseLoadManager.isLoadIncreasingScenario(scenario));

	}
}
