package com.dynatrace.easytravel.spring;

import org.junit.Assert;

import org.junit.Test;


public class DynamicPluginTest extends SpringTestBase {

	@Test
	public void testDynamicRegisterAndEnable() {
		MyDynamicTestPlugin plugin = new MyDynamicTestPlugin();
		plugin.setExtensionPoint(new String[] { "my.test.location" });
		plugin.setEnabled(true);
		SpringUtils.getPluginHolder().addPlugin(plugin);
		PluginLifeCycle.executeAll("my.test.location");

		Assert.assertTrue("Expecting the plugin to be executed", plugin.beenExecuted);
	}

	private MyDynamicTestPlugin addAndEnable(String[] extensionPoint) {
		MyDynamicTestPlugin plugin = new MyDynamicTestPlugin();
		plugin.setExtensionPoint(extensionPoint);
		SpringUtils.getPluginHolder().addPlugin(plugin);
		SpringUtils.getPluginStateProxy().setPluginEnabled(plugin.getName(), true);
		return plugin;
	}

	private void reset(MyDynamicTestPlugin[] plugins) {
		for (MyDynamicTestPlugin plugin : plugins) {
			plugin.beenExecuted = false;
		}
	}

	@Test
	public void testPluginList() {
		MyDynamicTestPlugin[] plugins = {
				addAndEnable(new String[] {"test.location"}),
				addAndEnable(new String[] {"test"}),
				addAndEnable(new String[] {"test.*"}),
				addAndEnable(new String[] {"test.location.*"}),
				addAndEnable(new String[] {"test.location.sub"} )
		};

		GenericPluginList list = new GenericPluginList("test.location");
		Assert.assertEquals("Exepcting 4 plugins in list: " + list, 4, list.size());

		reset(plugins);
		list.execute("test.location");

		Assert.assertTrue("Expecting the plugins[0] to be executed", plugins[0].beenExecuted);
		Assert.assertFalse("Expecting the plugins[1] to not be executed", plugins[1].beenExecuted);
		Assert.assertTrue("Expecting the plugins[2] to be executed", plugins[2].beenExecuted);
		Assert.assertTrue("Expecting the plugins[3] to be executed", plugins[3].beenExecuted);
		Assert.assertFalse("Expecting the plugins[4] to not be executed", plugins[4].beenExecuted);

		reset(plugins);
		list.execute("test.location.sub");

		Assert.assertFalse("Expecting the plugins[0] to not be executed", plugins[0].beenExecuted);
		Assert.assertFalse("Expecting the plugins[1] to not be executed", plugins[1].beenExecuted);
		Assert.assertTrue("Expecting the plugins[2] to be executed", plugins[2].beenExecuted);
		Assert.assertTrue("Expecting the plugins[3] to be executed", plugins[3].beenExecuted);
		Assert.assertTrue("Expecting the plugins[4] to be executed", plugins[4].beenExecuted);

		list = new GenericPluginList("test");
		Assert.assertEquals("Exepcting 5 plugins in list", 5, list.size());

		reset(plugins);
		list.execute("test");

		Assert.assertFalse("Expecting the plugins[0] to not be executed", plugins[0].beenExecuted);
		Assert.assertTrue("Expecting the plugins[1] to be executed", plugins[1].beenExecuted);
		Assert.assertTrue("Expecting the plugins[2] to be executed", plugins[2].beenExecuted);
		Assert.assertFalse("Expecting the plugins[3] to not be executed", plugins[3].beenExecuted);
		Assert.assertFalse("Expecting the plugins[4] to not be executed", plugins[4].beenExecuted);

		reset(plugins);
		list.execute("test.location");

		Assert.assertTrue("Expecting the plugins[0] to be executed", plugins[0].beenExecuted);
		Assert.assertFalse("Expecting the plugins[1] to not be executed", plugins[1].beenExecuted);
		Assert.assertTrue("Expecting the plugins[2] to be executed", plugins[2].beenExecuted);
		Assert.assertTrue("Expecting the plugins[3] to be executed", plugins[3].beenExecuted);
		Assert.assertFalse("Expecting the plugins[4] to not be executed", plugins[4].beenExecuted);

		reset(plugins);
		list.execute("test.location.sub");

		Assert.assertFalse("Expecting the plugins[0] to not be executed", plugins[0].beenExecuted);
		Assert.assertFalse("Expecting the plugins[1] to not be executed", plugins[1].beenExecuted);
		Assert.assertTrue("Expecting the plugins[2] to be executed", plugins[2].beenExecuted);
		Assert.assertTrue("Expecting the plugins[3] to be executed", plugins[3].beenExecuted);
		Assert.assertTrue("Expecting the plugins[4] to be executed", plugins[4].beenExecuted);

		list = new GenericPluginList("test.location.sub");
		Assert.assertEquals("Exepcting 3 plugins in list", 3, list.size());

		reset(plugins);
		list.execute("test.location.sub");

		Assert.assertFalse("Expecting the plugins[0] to not be executed", plugins[0].beenExecuted);
		Assert.assertFalse("Expecting the plugins[1] to not be executed", plugins[1].beenExecuted);
		Assert.assertTrue("Expecting the plugins[2] to be executed", plugins[2].beenExecuted);
		Assert.assertTrue("Expecting the plugins[3] to be executed", plugins[3].beenExecuted);
		Assert.assertTrue("Expecting the plugins[4] to be executed", plugins[4].beenExecuted);

		reset(plugins);
		list.execute("test.location.sub.dummy");

		Assert.assertFalse("Expecting the plugins[0] to not be executed", plugins[0].beenExecuted);
		Assert.assertFalse("Expecting the plugins[1] to not be executed", plugins[1].beenExecuted);
		Assert.assertTrue("Expecting the plugins[2] to be executed", plugins[2].beenExecuted);
		Assert.assertTrue("Expecting the plugins[3] to be executed", plugins[3].beenExecuted);
		Assert.assertFalse("Expecting the plugins[4] to not be executed", plugins[4].beenExecuted);
	}
}
