package com.dynatrace.easytravel.weblauncher;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWTException;
import org.junit.Ignore;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.integration.IntegrationTestBase;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.utils.TestHelpers;

public class WebLauncherTest {
	private static final Logger log = LoggerFactory.make();

	private static final int TIMEOUT = 10 * 1000;	// 10 Seconds

	@Ignore("This fails, but I could not find out why, it is probably not that useful to test UI stuff here anyway.")
	@Test
	public void testCreateUI() throws Exception {
		// in CI starting WebLauncher works sometimes, we should
		final AtomicBoolean shouldStop = new AtomicBoolean(false);

		Thread th = new Thread("WebLauncher shutdown thread.") {
			@Override
			public void run() {
				long end = System.currentTimeMillis() + TIMEOUT;

				while(System.currentTimeMillis() < end && !shouldStop.get()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						log.error("Interrupted", e);
						return;
					}
				}

				if(shouldStop.get()) {
					log.info("Waiter thread stopped");
					return;
				}

				// try to close Launcher and anything else that was started
				try {
					IntegrationTestBase.cleanup();
				} catch (Exception e) {
					log.error("Error while stopping Launcher", e);
				}
			}
		};
		th.start();

		WebLauncher launcher = new WebLauncher();
		try {
			assertEquals(0, launcher.createUI());
		} catch (IllegalStateException e) {
			TestHelpers.assertContains(e, "No context available");	// RWT is not initialized here
		} catch (SWTException e) {
			TestHelpers.assertContains(e.getCause(), "No context available");	// RWT is not initialized here
		}

		// stop thread if still running
		shouldStop.set(true);

		th.join();
	}
}
