package com.dynatrace.easytravel.spring;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.dynatrace.easytravel.plugins.PluginServiceTestBase;
import com.dynatrace.easytravel.utils.TestHelpers;


public class RemotePluginServiceTest extends PluginServiceTestBase {
//    private static final String installationType = InstallationType.APM.name();
	private static final String PLUGIN_NAME_A = "Plugin A";
	private static final String PLUGIN_NAME_B = "Plugin B";
	private static final String PLUGIN_NAME_C = "Plugin C";
	private static final String PLUGIN_A_DATA = PLUGIN_NAME_A + ":Plugin Group A:Classic:Plugin A Description";
	private static final String PLUGIN_B_DATA = PLUGIN_NAME_B + ":Plugin Group B:Classic:Plugin B Description";
	private static final String PLUGIN_C_DATA = PLUGIN_NAME_C + ":Plugin Group C:Classic:Plugin C Description";

	private static final String[] PLUGIN_NAMES = new String[] { PLUGIN_NAME_A, PLUGIN_NAME_B, PLUGIN_NAME_C };
	private static final String[] PLUGINS = new String[] { PLUGIN_A_DATA, PLUGIN_B_DATA, PLUGIN_C_DATA };

	@Test
	public void testRegisterPlugins() throws Exception {
		RemotePluginService remotePluginService = new RemotePluginService(HOST, ServicePort);
		remotePluginService.registerPlugins(PLUGINS);
	}

	@Test
	public void testGetAllPluginNames() throws Exception {
		RemotePluginService remotePluginService = new RemotePluginService(HOST, ServicePort);
		remotePluginService.registerPlugins(PLUGINS);
		assertArrayEquals(PLUGIN_NAMES, remotePluginService.getAllPluginNames());
	}

	@Test
	public void testGetEnabledPluginNames() throws Exception {
		RemotePluginService remotePluginService = new RemotePluginService(HOST, ServicePort);

		remotePluginService.registerPlugins(PLUGINS);
		assertArrayEquals(new String[0], remotePluginService.getEnabledPluginNames());

		remotePluginService.setPluginEnabled(PLUGIN_NAME_A, true);
		assertArrayEquals(new String[] { PLUGIN_NAME_A }, remotePluginService.getEnabledPluginNames());

		remotePluginService.setPluginEnabled(PLUGIN_NAME_A, false);
		assertArrayEquals(new String[0], remotePluginService.getEnabledPluginNames());

		remotePluginService.setPluginEnabled(PLUGIN_NAME_A, true);
		remotePluginService.setPluginEnabled(PLUGIN_NAME_B, true);
		assertArrayEquals(new String[] { PLUGIN_NAME_A, PLUGIN_NAME_B }, remotePluginService.getEnabledPluginNames());

	}

	@Test
	public void testGetAllPlugins() throws Exception {
		RemotePluginService remotePluginService = new RemotePluginService(HOST, ServicePort);

		remotePluginService.registerPlugins(PLUGINS);
		assertArrayEquals(PLUGINS, remotePluginService.getAllPlugins());
	}

	@Test
	public void testGetEnabledPlugins() throws Exception {
		RemotePluginService remotePluginService = new RemotePluginService(HOST, ServicePort);

		remotePluginService.registerPlugins(PLUGINS);
		assertArrayEquals(new String[0], remotePluginService.getEnabledPlugins());

		remotePluginService.setPluginEnabled(PLUGIN_NAME_A, true);
		assertArrayEquals(new String[] { PLUGIN_A_DATA }, remotePluginService.getEnabledPlugins());

		remotePluginService.setPluginEnabled(PLUGIN_NAME_A, false);
		assertArrayEquals(new String[0], remotePluginService.getEnabledPlugins());

		remotePluginService.setPluginEnabled(PLUGIN_NAME_A, true);
		remotePluginService.setPluginEnabled(PLUGIN_NAME_B, true);
		assertArrayEquals(new String[] { PLUGIN_A_DATA, PLUGIN_B_DATA }, remotePluginService.getEnabledPlugins());
	}

	@Test
	public void testHost() throws Exception {
		RemotePluginService remotePluginService = new RemotePluginService(HOST, ServicePort);

		remotePluginService.registerPlugins(PLUGINS);
		assertArrayEquals(new String[0], remotePluginService.getEnabledPluginsForHost("somehost"));
		assertArrayEquals(new String[0], remotePluginService.getEnabledPluginNamesForHost("somehost"));

		remotePluginService.setPluginEnabled(PLUGIN_NAME_A, true);
		assertArrayEquals(new String[] { PLUGIN_A_DATA }, remotePluginService.getEnabledPluginsForHost("somehost"));
		assertArrayEquals(new String[] { PLUGIN_NAME_A }, remotePluginService.getEnabledPluginNamesForHost("somehost"));

		remotePluginService.setPluginEnabled(PLUGIN_NAME_A, false);
		assertArrayEquals(new String[0], remotePluginService.getEnabledPluginsForHost("somehost"));
		assertArrayEquals(new String[0], remotePluginService.getEnabledPluginNamesForHost("somehost"));

		remotePluginService.setPluginEnabled(PLUGIN_NAME_A, true);
		remotePluginService.setPluginEnabled(PLUGIN_NAME_B, true);
		assertArrayEquals(new String[] { PLUGIN_A_DATA, PLUGIN_B_DATA }, remotePluginService.getEnabledPluginsForHost("somehost"));
		assertArrayEquals(new String[] { PLUGIN_NAME_A, PLUGIN_NAME_B }, remotePluginService.getEnabledPluginNamesForHost("somehost"));

		// plugin_a is gone for "somehost" if we register it for only "someotherhost"
		remotePluginService.setPluginHosts(PLUGIN_NAME_A, new String[] { "someotherhost" });
		assertArrayEquals(new String[] { PLUGIN_B_DATA }, remotePluginService.getEnabledPluginsForHost("somehost"));
		assertArrayEquals(new String[] { PLUGIN_NAME_B }, remotePluginService.getEnabledPluginNamesForHost("somehost"));
	}

	@Test
	public void testGetFails() throws Exception {
		// shut down service before trying to use it to get an exception
		// retrieveData(RESTConstants.SHUTDOWN);
		shutdown(); // this is how we shut it down now, with Tomcat
		waitForServiceToStop();

		RemotePluginService remotePluginService = new RemotePluginService(HOST, ServicePort);

		// this one swallows Exceptions!?
		remotePluginService.registerPlugins(PLUGINS);

		try {
			remotePluginService.getAllPlugins();
			fail("Should catch an exception here");
		} catch (RuntimeException e) {
			// make sure we have necessary information in the error message
			TestHelpers.assertContains(e, HOST, Integer.toString(ServicePort));
		}
	}
}
