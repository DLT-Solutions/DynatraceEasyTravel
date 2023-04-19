package com.dynatrace.easytravel;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.PluginConstants;

public class SlowUserLoginTest {
	private static final Logger log = LoggerFactory.make();

    private static final String SOMETHING_ELSE = "something.else";

	@Test
	public void testExecute() {
		UserLoginUtils login = new UserLoginUtils();
		login.setExtensionPoint(new String[] { PluginConstants.FRONTEND_LOGIN });
		login.setEnabled(true);

		log.info("Quick");
		long start = System.currentTimeMillis();
		login.execute(SOMETHING_ELSE);
		assertTrue("Should only take a few msec, but did take: " + (System.currentTimeMillis() - start),
				(System.currentTimeMillis() - start) < 100);

		log.info("Slow");
		login.execute(PluginConstants.FRONTEND_LOGIN);
		assertTrue("Should take at least 10000ms, but did take: " + (System.currentTimeMillis() - start) + "ms",
				(System.currentTimeMillis() - start) >= 9980);

		log.info("Done");
	}
}
