package com.dynatrace.easytravel.frontend.beans;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.spring.PluginList;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.spring.PluginRefreshThread;
import com.dynatrace.easytravel.spring.SpringTestBase;
import com.dynatrace.easytravel.utils.TestHelpers;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.webfonts.WebFontGoogle;

/**
 * @author wojtek.jarosz
 *
 */
public class WebFontsBeanTest extends SpringTestBase {
	
	private static final int PLUGIN_REFRESH_INTERVAL = 500;

	@Before
	public void setup() {
		PluginRefreshThread.setRefreshInterval(PLUGIN_REFRESH_INTERVAL);
		new WebFontsBean().getPluginList().setRefreshAllPlugins(true);
		cleanup();
	}
	
	@After
	public void tearDown() {
		PluginList.stopRefreshThread();
	}
	
	@Test
	public void testGetWebFonts() {
		WebFontsBean bean = new WebFontsBean();
		assertNotNull(bean.getWebFonts());
	}
	
	@Test 
	public void testWebFontGoogle() throws InterruptedException {

		final String PLUGIN_NAME = Constants.Plugin.WebFontGoogle; // plugin name as given in .ctx.xml
		final String PLUGIN_DELIVERABLE = "<script src=\"//ajax.googleapis.com/ajax/libs/webfont/1.4.7/webfont.js\"></script>" +
										"<script>WebFont.load({google: {families: ['Pacifico']}});</script>";
		TestHelpers.initPlugin(new WebFontGoogle(), PLUGIN_NAME, new String[] {
			PluginConstants.FRONTEND_WEBFONTS // plugin extension point
		});
		
		// disable plugin and test
		TestHelpers.enablePlugin(PLUGIN_NAME, false);
		assertFalse("plugin is enabled", TestHelpers.isPluginEnabled(PLUGIN_NAME));		
		WebFontsBean bean = new WebFontsBean();
		assertFalse(bean.getWebFonts().equals(PLUGIN_DELIVERABLE));

		// Enable plugin and test again
		TestHelpers.enablePlugin(PLUGIN_NAME, true);
		Thread.sleep(PLUGIN_REFRESH_INTERVAL*2);
		assertTrue("plugin is not enabled", TestHelpers.isPluginEnabled(PLUGIN_NAME));
		assertTrue(bean.getWebFonts().equals(PLUGIN_DELIVERABLE));
	}
	
	private void cleanup () {
		final String PLUGIN_NAME = Constants.Plugin.JQueryEffectsCloudflare;
		TestHelpers.enablePlugin(PLUGIN_NAME, false);
	}
	
}
