package com.dynatrace.easytravel.launcher.plugin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.procedures.utils.CentralTechnologyActivator;
import com.dynatrace.easytravel.plugin.RemotePluginController;
import com.dynatrace.easytravel.utils.TestHelpers;

import ch.qos.logback.classic.Level;



/**
 *
 * @author stefan.moschinski
 */
@RunWith(MockitoJUnitRunner.class)
public class BasePluginManagerTest {
	@Mock
	private RemotePluginController controller;

	private final static long DEFAULT_TIMEOUT = TimeUnit.SECONDS.toMillis(5);

	@Test
	public void enablePluginsWorks() throws InterruptedException, IOException {
		// setup
		when(controller.requestAllPluginNames()).thenReturn(new String[0], new String[] { "plugin1", "plugin2", "plugin3" });

		BasePluginManager manager = new BasePluginManager("Base-Plugin-Manager-Thread", controller, DEFAULT_TIMEOUT);
		assertEquals(controller, manager.getPluginController());

		manager.addPluginsToEnable(Arrays.asList("plugin1", "plugin2", "plugin3"));
		manager.start();

		waitUntilFinished(manager);

		assertNull(manager.getException());

		verify(controller).sendPluginStateChanged("plugin1", true);
		verify(controller).sendPluginStateChanged("plugin2", true);
		verify(controller).sendPluginStateChanged("plugin3", true);
	}

	@Test
	public void enablePluginsTechtypeDisabled() throws InterruptedException, IOException {
		CentralTechnologyActivator activator = CentralTechnologyActivator.getIntance();
		activator.notifyUserChangedState(Technology.DOTNET_20, false);

		assertFalse(activator.isAllowed(Technology.DOTNET_20));
		assertTrue(activator.getActivator(Technology.DOTNET_20).getDefaultPlugins().
				contains(Constants.Plugin.DotNetPaymentService));

		// setup
		when(controller.requestAllPluginNames()).thenReturn(new String[0], new String[] { Constants.Plugin.DotNetPaymentService });

		BasePluginManager manager = new BasePluginManager("Base-Plugin-Manager-Thread", controller, DEFAULT_TIMEOUT);

		manager.addPluginsToEnable(Arrays.asList(Constants.Plugin.DotNetPaymentService));
		manager.start();

		waitUntilFinished(manager);

		assertNull(manager.getException());
	}

	@Test
	public void enablePluginsWorksAlthoughIOExceptionIsInitiallyThrown() throws InterruptedException, IOException {
		// setup
		when(controller.requestAllPluginNames())
				.thenThrow(new IOException())
				.thenThrow(new IOException())
				.thenThrow(new IOException())
				.thenReturn(new String[] { "plugin1", "plugin3", "plugin2" });

		BasePluginManager manager = new BasePluginManager("Base-Plugin-Manager-Thread", controller, 10000*DEFAULT_TIMEOUT);
		manager.addPluginsToEnable(Arrays.asList("plugin1", "plugin2", "plugin3"));
		manager.start();

		waitUntilFinished(manager);

		assertNull("Should not have an exception as it worked eventually", manager.getException());

		verify(controller).sendPluginStateChanged("plugin1", true);
		verify(controller).sendPluginStateChanged("plugin2", true);
		verify(controller).sendPluginStateChanged("plugin3", true);
	}

	@Test
	public void enablePluginsWorksWhenPluginIsNotYetRegistered() throws Throwable {
		// setup
		when(controller.requestAllPluginNames()).thenReturn(new String[0], new String[] { "plugin1" },
				new String[] { "plugin1", "plugin3" });

		BasePluginManager manager = new BasePluginManager("Base-Plugin-Manager-Thread", controller, DEFAULT_TIMEOUT);
		manager.addPluginsToEnable(Arrays.asList("plugin1", "plugin2", "plugin3"));
		manager.start();

		waitUntilFinished(manager);

		verify(controller).registerPlugin("plugin2");
		verify(controller).sendPluginStateChanged("plugin2", true);
	}

	@Test
	public void enablePluginsWorksFailsWhenRegisteringPluginFails() throws Throwable {
		// setup
		doThrow(new IOException("testexception")).when(controller).registerPlugin("plugin2");
		when(controller.requestAllPluginNames()).thenReturn(new String[0], new String[] { "plugin1" },
				new String[] { "plugin1", "plugin3" });

		BasePluginManager manager = new BasePluginManager("Base-Plugin-Manager-Thread", controller, DEFAULT_TIMEOUT);
		manager.addPluginsToEnable(Arrays.asList("plugin1", "plugin2", "plugin3"));
		manager.start();

		waitUntilFinished(manager);

		verify(controller, atLeastOnce()).registerPlugin("plugin2");

		Throwable exception = manager.getException();
		assertNotNull(exception);
		assertEquals(IllegalStateException.class, exception.getClass());
		TestHelpers.assertContains(exception, "Could not enable: [plugin2]");
	}

