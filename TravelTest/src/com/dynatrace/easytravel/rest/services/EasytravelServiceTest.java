/**
 *
 */
package com.dynatrace.easytravel.rest.services;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author tomasz.wieremjewicz
 * @date 9 maj 2019
 *
 */
public class EasytravelServiceTest {
	private static final String PLUGIN_DATA = "NodeJSWeatherApplication:%s:Both:This plugin includes the current weather of the destination into the results page and the weather forecast for the trip duration into the trip details page.";

	@Test
	public void testIfPluginIsAngular() {
		String simpleAngularPlugin = String.format(PLUGIN_DATA, "Angular");
		String notAngularPlugin = String.format(PLUGIN_DATA, "UI related plugin");
		String dualPlugin = String.format(PLUGIN_DATA, "UI related plugin, Angular");

		EasytravelService service = new EasytravelService();

		Assert.assertFalse("Null is not an angular plugin", service.isAngularPlugin(null));
		Assert.assertFalse("An empty string is not an angular plugin", service.isAngularPlugin(""));
		Assert.assertFalse("UI related plugin is not an angular plugin", service.isAngularPlugin(notAngularPlugin));

		Assert.assertTrue("An angular plugin should be detected as angular plugin", service.isAngularPlugin(simpleAngularPlugin));
		Assert.assertTrue("A dual plugin should be detected as angular plugin", service.isAngularPlugin(dualPlugin));
	}
}
