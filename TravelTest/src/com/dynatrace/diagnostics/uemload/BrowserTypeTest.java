package com.dynatrace.diagnostics.uemload;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class BrowserTypeTest {

	@Test
	public void testEqualsBrowserType() {
		BrowserType browser1 = BrowserType.CHROME_56;
		BrowserType browser2 = BrowserType.CHROME_56;

		assertTrue(browser1.equals(browser2));
		browser2 = BrowserType.CHROME_57;
		assertFalse(browser1.equals(browser2));

		browser1 = BrowserType.FF_530;
		browser2 = BrowserType.FF_530;
		assertTrue(browser1.equals(browser2));

		Object object = new Object();
		assertFalse(browser1.equals(object));
		assertFalse(browser1.equals(null));
	}
}