	@Test
	public void enablePluginsWorksFailsWhenRequestingPluginNames() throws Throwable {
		// setup
		when(controller.requestAllPluginNames()).thenThrow(new IOException("Testexception"));

		BasePluginManager manager = new BasePluginManager("Base-Plugin-Manager-Thread", controller, DEFAULT_TIMEOUT);
		manager.addPluginsToEnable(Arrays.asList("plugin1", "plugin2", "plugin3"));
		manager.start();

		waitUntilFinished(manager);

		verify(controller, never()).registerPlugin("plugin2");

		Throwable exception = manager.getException();
		assertNotNull(exception);
		assertEquals(IllegalStateException.class, exception.getClass());
		TestHelpers.assertContains(exception, "Could not enable:", "plugin1", "plugin2", "plugin3");
	}

	@Test
	public void enablePluginsFailsIfNoConnectionAvailable() throws Throwable {
		// setup
		when(controller.requestAllPluginNames()).thenReturn(new String[0]);
		when(controller.requestAllPluginNames()).thenThrow(new IOException());

		BasePluginManager manager = new BasePluginManager("Base-Plugin-Manager-Thread", controller, DEFAULT_TIMEOUT);
		manager.addPluginsToEnable(Arrays.asList("plugin1", "plugin2", "plugin3"));
		manager.start();

		waitUntilFinished(manager);

		Throwable exception = manager.getException();
		assertNotNull(exception);
		assertEquals(IllegalStateException.class, exception.getClass());
		TestHelpers.assertContains(exception, "plugin1", "plugin2", "plugin3", "Could not enable:");
	}

	@Test
	public void disablePluginsWorksWhenAllPluginsDisabledConcurrently() throws InterruptedException, IOException {
		// setup
		when(controller.requestAllPluginNames()).thenReturn(new String[] { "plugin1", "plugin2", "plugin3" });
		BasePluginManager manager = new BasePluginManager("Base-Plugin-Manager-Thread", controller, DEFAULT_TIMEOUT);

		manager.addPluginsToDisable(Arrays.asList("plugin1", "plugin2", "plugin3"));
		manager.start();

		waitUntilFinished(manager);

		assertNull(manager.getException());
		verify(controller).sendPluginStateChanged("plugin1", false);
		verify(controller).sendPluginStateChanged("plugin2", false);
		verify(controller).sendPluginStateChanged("plugin3", false);
	}

	@Test
	public void disablePluginsWorksAlthoughIOExceptionIsInitiallyThrown() throws InterruptedException,
			IOException {
		// setup
		when(controller.requestAllPluginNames())
				.thenThrow(new IOException())
				.thenThrow(new IOException())
				.thenThrow(new IOException())
				.thenReturn(new String[] { "plugin1", "plugin3", "plugin2" });

		BasePluginManager manager = new BasePluginManager("Base-Plugin-Manager-Thread", controller, DEFAULT_TIMEOUT);

		manager.addPluginsToDisable(Arrays.asList("plugin1", "plugin2", "plugin3"));
		manager.start();

		waitUntilFinished(manager);

		assertNull(manager.getException());

		verify(controller).sendPluginStateChanged("plugin1", false);
		verify(controller).sendPluginStateChanged("plugin2", false);
		verify(controller).sendPluginStateChanged("plugin3", false);
	}

	@Test
	public void disablePluginsFailsWhenPluginCannotBeDisabled() throws Throwable {
		// setup
		when(controller.requestAllPluginNames()).thenReturn(new String[0], new String[] { "plugin1" },
				new String[] { "plugin1", "plugin3" });

		BasePluginManager manager = new BasePluginManager("Base-Plugin-Manager-Thread", controller, DEFAULT_TIMEOUT);
		manager.addPluginsToDisable(Arrays.asList("plugin1", "plugin2", "plugin3"));
		manager.start();

		waitUntilFinished(manager);

		verify(controller).sendPluginStateChanged("plugin1", false);
		verify(controller, never()).sendPluginStateChanged("plugin2", false);
		verify(controller).sendPluginStateChanged("plugin3", false);

		Throwable exception = manager.getException();
		assertNotNull(exception);
		assertEquals(IllegalStateException.class, exception.getClass());
		TestHelpers.assertContains(exception, "Could not disable: [plugin2]");
	}

