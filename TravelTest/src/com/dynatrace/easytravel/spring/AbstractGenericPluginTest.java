package com.dynatrace.easytravel.spring;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.utils.TestHelpers;

import ch.qos.logback.classic.Level;

public class AbstractGenericPluginTest {

	@Test
	public void testDoExecute() {
		final AtomicBoolean called = new AtomicBoolean();

		GenericPlugin plugin = new AbstractGenericPlugin() {

			@Override
			protected Object doExecute(String location, Object... context) {
				called.set(true);
				return null;
			}

			@Override
			public String[] getExtensionPoint() {
				return new String[] {"mylocation", PluginConstants.LIFECYCLE_BACKEND_START};
			}


		};

		plugin.setEnabled(false);
		plugin.execute("mylocation");
		assertFalse("not enabled yet so should not be called...", called.get());

		plugin.execute(PluginConstants.LIFECYCLE_BACKEND_START);
		assertTrue("always called for the lifecycle events...", called.get());

		called.set(false);
		plugin.setEnabled(true);
		plugin.execute("otherlocation");
		assertFalse("Not called for a different location", called.get());

		called.set(false);
		plugin.setEnabled(true);
		plugin.execute("mylocation");
		assertTrue("Now it should be called", called.get());

		called.set(false);
		EasyTravelConfig.read().officialHost = "myhost";
		plugin.setHosts(new String[] { "host1" });
		plugin.execute("mylocation");
		assertFalse("Not called for a different host", called.get());
	}

	@Test
	public void testExecuteNullExtensionPoint() {
		GenericPluginList list = new GenericPluginList("somepoint");

		try {
			list.interested(new AbstractGenericPlugin() {
				@Override
				protected Object doExecute(String location, Object... context) {
					throw new UnsupportedOperationException();
				}

				@Override
				public String[] getExtensionPoint() {
					return null;
				}
			});
		} catch (IllegalStateException e) {
			// expected here
		}
	}


	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		com.dynatrace.easytravel.utils.PrivateConstructorCoverage.executePrivateConstructor(PluginLifeCycle.class);
	}


	@Test
	public void testWithDifferentLogLevel() {
		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				testDoExecute();

			}
		}, PluginLifeCycle.class.getName(), Level.DEBUG);
	}
}
