package com.dynatrace.easytravel.amazon;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;


public class AmazonTrackingPluginTest {

	@Test
	public void test() {
		// simple test to cover the class
		assertNotNull(new AmazonTrackingPlugin().getFooterScript());
		assertNull(new AmazonTrackingPlugin().getFooter());
	}
}
