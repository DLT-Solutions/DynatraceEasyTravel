package com.dynatrace.easytravel.spring;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import com.dynatrace.easytravel.DummyPaymentService;
import com.dynatrace.easytravel.ipc.NamedPipeNativeApplication;
import com.dynatrace.easytravel.ipc.SocketNativeApplication;


public class PluginHolderTest extends SpringTestBase {

	@Test
	public void test() {
		PluginHolder holder = new PluginHolder();
		assertNull(holder.getPlugins());

		List<Plugin> plugins = Collections.emptyList();
		holder.setPlugins(plugins);
		assertEquals(0, holder.getPlugins().size());

		Plugin plugin = new NamedPipeNativeApplication();
		holder.setPlugins(Collections.singletonList(plugin));
		assertEquals(1, holder.getPlugins().size());
		assertEquals(plugin.getName(), holder.getPlugins().get(0).getName());

		plugin = new DummyPaymentService();
		holder.setPlugins(Collections.singletonList(plugin));
		assertEquals(1, holder.getPlugins().size());
		assertEquals(plugin.getName(), holder.getPlugins().get(0).getName());

		String registeredPlugins = ArrayUtils.toString(SpringUtils.getPluginStateProxy().getAllPluginNames());
		assertFalse("DummyPaymentService is not yet registered",
				registeredPlugins.contains("DummyPaymentService"));
		holder.registerPlugins();
		registeredPlugins = ArrayUtils.toString(SpringUtils.getPluginStateProxy().getAllPluginNames());
		assertTrue("Now DummyPaymentService is registered as well",
				registeredPlugins.contains("DummyPaymentService"));

		plugin = new SocketNativeApplication() {
			@Override
			public boolean isActivatable() {
				return false;
			}
		};

		holder.addPlugin(plugin);
		assertEquals(1, holder.getPlugins().size());

		plugin = new SocketNativeApplication() {
			@Override
			public boolean isActivatable() {
				return true;
			}
		};
		holder.addPlugin(plugin);
		assertEquals(2, holder.getPlugins().size());
	}
}

