package com.dynatrace.easytravel.business.webservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.plugins.TemplateConfigurationTestBase;
import com.dynatrace.easytravel.spring.SpringUtils;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.MockRESTServer;


public class ConfigurationServiceAPMTest extends TemplateConfigurationTestBase {
	private static final String PLUGIN_NAME_A = "Plugin A";
	private static final String PLUGIN_NAME_B = "Plugin B";

	@BeforeClass
	public static void setUpClass() {
        SpringUtils.initBusinessBackendContextForTest();
	}

	@AfterClass
	public static void tearDownClass() {
		SpringUtils.disposeBusinessBackendContext();
	}

	@Before
	public void setUp() {
		DtVersionDetector.enforceInstallationType(InstallationType.APM);
	}

	@Test
	public void testTemplateConfiguration() throws IOException {
		MockRESTServer server = null;

		MyHttpRunnable runnable = new MyHttpRunnable();
		server = new MockRESTServer(runnable);
		configureEasytravel(server);

		String templateJson = createTemplateConfigurationJson(PLUGIN_NAME_A);


		ConfigurationService service = new ConfigurationService();
		assertEquals("pong", service.ping());

		service.setPluginTemplateConfiguration(templateJson);

		assertEquals("Had: " + Arrays.toString(service.getAllPluginNames()),
				6, service.getAllPluginNames().length);

		service.registerPlugins(new String[] { PLUGIN_NAME_A, PLUGIN_NAME_B });
		assertEquals("Had: " + Arrays.toString(service.getAllPluginNames()),
				8, service.getAllPluginNames().length);

		assertEquals(0, runnable.getServerContactCount());

		service.setPluginEnabled(PLUGIN_NAME_B, true);
		assertEquals(0, runnable.getServerContactCount());
		assertTrue("Had: " + Arrays.toString(service.getEnabledPluginNames()),
				ArrayUtils.contains(service.getEnabledPluginNames(), PLUGIN_NAME_B));

		service.setPluginEnabled(PLUGIN_NAME_A, true);
		assertEquals(1, runnable.getServerContactCount());
		assertTrue("Had: " + Arrays.toString(service.getEnabledPluginNames()),
				ArrayUtils.contains(service.getEnabledPluginNames(), PLUGIN_NAME_A));
	}
}
