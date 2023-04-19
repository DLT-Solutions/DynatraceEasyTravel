package com.dynatrace.easytravel.spring;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.utils.TestHelpers;


public class AbstractPluginTest {
	@After
	public void tearDown() {
		EasyTravelConfig.resetSingleton();
	}

	@Test
	public void test() {
		AbstractPlugin plugin = new AbstractPlugin("name", "groupname", "compat", "description");
		assertEquals("compat", plugin.getCompatibility());

		plugin = new AbstractPlugin("name", "groupname", null, "description");
		assertEquals("Both", plugin.getCompatibility());
		plugin.setCompatibility("other");

		plugin.setName("somename");
		assertEquals("somename", plugin.getName());

		plugin.setGroupName("group1");
		assertEquals("group1", plugin.getGroupName());

		plugin.setDescription("somedesc");
		assertEquals("somedesc", plugin.getDescription());

		assertFalse(plugin.isEnabled());
		plugin.setEnabled(true);
		assertTrue(plugin.isEnabled());
		assertNull(plugin.getPluginDependencies());
		assertTrue(plugin.isActivatable());

		plugin = new AbstractPlugin("name", "groupname", "description");
		assertEquals("name", plugin.getName());
		assertEquals("groupname", plugin.getGroupName());
		assertEquals("description", plugin.getDescription());

		// empty should be accepted
		plugin.setDependencies(new String[] {});
		assertEquals(0, plugin.getDependencies().length);

		TestHelpers.ToStringTest(plugin);
		TestHelpers.ToStringTest(new AbstractPlugin());		// also check toString() for empty class

		assertTrue(plugin.isActivatable());
		plugin.setDependencies(new String[] {"somedep", BaseConstants.BusinessBackend.Persistence.CASSANDRA});
		assertFalse(plugin.isActivatable());

		assertNull("no hosts initially", plugin.getHosts());
		assertTrue("always enabled if no host is set", plugin.isEnabledFor("somedummyhost"));
		assertTrue("enabled for current host as well", plugin.isEnabledForCurrentHost());

		// now set some hosts
		plugin.setHosts(new String[] {"someotherhost", null});
		assertFalse("Still not enabled as official host is not set in config", plugin.isEnabledForCurrentHost());

		EasyTravelConfig.read().officialHost = "myhost";
		assertFalse("not enabled if enabled host and official host differ", plugin.isEnabledForCurrentHost());

		EasyTravelConfig.read().officialHost = "someotherhost";
		assertTrue("finally enabled because official host and enabled host match", plugin.isEnabledForCurrentHost());

		assertFalse(plugin.isEnabledFor(null));
		assertFalse(plugin.isEnabledFor("anynonlistedhost"));
		assertTrue(plugin.isEnabledFor("someotherhost"));
	}

}
