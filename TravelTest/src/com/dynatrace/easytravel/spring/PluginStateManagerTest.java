package com.dynatrace.easytravel.spring;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.plugins.TemplateConfigurationTestBase;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.MockRESTServer;

/**
 * @author cwpl-rpsciuk
 *
 */
public class PluginStateManagerTest extends TemplateConfigurationTestBase {
	private PluginStateManager manager;
	private static final String HOST1 = "myhost";
	private static final String HOST2 = "ohterHost";
	private static final String NAME = "testPlugin";
	private static final String PLUGIN = NAME + ":testGroup:Both:test plugin used in tests";

	private static final String PLUGIN_NAME_A = "Plugin A";
	private static final String PLUGIN_NAME_B = "Plugin B";
	private static final String PLUGIN_A_DATA = PLUGIN_NAME_A + ":Plugin Group A:APM:Plugin A Description";
	private static final String PLUGIN_B_DATA = PLUGIN_NAME_B + ":Plugin Group B:APM:Plugin B Description";

	@Before
	public void setup() {
		manager = new PluginStateManager();
	}

	@Test
	public void test() {
		//check if we can call some method on unregistred plugin
		manager.setPluginHosts(NAME, new String[] {HOST1});
		Assert.assertFalse(isPluginEnabled(NAME));
		manager.setPluginEnabled(NAME, true);
		Assert.assertFalse(isPluginEnabled(NAME));
		///register plugin
		manager.registerPlugins(new String[] {PLUGIN});
		List<String> allPlugins = Arrays.asList(manager.getAllPluginNames());
		assertTrue(allPlugins.contains (NAME));
		//set plugin hosts
		manager.setPluginHosts(NAME, new String[] {HOST1});
		//enable plugin
		assertFalse(isPluginEnabled(NAME));
		manager.setPluginEnabled(NAME, true);
		assertTrue(isPluginEnabled(NAME));
		//check if it is enabled/not enabled for given hosts
		assertTrue(isPluginEnabledForHost(NAME, HOST1));
		//should not be enabled here
		assertFalse(isPluginEnabledForHost(NAME, HOST2));
		//call registration one more time
		manager.registerPlugins(new String[] {PLUGIN});
		manager.setPluginHosts(NAME, new String[] {HOST1});
		//check again if it is enabled for given hosts
		assertTrue(isPluginEnabledForHost(NAME, HOST1));
		//should not be enabled here
		assertFalse(isPluginEnabledForHost(NAME, HOST2));
		//disable plugin
		manager.setPluginEnabled(NAME, false);
		assertFalse(isPluginEnabledForHost(NAME, HOST1));
	}

	private boolean isPluginEnabled (String pluginName) {
		List<String> enabledPlugins = Arrays.asList(manager.getEnabledPluginNames());
		return enabledPlugins.contains(pluginName);
	}

	private boolean isPluginEnabledForHost (String pluginName, String host) {
		List<String> enabledPlugins = Arrays.asList(manager.getEnabledPluginNamesForHost(host));
		return enabledPlugins.contains(pluginName);
	}

	@Test
	public void testTemplateConfiguration() throws IOException {
		MockRESTServer server = null;
		DtVersionDetector.enforceInstallationType(InstallationType.APM);

		MyHttpRunnable runnable = new MyHttpRunnable();
		server = new MockRESTServer(runnable);
		configureEasytravel(server);

		String templateJson = createTemplateConfigurationJson(PLUGIN_NAME_A);

		manager.setPluginTemplateConfiguration(templateJson);
		manager.registerPlugins(new String[] { PLUGIN_A_DATA, PLUGIN_B_DATA });
		List<String> allPlugins = Arrays.asList(manager.getAllPluginNames());
		assertTrue(allPlugins.contains (PLUGIN_NAME_A));
		assertTrue(allPlugins.contains (PLUGIN_NAME_B));

		assertEquals(0, runnable.getServerContactCount());

		manager.setPluginEnabled(PLUGIN_NAME_B, true);
		assertTrue(isPluginEnabled(PLUGIN_NAME_B));
		assertEquals(0, runnable.getServerContactCount());

		manager.setPluginEnabled(PLUGIN_NAME_A, true);
		assertTrue(isPluginEnabled(PLUGIN_NAME_A));
		assertEquals(1, runnable.getServerContactCount());
	}
}
