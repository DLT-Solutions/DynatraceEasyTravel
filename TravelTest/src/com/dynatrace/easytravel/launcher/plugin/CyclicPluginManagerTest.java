package com.dynatrace.easytravel.launcher.plugin;

import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.builder.SettingBuilder;
import com.dynatrace.easytravel.plugin.RemotePluginController;

@RunWith(MockitoJUnitRunner.class)
public class CyclicPluginManagerTest {
	private DefaultProcedureMapping mappings = new DefaultProcedureMapping(Constants.Procedures.BUSINESS_BACKEND_ID);

	@Mock
	RemotePluginController controller/* = mock(RemotePluginController.class)*/;

	@Test
	public void testNoPlugins() throws Exception {
		CyclicPluginManager mgr = new CyclicPluginManager("test-cyclic",
				mappings, controller);
		mgr.start();

		// without plugins the thread should stop automatically: mgr.setKeeprunning(false); 
		//

		mgr.join();

		verify(controller, never()).sendEnabled(Constants.Plugin.JourneyUpdateSlow, false, null);
		verify(controller, never()).sendEnabled(Constants.Plugin.JourneyUpdateSlow, true, null);
	}

	@Test
	public void testPlugins() throws Exception {
		mappings.addSetting(
				SettingBuilder.plugin(Constants.Plugin.JourneyUpdateSlow).
					setStayOffDuration(1).
					setStayOnDuration(2000).
					disable().
					create());

		CyclicPluginManager mgr = new CyclicPluginManager("test-cyclic",
				mappings, controller);
		mgr.start();

		// no wait needed here: Thread.sleep(1000);
		mgr.setKeeprunning(false);

		mgr.join();

		verify(controller, never()).sendEnabled(Constants.Plugin.JourneyUpdateSlow, true, null);
		verify(controller, never()).sendEnabled(Constants.Plugin.JourneyUpdateSlow, false, null);
	}

	@Test
	public void testPluginsAndWait() throws Exception {
		mappings.addSetting(
				SettingBuilder.plugin(Constants.Plugin.JourneyUpdateSlow).
					setStayOffDuration(1).
					setStayOnDuration(2000).
					disable().
					create());

		when(controller.sendEnabled(Constants.Plugin.JourneyUpdateSlow, true, null)).thenReturn("Ok");

		CyclicPluginManager mgr = new CyclicPluginManager("test-cyclic",
				mappings, controller);
		mgr.start();
		// wait for the call
		Thread.sleep(1500);
		mgr.setKeeprunning(false);

		mgr.join();

		// now expect the enabled call
		verify(controller).sendEnabled(Constants.Plugin.JourneyUpdateSlow, true, null);
	}

	@Test
	public void testPluginsAndWaitOnAndOff() throws Exception {
		mappings.addSetting(
				SettingBuilder.plugin(Constants.Plugin.JourneyUpdateSlow).
					setStayOffDuration(1).
					setStayOnDuration(1).
					disable().
					create());

		when(controller.sendEnabled(Constants.Plugin.JourneyUpdateSlow, true, null)).thenReturn("Ok");
		when(controller.sendEnabled(Constants.Plugin.JourneyUpdateSlow, false, null)).thenReturn("Ok");

		CyclicPluginManager mgr = new CyclicPluginManager("test-cyclic",
				mappings, controller);
		mgr.start();
		// wait for the call
		Thread.sleep(3500);
		mgr.setKeeprunning(false);

		mgr.join();

		// now expect the enabled call
		verify(controller, times(2)).sendEnabled(Constants.Plugin.JourneyUpdateSlow, true, null);
		verify(controller, times(1)).sendEnabled(Constants.Plugin.JourneyUpdateSlow, false, null);
	}

	@Test
	public void testConstructor() {
		// just to cover this constructor as well
		new CyclicPluginManager(mappings);
	}

	@Test
	public void testPluginsThrows() throws Exception {
		mappings.addSetting(
				SettingBuilder.plugin(Constants.Plugin.JourneyUpdateSlow).
					setStayOffDuration(1).
					setStayOnDuration(2000).
					disable().
					create());

		when(controller.sendEnabled(Constants.Plugin.JourneyUpdateSlow, true, null)).thenThrow(new RuntimeException("TestException"));

		CyclicPluginManager mgr = new CyclicPluginManager("test-cyclic",
				mappings, controller);
		mgr.start();
		// wait for the call
		Thread.sleep(1500);
		mgr.setKeeprunning(false);

		mgr.join();

		// now expect the enabled call
		verify(controller).sendEnabled(Constants.Plugin.JourneyUpdateSlow, true, null);
	}

	@Test
	public void testPluginsAndWaitOnAndOffLonger() throws Exception {
		mappings.addSetting(
				SettingBuilder.plugin(Constants.Plugin.JourneyUpdateSlow).
					setStayOffDuration(1).
					setStayOnDuration(3).
					disable().
					create());

		when(controller.sendEnabled(Constants.Plugin.JourneyUpdateSlow, true, null)).thenReturn("Ok");
		when(controller.sendEnabled(Constants.Plugin.JourneyUpdateSlow, false, null)).thenReturn("Ok");

		CyclicPluginManager mgr = new CyclicPluginManager("test-cyclic",
				mappings, controller);
		mgr.start();
		// wait for the call
		Thread.sleep(5000);
		mgr.setKeeprunning(false);

		mgr.join();

		// now expect the enabled call
		verify(controller, times(1)).sendEnabled(Constants.Plugin.JourneyUpdateSlow, true, null);
		verify(controller, times(1)).sendEnabled(Constants.Plugin.JourneyUpdateSlow, false, null);
	}

	@Test
	public void testPluginsAndWaitOffAndOn() throws Exception {
		mappings.addSetting(
				SettingBuilder.plugin(Constants.Plugin.JourneyUpdateSlow).
					setStayOffDuration(1).
					setStayOnDuration(1).
					enable().
					create());

		when(controller.sendEnabled(Constants.Plugin.JourneyUpdateSlow, true, null)).thenReturn("Ok");
		when(controller.sendEnabled(Constants.Plugin.JourneyUpdateSlow, false, null)).thenReturn("Ok");

		CyclicPluginManager mgr = new CyclicPluginManager("test-cyclic",
				mappings, controller);
		mgr.start();
		// wait for the call
		Thread.sleep(3500);
		mgr.setKeeprunning(false);

		mgr.join();

		// now expect the enabled call
		verify(controller, times(1)).sendEnabled(Constants.Plugin.JourneyUpdateSlow, true, null);
		verify(controller, times(2)).sendEnabled(Constants.Plugin.JourneyUpdateSlow, false, null);
	}

	/*@Test
	public void test() throws Exception {
		testNoPlugins();
		reset(controller);
		testPlugins();
		reset(controller);
		testConstructor();
		reset(controller);
		testPluginsThrows();
		reset(controller);
		testPluginsAndWaitOnAndOffLonger();
	}*/
}