	@Test
	public void disablePluginsFailsIfNoConnectionAvailable() throws Throwable {
		// setup
		when(controller.requestAllPluginNames()).thenThrow(new IOException());

		BasePluginManager manager = new BasePluginManager("Base-Plugin-Manager-Thread", controller, DEFAULT_TIMEOUT);
		manager.addPluginsToDisable(Arrays.asList("plugin1", "plugin2", "plugin3"));
		manager.start();

		waitUntilFinished(manager);

		verify(controller, never()).sendPluginStateChanged("plugin1", false);
		verify(controller, never()).sendPluginStateChanged("plugin2", false);
		verify(controller, never()).sendPluginStateChanged("plugin3", false);

		Throwable exception = manager.getException();
		assertNotNull(exception);
		assertEquals(IllegalStateException.class, exception.getClass());
		TestHelpers.assertContains(exception, "plugin1", "plugin2", "plugin3", "Could not disable:");
	}

	@Test
	public void enableAndDisablePluginsWorks() throws Throwable {
		// setup
		when(controller.requestAllPluginNames()).thenReturn(new String[] { "disable1", "disable2", "enable1", "enable2" });

		BasePluginManager manager = new BasePluginManager("Base-Plugin-Manager-Thread", controller, DEFAULT_TIMEOUT);
		manager.addPluginsToDisable(Arrays.asList("disable1", "disable2"));
		manager.addPluginsToEnable(Arrays.asList("enable1", "enable2"));
		manager.start();

		waitUntilFinished(manager);

		verify(controller).sendPluginStateChanged("disable1", false);
		verify(controller).sendPluginStateChanged("disable2", false);
		verify(controller).sendPluginStateChanged("enable1", true);
		verify(controller).sendPluginStateChanged("enable2", true);

		assertNull(manager.getException());
	}

	@Test
	public void enableAndDisablePluginsFailsIfNoConnectionAvailable() throws Throwable {
		// setup
		when(controller.requestAllPluginNames()).thenThrow(new IOException());

		BasePluginManager manager = new BasePluginManager("Base-Plugin-Manager-Thread", controller, DEFAULT_TIMEOUT);
		manager.addPluginsToDisable(Arrays.asList("disable1", "disable2"));
		manager.addPluginsToEnable(Arrays.asList("enable1", "enable2"));
		manager.start();

		waitUntilFinished(manager);

		verify(controller, never()).sendPluginStateChanged("disable1", false);
		verify(controller, never()).sendPluginStateChanged("disable2", false);
		verify(controller, never()).sendPluginStateChanged("enable1", true);
		verify(controller, never()).sendPluginStateChanged("enable2", true);

		Throwable exception = manager.getException();
		assertNotNull(exception);
		assertEquals(IllegalStateException.class, exception.getClass());
		TestHelpers.assertContains(exception, "enable1", "enable2", "disable1", "disable2", "Could not enable:", "Could not disable:");
	}

	private void waitUntilFinished(BasePluginManager manager) throws InterruptedException {
		while (!manager.isFinished()) {
			Thread.sleep(10);
		}
	}

	@Test
	public void testConstruct() {
		assertNotNull(new BasePluginManager());
	}

	@Test
	public void testFailsToAddWhenStarted() throws Exception {
		// setup
		when(controller.requestAllPluginNames()).thenReturn(new String[0], new String[] { "plugin1", "plugin2", "plugin3" });

		BasePluginManager manager = new BasePluginManager("Base-Plugin-Manager-Thread", controller, DEFAULT_TIMEOUT);

		manager.addPluginsToEnable(Arrays.asList("plugin1", "plugin2", "plugin3"));
		manager.start();

		waitUntilFinished(manager);

		try {
			manager.addPluginsToDisable(null);
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "Unable to disable plugins");
		}
		try {
			manager.addPluginsToEnable(null);
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "Unable to enable plugins");
		}
	}

	@Test
	public void testFinishedWhenNoWork() throws Exception {
		BasePluginManager manager = new BasePluginManager("Base-Plugin-Manager-Thread", controller, DEFAULT_TIMEOUT);

		manager.start();

		waitUntilFinished(manager);
		assertTrue(manager.isFinished());
	}

	@Test
	public void testWithDifferentLogLevel() throws Exception {
		final AtomicReference<Exception> exception = new AtomicReference<Exception>(null);

		TestHelpers.runTestWithDifferentLogLevel(new Runnable() {

			@Override
			public void run() {
				try {
					enablePluginsWorks();
				} catch (Exception e) {
					exception.set(e);
				}
			}
		}, BasePluginManager.class.getName(), Level.DEBUG);

		if(exception.get() != null) {
			throw exception.get();
		}
	}
}
