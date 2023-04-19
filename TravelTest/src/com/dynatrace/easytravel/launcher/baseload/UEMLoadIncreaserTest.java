package com.dynatrace.easytravel.launcher.baseload;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.easymock.internal.matchers.Any;
import org.junit.Test;

import com.dynatrace.easytravel.config.CustomerTrafficScenarioEnum;
import com.dynatrace.easytravel.launcher.baseload.UEMLoadModificator.UEMLoadModificatorType;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.panels.HeaderPanelInterface;
import com.dynatrace.easytravel.utils.TestHelpers;


public class UEMLoadIncreaserTest {

	@Test
	public void testEnableDisable() {
		LoadController loadController = createMock(LoadController.class);
		HeaderPanelInterface panel = createStrictMock(HeaderPanelInterface.class);

		expectLoadController(loadController, panel);

		expectEnable(panel);

		replay(loadController, panel);

		UEMLoadIncreaser inc = new UEMLoadIncreaser(loadController);

		// not used at all right now...
		inc.notifyConfigLoaded(null, null);

		assertFalse("Not running initially", inc.isRunning());

		inc.enable();

		assertFalse("Not running after being enabled", inc.isRunning());

		assertEquals(UEMLoadModificatorType.LOAD_INCREASER, inc.getType());

		verify(loadController, panel);

		// now disable
		reset(loadController, panel);

		expectLoadController(loadController, panel);

		expectDisable(panel);

		replay(loadController, panel);

		inc.disable();

		assertFalse("Not running after being disabled", inc.isRunning());

		verify(loadController, panel);
	}

	@Test
	public void testDisableWithoutEnable() {
		LoadController loadController = createMock(LoadController.class);
		HeaderPanelInterface panel = createStrictMock(HeaderPanelInterface.class);

		expectLoadController(loadController, panel);

		replay(loadController, panel);

		UEMLoadIncreaser inc = new UEMLoadIncreaser(loadController);

		// just to ensure that disable without prior enable does not fail
		inc.disable();

		verify(loadController, panel);
	}

	@Test
	public void testStart() {
		LoadController loadController = createMock(LoadController.class);
		HeaderPanelInterface panel = createStrictMock(HeaderPanelInterface.class);

		CustomerBaseLoad load = expectLoadController(loadController, panel);

		// set up so that we have hosts
		load.addHost2Scenario("somehost");
		assertTrue(load.hasHost());

		expectEnable(panel);

		expectDisable(panel);

		replay(loadController, panel);

		UEMLoadIncreaser inc = new UEMLoadIncreaser(loadController);
		inc.enable();

		assertFalse("Not running after being enabled", inc.isRunning());
		inc.start();
		assertTrue("Running after being started", inc.isRunning());

		inc.disable();

		assertFalse("Not running after being disabled", inc.isRunning());

		verify(loadController, panel);
	}

	@Test
	public void testStartFails() {
		LoadController loadController = createMock(LoadController.class);
		HeaderPanelInterface panel = createStrictMock(HeaderPanelInterface.class);

		expectLoadController(loadController, panel);

		expectEnable(panel);

		expectDisable(panel);

		replay(loadController, panel);

		UEMLoadIncreaser inc = new UEMLoadIncreaser(loadController);
		inc.enable();

		assertFalse("Not running after being enabled", inc.isRunning());
		inc.start();
		assertFalse("Not running after being started because cannot start because no hosts", inc.isRunning());

		inc.disable();

		assertFalse("Not running after being disabled", inc.isRunning());

		verify(loadController, panel);
	}

	@Test
	public void testNotify() {
		LoadController loadController = createMock(LoadController.class);
		HeaderPanelInterface panel = createMock(HeaderPanelInterface.class);

		CustomerBaseLoad load = expectLoadController(loadController, panel);

		// set up so that we have hosts
		load.addHost2Scenario("somehost");
		assertTrue(load.hasHost());

		panel.deactivateUEMLoadPanel();
		expectLastCall().anyTimes();
		panel.enableTaggedWebRequest();
		expectLastCall().anyTimes();
		panel.setLoad(0);
		expectLastCall().anyTimes();

		panel.activateUEMLoadPanel();
		expectLastCall().anyTimes();
		panel.resetTrafficLabel();
		expectLastCall().anyTimes();

		replay(loadController, panel);

		UEMLoadIncreaser inc = new UEMLoadIncreaser(loadController);
		inc.enable();

		assertFalse("Not running after being enabled", inc.isRunning());
		inc.notifyBatchStateChanged(null, null, State.OPERATING);
		assertTrue("Running after being started", inc.isRunning());
		inc.notifyBatchStateChanged(null, null, State.STOPPING);
		assertFalse("Not running after being disabled", inc.isRunning());
		inc.notifyBatchStateChanged(null, null, State.STOPPED);
		assertFalse("Not running after being disabled", inc.isRunning());
		inc.notifyBatchStateChanged(null, null, State.FAILED);
		assertFalse("Not running after being disabled", inc.isRunning());
		inc.notifyBatchStateChanged(null, null, State.TIMEOUT);
		assertTrue("Running after being started with TIMEOUT", inc.isRunning());

		verify(loadController, panel);
	}

