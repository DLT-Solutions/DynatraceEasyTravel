package com.dynatrace.easytravel.spring;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.plugins.PluginServiceTestBase;
import com.dynatrace.easytravel.utils.MockRESTServer;


public class RemotePluginServiceAPMTest extends PluginServiceTestBase {
	private static final String PLUGIN_NAME_A = "Plugin A";
	private static final String PLUGIN_NAME_B = "Plugin B";
	private static final String PLUGIN_A_DATA = PLUGIN_NAME_A + ":Plugin Group A:APM:Plugin A Description";
	private static final String PLUGIN_B_DATA = PLUGIN_NAME_B + ":Plugin Group B:APM:Plugin B Description";
	private static final String[] PLUGINS = new String[] { PLUGIN_A_DATA, PLUGIN_B_DATA };

	@Override
	protected InstallationType getInstallationType() {
		return InstallationType.APM;
	}

	@Test
	public void testTemplateConfigWhenInAPMMode() {
		MockRESTServer server = null;
		RemotePluginService remotePluginService = new RemotePluginService(HOST, ServicePort);

		try {
			MyHttpRunnable runnable = new MyHttpRunnable();
			server = new MockRESTServer(runnable);
			configureEasytravel(server);

			String templateJson = createTemplateConfigurationJson(PLUGIN_NAME_A);

			remotePluginService.registerPlugins(PLUGINS);
			remotePluginService.setPluginTemplateConfiguration(templateJson);

			assertEquals(0, runnable.getServerContactCount());

			remotePluginService.setPluginEnabled(PLUGIN_NAME_B, true);
			assertEquals(0, runnable.getServerContactCount());

			remotePluginService.setPluginEnabled(PLUGIN_NAME_A, true);
			assertEquals(1, runnable.getServerContactCount());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			EasyTravelConfig.resetSingleton();
			if (server != null) {
				server.stop();
			}
		}
	}
}
