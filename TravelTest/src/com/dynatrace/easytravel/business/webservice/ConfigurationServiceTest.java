package com.dynatrace.easytravel.business.webservice;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.spring.SpringUtils;
import com.dynatrace.easytravel.util.DtVersionDetector;


public class ConfigurationServiceTest {
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
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
	}

	@Test
	public void test() {
		ConfigurationService service = new ConfigurationService();

		assertEquals("pong", service.ping());

		assertNull(service.getExternalUrl());
		service.setExternalUrl("someurl");
		assertEquals("someurl", service.getExternalUrl());

		assertFalse(service.isMemoryLeakEnabled());
		service.setMemoryLeakEnabled(true);
		assertTrue(service.isMemoryLeakEnabled());

		assertFalse(service.isDBSpammingEnabled());
		service.setDBSpammingEnabled(true);
		assertTrue(service.isDBSpammingEnabled());

		assertFalse(service.isFrontendDeadlockEnabled());
		service.setFrontendDeadlockEnabled(true);
		assertTrue(service.isFrontendDeadlockEnabled());

		assertEquals(0, service.getBackendCPUCycleTime());
		service.setBackendCPUCycleTime(100);
		assertEquals(100, service.getBackendCPUCycleTime());

		assertEquals("Had: " + Arrays.toString(service.getAllPluginNames()),
				6, service.getAllPluginNames().length);
		assertTrue("Had: " + Arrays.toString(service.getAllPluginNames()),
				ArrayUtils.contains(service.getAllPluginNames(), "DatabaseCleanup"));

		service.registerPlugins(new String[] {"plugin"});
		assertEquals("Had: " + Arrays.toString(service.getAllPluginNames()),
				7, service.getAllPluginNames().length);
		assertTrue("Had: " + Arrays.toString(service.getAllPluginNames()),
				ArrayUtils.contains(service.getAllPluginNames(), "plugin"));

		service.registerPlugins(new String[] {"hostplugin"});
		assertEquals("Had: " + Arrays.toString(service.getAllPluginNames()),
				8, service.getAllPluginNames().length);
		assertTrue("Had: " + Arrays.toString(service.getAllPluginNames()),
				ArrayUtils.contains(service.getAllPluginNames(), "hostplugin"));

		assertFalse("Had: " + Arrays.toString(service.getEnabledPluginNames()),
				ArrayUtils.contains(service.getEnabledPluginNames(), "plugin"));
		service.setPluginEnabled("plugin", true);
		assertTrue("Had: " + Arrays.toString(service.getEnabledPluginNames()),
				ArrayUtils.contains(service.getEnabledPluginNames(), "plugin"));

		assertFalse("Had: " + Arrays.toString(service.getEnabledPluginNamesForHost("host1")),
				ArrayUtils.contains(service.getEnabledPluginNamesForHost("host1"), "hostplugin"));
		assertFalse("Had: " + Arrays.toString(service.getEnabledPluginNamesForHost("host3")),
				ArrayUtils.contains(service.getEnabledPluginNamesForHost("host3"), "hostplugin"));
		service.setPluginEnabled("hostplugin", true);
		service.setPluginHosts("hostplugin", new String[] {"host1", "host2"});
		assertTrue("Had: " + Arrays.toString(service.getEnabledPluginNamesForHost("host1")),
				ArrayUtils.contains(service.getEnabledPluginNamesForHost("host1"), "hostplugin"));
		assertFalse("Had: " + Arrays.toString(service.getEnabledPluginNamesForHost("host3")),
				ArrayUtils.contains(service.getEnabledPluginNamesForHost("host3"), "hostplugin"));

		assertNotNull(service.getAllPlugins());
		assertNotNull(service.getEnabledPluginsForHost("host1"));
		assertNotNull(service.getEnabledPlugins());
	}
}