	protected CustomerBaseLoad expectLoadController(LoadController loadController, HeaderPanelInterface panel) {
		CustomerBaseLoad customerBaseLoad = new CustomerBaseLoad(CustomerTrafficScenarioEnum.EasyTravel, 0, 1, true);

		expect(loadController.getCustomerLoadController()).andReturn(customerBaseLoad).anyTimes();
		expect(loadController.getB2bLoadController()).andReturn(new B2BBaseLoad(0, 1, true)).anyTimes();
		expect(loadController.getMobileNativeLoadController()).andReturn(new MobileBaseLoad(0, 1, false)).anyTimes();
		expect(loadController.getMobileBrowserLoadController()).andReturn(new MobileBrowserBaseLoad(0, 1, true)).anyTimes();
		expect(loadController.getHeaderPanel()).andReturn(panel).anyTimes();

		return customerBaseLoad;
	}


	@Test
	public void testStartWithLoadIncreaserException() throws Exception{
		LoadController loadController = createMock(LoadController.class);
		HeaderPanelInterface panel = createStrictMock(HeaderPanelInterface.class);

		CustomerBaseLoad load = expectLoadController(loadController, panel);

		// set up so that we have hosts
		load.addHost2Scenario("somehost");
		assertTrue(load.hasHost());

		expectEnable(panel);

		expectDisable(panel);

		replay(loadController, panel);

		UEMLoadIncreaser inc = new UEMLoadIncreaser(loadController);
		inc.enable();

		inc.setInitialDelay(1);
		inc.setPeriodBetweenRuns(1);

		assertFalse("Not running after being enabled", inc.isRunning());
		ScheduledFuture<?> future = inc.start();
		assertTrue("Running after being started", inc.isRunning());

		// let the schedule be executed
		Thread.sleep(1500);

		// check that it did run
		assertTrue(future.isDone());
		try {
			future.get();
			fail("Should report exception on the schedule");
		} catch (ExecutionException e) {
			TestHelpers.assertContains(e, "AssertionError");
		}

		inc.disable();

		assertFalse("Not running after being disabled", inc.isRunning());

		verify(loadController, panel);
	}

	@Test
	public void testStartWithLoadIncreaserWorks() throws Exception{
		LoadController loadController = createMock(LoadController.class);
		HeaderPanelInterface panel = createMock(HeaderPanelInterface.class);

		CustomerBaseLoad load = expectLoadController(loadController, panel);

		// set up so that we have hosts
		load.addHost2Scenario("somehost");
		assertTrue(load.hasHost());

		expectEnable(panel);

		// expect things in the LoadIncreaser
		panel.setLoad(anyInt());
		expectLastCall().anyTimes();
		panel.setTrafficLabel(anyString());
		expectLastCall().anyTimes();

		expectDisable(panel);


		replay(loadController, panel);

		UEMLoadIncreaser inc = new UEMLoadIncreaser(loadController);
		inc.enable();

		inc.setInitialDelay(1);
		inc.setPeriodBetweenRuns(1);

		assertFalse("Not running after being enabled", inc.isRunning());
		ScheduledFuture<?> future = inc.start();
		assertTrue("Running after being started", inc.isRunning());

		// let the schedule be executed
		Thread.sleep(1500);

		// check that it did run
		assertFalse("Not done as scheduled again", future.isDone());
		try {
			future.get(100, TimeUnit.MILLISECONDS);
			fail("Should timeout here");
		} catch (TimeoutException e) {
			// expected!
		}

		inc.disable();

		assertFalse("Not running after being disabled", inc.isRunning());

		verify(loadController, panel);
	}

	private String anyString() {
        reportMatcher(Any.ANY);
		return null;
	}

	protected void expectDisable(HeaderPanelInterface panel) {
		panel.activateUEMLoadPanel();
		panel.enableTaggedWebRequest();
		panel.resetTrafficLabel();
	}

	protected void expectEnable(HeaderPanelInterface panel) {
		panel.deactivateUEMLoadPanel();
		panel.enableTaggedWebRequest();
		panel.setLoad(0);
	}
}
