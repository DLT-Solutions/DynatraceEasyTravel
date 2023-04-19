package com.dynatrace.easytravel.spring;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.plugins.PluginServiceTestBase;
import com.dynatrace.easytravel.util.DtVersionDetector;


public class SpringPluginStateProxyTest {
	private static final PluginServiceTestBase PLUGIN_SERVICE = new PluginServiceTestBase();
	private EasyTravelConfig config = EasyTravelConfig.read();

	@Before
	public void setUp() {
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
	}

	@After
	public void tearDown() {
		// restore default config
		EasyTravelConfig.resetSingleton();
	}

	@Test
	public void testLocal() {
		// for local plugin manager
		config.pluginServiceHost = null;

		checkMethods(6);
	}

	@Test
	public void testRemote() throws Exception {
		// for local plugin manager
		config.pluginServiceHost = "localhost";

		PLUGIN_SERVICE.prepareEasyTravelConfig();

		try {
			checkMethods(0);
		} finally {
			PLUGIN_SERVICE.shutdown();
		}
	}

	protected void checkMethods(int count) {
		SpringPluginStateProxy proxy = new SpringPluginStateProxy();

		// just ensure that none fails completely, some plugins are automatically registered via bootPlugins
		assertEquals(count, proxy.getAllPluginNames().length);
		assertEquals(count, proxy.getEnabledPluginNames().length);
		assertEquals(count, proxy.getEnabledPluginNamesForHost("somehost").length);
		assertEquals(count, proxy.getAllPlugins().length);
		assertEquals(count, proxy.getEnabledPlugins().length);
		assertEquals(count, proxy.getEnabledPluginsForHost("somehost").length);
		proxy.registerPlugins(new String[] {});
		proxy.setPluginEnabled("someplugin", false);
		proxy.setPluginHosts("someplugin", new String[] { "somehost" });
	}
}
