package com.dynatrace.easytravel.plugins;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.net.UrlUtils;
import com.dynatrace.easytravel.utils.MockRESTServer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PluginServiceAPMTest extends PluginServiceTestBase {
	private static final String PLUGIN_NAME_A = "Plugin A";
	private static final String PLUGIN_NAME_B = "Plugin B";
	private static final String PLUGIN_A_DATA = PLUGIN_NAME_A + ":Plugin Group A:APM:Plugin A Description";
	private static final String PLUGIN_B_DATA = PLUGIN_NAME_B + ":Plugin Group B:APM:Plugin B Description";

	private static final int TIMEOUT = 30000;

	@Override
	protected InstallationType getInstallationType() {
		return InstallationType.APM;
	}

	@Test
	public void testSetAndGetTemplateConfig() throws Exception {
		MockRESTServer server = null;
		ObjectMapper mapper = new ObjectMapper();

		MyHttpRunnable runnable = new MyHttpRunnable();
		server = new MockRESTServer(runnable);
		configureEasytravel(server);

		String templateJson = createTemplateConfigurationJson(PLUGIN_NAME_A);
		new PluginService().setPluginTemplateConfiguration(templateJson);

		assertEquals("[]", new PluginService().getAllPluginNames());
		assertEquals("[]", new PluginService().getEnabledPluginNames());
		assertEquals("[]", UrlUtils.retrieveData(getURL(PluginService.CONTEXT + "/getEnabledPlugins").toExternalForm(), TIMEOUT));

		new PluginService().registerPlugins(mapper.writeValueAsString(new String[] { PLUGIN_A_DATA, PLUGIN_B_DATA }));
		assertEquals("[]", UrlUtils.retrieveData(getURL(PluginService.CONTEXT + "/getEnabledPlugins").toExternalForm(), TIMEOUT));

		assertEquals(0, runnable.getServerContactCount());

		new PluginService().setPluginEnabled(PLUGIN_NAME_B, true);
		assertEquals(mapper.writeValueAsString(new String[] { PLUGIN_B_DATA }), UrlUtils.retrieveData(getURL(PluginService.CONTEXT + "/getEnabledPlugins").toExternalForm(), TIMEOUT));
		assertEquals(0, runnable.getServerContactCount());

		new PluginService().setPluginEnabled(PLUGIN_NAME_A, true);
		assertEquals(mapper.writeValueAsString(new String[] { PLUGIN_A_DATA, PLUGIN_B_DATA }), UrlUtils.retrieveData(getURL(PluginService.CONTEXT + "/getEnabledPlugins").toExternalForm(), TIMEOUT));
		assertEquals(1, runnable.getServerContactCount());
	}
}
