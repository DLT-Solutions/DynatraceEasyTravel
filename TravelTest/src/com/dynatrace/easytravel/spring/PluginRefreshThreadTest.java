package com.dynatrace.easytravel.spring;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.plugins.PluginServiceTestBase;
import com.dynatrace.easytravel.utils.TestHelpers;
import com.dynatrace.easytravel.utils.ThreadTestHelper;

import ch.qos.logback.classic.Level;


public class PluginRefreshThreadTest extends SpringTestBase {
	private static final PluginServiceTestBase PLUGIN_SERVICE = new PluginServiceTestBase();
	
	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();
	}
	
	@Before
	public void setUp() {
		// to speed up test
		PluginRefreshThread.setRefreshInterval(500);
	}

	@After
	public void tearDown() throws Exception {
		//System.out.println("TearDown - PluginRefreshThreadTest");

		// first ensure that we do not start the thread any more via timers
		GenericPluginList.cancelTimer();

		// then close any running thread to avoid dependencies between test-methods
		PluginList.stopRefreshThread();

		// now wait for the thread to finish
		ThreadTestHelper.waitForThreadToFinish("Plugin Refresh Thread");

		// dispose and re-init to remove plugin added above
		dispose();
		init();
	}

	@Test
	public void testRun() throws Exception {
		PluginRefreshThread thread = new PluginRefreshThread();
		thread.start();

		try {
			// there are 5 plugins in the default list of bootplugins
			assertEquals("Had: " + Arrays.toString(thread.getEnabledPlugins()),
					6, thread.getEnabledPlugins().length);

			// make the plugin available
			SpringUtils.getPluginStateProxy().registerPlugins(new String[] { "TestPlugin:Group" });
			SpringUtils.getPluginStateProxy().setPluginEnabled("TestPlugin", true);

			// still 5 as the thread did not refresh yet
			assertEquals("Had: " + Arrays.toString(thread.getEnabledPlugins()),
					6, thread.getEnabledPlugins().length);
		} finally {
			thread.shouldStop();

			thread.join();

			// thread should be dead now
			assertFalse(thread.isAlive());
		}
	}

	@Test
	public void testRunWithRefresh() throws Exception {
		PluginRefreshThread thread = new PluginRefreshThread();
		thread.start();

		try {
			// there are 5 plugins in the default list of bootplugins
			String[] enabledPlugins = thread.getEnabledPlugins();
			assertEquals("Had: " + Arrays.toString(thread.getEnabledPlugins()),
					6, enabledPlugins.length);


			// register a new plugin internally via Mocking
			GenericPlugin plugin = registerPlugin();

			// still 5 as the thread did not refresh yet
			assertEquals("Had: " + Arrays.toString(thread.getEnabledPlugins()),
					6, enabledPlugins.length);

			Thread.sleep(1000);

			// now 6 as the thread did refresh now
			enabledPlugins = thread.getEnabledPlugins();
			assertEquals("Had: " + Arrays.toString(enabledPlugins),
					7, enabledPlugins.length);

			verify(plugin);
		} finally {
			thread.shouldStop();

			thread.join();

			// thread should be dead now
			assertFalse(thread.isAlive());
		}
	}

	private GenericPlugin registerPlugin() {
		GenericPlugin plugin = createMock(GenericPlugin.class);

		// make the plugin available
		SpringUtils.getPluginStateProxy().registerPlugins(new String[] { "TestPlugin:Group:Both" });
		SpringUtils.getPluginStateProxy().setPluginEnabled("TestPlugin", true);

		expect(plugin.isActivatable()).andReturn(true).anyTimes();
		expect(plugin.getName()).andReturn("TestPlugin").anyTimes();
		expect(plugin.getGroupName()).andReturn("Group").anyTimes();
        expect(plugin.getCompatibility()).andReturn("Both").anyTimes();
		expect(plugin.getDescription()).andReturn("").anyTimes();
		expect(plugin.getExtensionPoint()).andReturn(new String[] { PluginConstants.FRONTEND_PAGE }).anyTimes();

		replay(plugin);

		// make the plugin available
		SpringUtils.getPluginHolder().addPlugin(plugin);
		SpringUtils.getPluginStateProxy().setPluginEnabled("TestPlugin", true);

		return plugin;
	}

	@Test
	public void testWithDifferentLogLevel() {
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				try {
					testRunWithRefresh();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}, PluginRefreshThread.class.getName(), Level.DEBUG);
	}

	@Test
	public void testRunWithOfficialHost() throws Exception {
		EasyTravelConfig.read().officialHost = "somehost";

		PluginRefreshThread thread = new PluginRefreshThread();
		thread.start();

		try {
			// there are 5 plugins in the default list of bootplugins
			String[] enabledPlugins = thread.getEnabledPlugins();
			assertEquals("Had: " + Arrays.toString(thread.getEnabledPlugins()),
					6, enabledPlugins.length);


			// register a new plugin internally via Mocking
			GenericPlugin plugin = registerPlugin();

			// still 5 as the thread did not refresh yet
			assertEquals("Had: " + Arrays.toString(thread.getEnabledPlugins()),
					6, enabledPlugins.length);

			Thread.sleep(1000);

			// now 6 as the thread did refresh now
			enabledPlugins = thread.getEnabledPlugins();
			assertEquals("Had: " + Arrays.toString(enabledPlugins),
					7, enabledPlugins.length);

			verify(plugin);
		} finally {
			thread.shouldStop();

			thread.join();

			// thread should be dead now
			assertFalse(thread.isAlive());

			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testRunWithPluginService() throws Exception {
		EasyTravelConfig config = EasyTravelConfig.read();
		config.pluginServiceHost = "localhost";

		PLUGIN_SERVICE.prepareEasyTravelConfig();

		PluginRefreshThread thread = new PluginRefreshThread();
		thread.start();


		try {
			// there are 0 plugins in the default service
			String[] enabledPlugins = thread.getEnabledPlugins();
			assertEquals("Had: " + Arrays.toString(thread.getEnabledPlugins()),
					0, enabledPlugins.length);

			RemotePluginService server = new RemotePluginService(config.pluginServiceHost, config.pluginServicePort);
			server.registerPlugins(new String[] {"TestPlugin1234"});
			server.setPluginEnabled("TestPlugin1234", true);
			assertEquals("{TestPlugin1234}", ArrayUtils.toString(server.getEnabledPluginNames()));

			// still 5 as the thread did not refresh yet
			assertEquals("Had: " + Arrays.toString(thread.getEnabledPlugins()),
					0, enabledPlugins.length);

			Thread.sleep(1000);

			// now 6 as the thread did refresh now
			enabledPlugins = thread.getEnabledPlugins();
			assertEquals("Had: " + Arrays.toString(enabledPlugins),
					1, enabledPlugins.length);
		} finally {
			thread.shouldStop();

			thread.join();

			// thread should be dead now
			assertFalse(thread.isAlive());

			PLUGIN_SERVICE.shutdown();

			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testInterruptShouldStop() throws Exception {
		final PluginRefreshThread thread = new PluginRefreshThread();
		thread.start();

		// need a bit more sleep-time here to make the shouldStop() wait longer here
		PluginRefreshThread.setRefreshInterval(1500);

		new Thread() {

			@Override
			public void run() {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					System.out.println("Interrupted:" + e);
				}

				thread.interrupt();
			}
		}.start();

		thread.shouldStop();

		thread.join();

		// thread should be dead now
		assertFalse(thread.isAlive());
	}

	@Test
	public void testRunWithException() throws Exception {
		EasyTravelConfig config = EasyTravelConfig.read();
		config.pluginServiceHost = "localhost";

		PLUGIN_SERVICE.prepareEasyTravelConfig();

		PluginRefreshThread thread = new PluginRefreshThread();

		// shutdown plugin service to trigger an exception in the run() execution of the thread
		PLUGIN_SERVICE.shutdown();

		thread.start();

		try {
			// just sleep a bit to let the refresh trigger and cause an exception
			Thread.sleep(700);
		} finally {
			thread.shouldStop();

			thread.join();

			// thread should be dead now
			assertFalse(thread.isAlive());

			EasyTravelConfig.resetSingleton();
		}
	}

	@Test
	public void testNoSpringContext() throws InterruptedException {
		PluginRefreshThread thread = new PluginRefreshThread();
		thread.start();

		try {
			// there are 5 plugins in the default list of bootplugins
			String[] enabledPlugins = thread.getEnabledPlugins();
			assertEquals("Had: " + Arrays.toString(thread.getEnabledPlugins()),
					6, enabledPlugins.length);


			// register a new plugin internally via Mocking
			GenericPlugin plugin = registerPlugin();

			// still 5 as the thread did not refresh yet
			assertEquals("Had: " + Arrays.toString(thread.getEnabledPlugins()),
					6, enabledPlugins.length);

			// now dispose to have state without Spring similar to how we run in Launcher, thread should not spam the log
			dispose();

			Thread.sleep(1000);

			try {
				// still 5 as the thread cannot call Spring proxy
				enabledPlugins = thread.getEnabledPlugins();
				assertEquals("Had: " + Arrays.toString(enabledPlugins),
						6, enabledPlugins.length);

				verify(plugin);
			} finally {
				// now init() again to allow tearDown() to work later on
				init();
			}
		} finally {
			thread.shouldStop();

			thread.join();

			// thread should be dead now
			assertFalse(thread.isAlive());
		}
	}
}
