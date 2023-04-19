package com.dynatrace.easytravel.spring;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.TestHelpers;

import ch.qos.logback.classic.Level;


public class PluginListTest extends SpringTestBase {
	@After
	public void tearDown() {
		PluginList.stopRefreshThread();

		// re-init Spring between each test to have pristine plugin lists again
		SpringUtils.disposeBusinessBackendContext();
		SpringUtils.initBusinessBackendContextForTest();
	}

	@Test
	public void testPluginList() {
		PluginList<GenericPlugin> plugins = new PluginList<GenericPlugin>(GenericPlugin.class);

		GenericPlugin plugin = registerPlugin(true, false);

		assertTrue("Expected to find the registered plugin in the list of available plugins, but did not",
				plugins.getAllPlugins().iterator().hasNext());

		assertTrue("Expected to find the registered plugin in the list of enabled plugins, but did not",
				plugins.getEnabledPlugins().iterator().hasNext());

		assertTrue("Expected to find the registered plugin in the iterator (i.e. enabled), but did not",
				plugins.iterator().hasNext());

		//assertFalse(plugins.interested(null));

		assertTrue(plugins.interested(plugin));

		{ // not interested in other plugin
			PluginList<MockPlugin> plugins2 = new PluginList<MockPlugin>(MockPlugin.class);
			assertFalse(plugins2.interested(plugin));
		}

		assertEquals(1, plugins.size());

		TestHelpers.ToStringTest(plugins);

		verify(plugin);
	}

	@Test
	public void testPluginListNotEnabled() {
		PluginList<GenericPlugin> plugins = new PluginList<GenericPlugin>(GenericPlugin.class);

		GenericPlugin plugin = registerPlugin(false, false);

		assertTrue("Expected to find the registered plugin, but did not",
				plugins.getAllPlugins().iterator().hasNext());

		assertFalse("Expected to not find the enabled plugin, but did find it",
				plugins.getEnabledPlugins().iterator().hasNext());

		assertFalse("Expected to not find the enabled plugin, but did find it",
				plugins.iterator().hasNext());

		//assertFalse(plugins.interested(null));

		assertTrue(plugins.interested(plugin));

		{ // not interested in other plugin
			PluginList<MockPlugin> plugins2 = new PluginList<MockPlugin>(MockPlugin.class);
			assertFalse(plugins2.interested(plugin));
		}

		assertEquals(1, plugins.size());

		TestHelpers.ToStringTest(plugins);

		verify(plugin);
	}

	@Test
	public void testPluginListNotEnabledThenEnabled() throws Exception {
		PluginList<GenericPlugin> plugins = new PluginList<GenericPlugin>(GenericPlugin.class);

		GenericPlugin plugin = registerPlugin(false, true);

		assertTrue("Expected to find the registered plugin, but did not",
				plugins.getAllPlugins().iterator().hasNext());

		assertFalse("Expected to not find the enabled plugin, but did find it",
				plugins.getEnabledPlugins().iterator().hasNext());

		assertFalse("Expected to not find the enabled plugin, but did find it",
				plugins.iterator().hasNext());

		//assertFalse(plugins.interested(null));

		assertTrue(plugins.interested(plugin));

		{ // not interested in other plugin
			PluginList<MockPlugin> plugins2 = new PluginList<MockPlugin>(MockPlugin.class);
			assertFalse(plugins2.interested(plugin));
		}

		assertEquals(1, plugins.size());

		TestHelpers.ToStringTest(plugins);

		SpringUtils.getPluginStateProxy().setPluginEnabled("PluginName", true);

		// stop the thread so it is started up again and refreshes the data upon next access
		PluginList.stopRefreshThread();

		// now the plugin should be enabled
		assertTrue("Expected to find the registered plugin, but did not",
				plugins.getEnabledPlugins().iterator().hasNext());

		assertTrue("Expected to find the registered plugin, but did not",
				plugins.iterator().hasNext());

		verify(plugin);
	}

	@Test
	public void testPluginListEnabledThenDisabled() throws Exception {
		PluginList<GenericPlugin> plugins = new PluginList<GenericPlugin>(GenericPlugin.class);

		GenericPlugin plugin = registerPlugin(true, false);

		assertTrue("Expected to find the registered plugin, but did not",
				plugins.getAllPlugins().iterator().hasNext());

		assertTrue("Expected to find the registered plugin, but did not",
				plugins.getEnabledPlugins().iterator().hasNext());

		assertTrue("Expected to find the registered plugin, but did not",
				plugins.iterator().hasNext());

		//assertFalse(plugins.interested(null));

		assertTrue(plugins.interested(plugin));

		{ // not interested in other plugin
			PluginList<MockPlugin> plugins2 = new PluginList<MockPlugin>(MockPlugin.class);
			assertFalse(plugins2.interested(plugin));
		}

		assertEquals(1, plugins.size());

		TestHelpers.ToStringTest(plugins);

		SpringUtils.getPluginStateProxy().setPluginEnabled("PluginName", false);

		// stop the thread so it is started up again and refreshes the data upon next access
		PluginList.stopRefreshThread();

		// now the plugin should be enabled
		assertFalse("Expected to not find the enabled plugin, but did find it",
				plugins.getEnabledPlugins().iterator().hasNext());

		assertFalse("Expected to not find the enabled plugin, but did find it",
				plugins.iterator().hasNext());

		verify(plugin);
	}
	@Test
	public void testPluginListErrorOnNull() {
		try {
			new PluginList<GenericPlugin>(null);
			fail("Expect an exception here");
		} catch (IllegalArgumentException e) {
			TestHelpers.assertContains(e, "must not be null");
		}
	}

	private GenericPlugin registerPlugin(boolean enabled, boolean willBeEnabled) {
		GenericPlugin plugin = createMock(GenericPlugin.class);

		// make the plugin available
		SpringUtils.getPluginStateProxy().registerPlugins(new String[] { "TestPlugin:Group:" + InstallationType.Both.name() });
		SpringUtils.getPluginStateProxy().setPluginEnabled("PluginName", enabled);

		expect(plugin.isActivatable()).andReturn(true);
		expect(plugin.getName()).andReturn("PluginName").anyTimes();
		expect(plugin.getGroupName()).andReturn("TestGroup").anyTimes();
		expect(plugin.getDescription()).andReturn("").anyTimes();
        expect(plugin.getCompatibility()).andReturn(InstallationType.Both.name()).anyTimes();
		expect(plugin.getExtensionPoint()).andReturn(new String[]{PluginConstants.FRONTEND_PAGE}).anyTimes();
		expect(plugin.isEnabled()).andReturn(enabled).anyTimes();
		if(!enabled || !willBeEnabled) {
			plugin.setEnabled(false);
			expectLastCall().anyTimes();
			expect(plugin.execute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE)).andReturn(null).anyTimes();
		}
		if(willBeEnabled) {
			plugin.setEnabled(true);
			expectLastCall().anyTimes();
			expect(plugin.execute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE)).andReturn(null).anyTimes();
		}

		replay(plugin);

		// make the plugin available
		SpringUtils.getPluginHolder().addPlugin(plugin);
		SpringUtils.getPluginStateProxy().setPluginEnabled("PluginName", enabled);

		return plugin;
	}

	// just a dummy class to test interested()
	public interface MockPlugin extends Plugin {
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(PluginConstants.class);
	}

	@Test
	public void testWithDifferentLogLevel() {
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				testPluginList();

			}
		}, PluginLifeCycle.class.getName(), Level.DEBUG);
	}
}
