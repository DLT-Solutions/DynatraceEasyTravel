package com.dynatrace.easytravel.spring;

import java.util.Arrays;

import org.junit.Assert;

import org.junit.Test;


/**
 * Starts a frontend instance outside the Servlet environment.
 *
 * @author philipp.grasboeck
 */
public class StartFrontendTest extends SpringTestBase {

	@Test
	public void testStartFrontend() {
		String[] names = SpringUtils.getPluginStateProxy().getAllPluginNames();
		Assert.assertNotNull("Expecting some plugins", names);
		System.out.println("All plugins: " + Arrays.toString(names));

		names = SpringUtils.getPluginStateProxy().getEnabledPluginNames();
		Assert.assertNotNull("Expecting some enabled plugins", names);
		System.out.println("Enabled plugins: " + Arrays.toString(names));
	}
}
