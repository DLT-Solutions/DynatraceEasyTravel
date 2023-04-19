package com.dynatrace.easytravel.launcher.plugin;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;


public class PluginStateRefresherTest {
	@Before
	public void setUp() {
		System.setProperty(PluginStateRefresher.PROPERTY_DELAY, "100");
		System.setProperty(PluginStateRefresher.PROPERTY_PERIOD, "100");
	}

	@Test
	public void test() throws InterruptedException {
		PluginStateRefresher refresh = new PluginStateRefresher();

		refresh.notifyUIChanged(null);
		assertNotNull(refresh.getPluginStateListener());


		// stop before start works
		refresh.stopTimer();

		// now start the timer
		refresh.startTimer();

		refresh.notifyUIChanged(null);
		assertNotNull(refresh.getPluginStateListener());

		// wait some time to let the timer trigger
		Thread.sleep(1000);

		// stop it again
		refresh.stopTimer();

		// a second time does not break
		refresh.stopTimer();

		refresh.notifyUIChanged(null);
		assertNotNull(refresh.getPluginStateListener());
	}
}
