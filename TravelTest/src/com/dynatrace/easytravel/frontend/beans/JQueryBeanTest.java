package com.dynatrace.easytravel.frontend.beans;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.jquery.JQueryEffectsStandard;
import com.dynatrace.easytravel.jquery.JQueryEffectsCloudflare;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.spring.PluginList;
import com.dynatrace.easytravel.spring.PluginRefreshThread;
import com.dynatrace.easytravel.spring.SpringTestBase;
import com.dynatrace.easytravel.utils.TestHelpers;


/**
 * @author rafal.psciuk
 *
 */
public class JQueryBeanTest extends SpringTestBase {
	private static final int PLUGIN_REFRESH_INTERVAL = 500;
	
	@Before
	public void setup() {
		PluginRefreshThread.setRefreshInterval(PLUGIN_REFRESH_INTERVAL);
		new JQueryBean().getPluginList().setRefreshAllPlugins(true);
		cleanup();
	}
	
	@After
	public void tearDown() {
		PluginList.stopRefreshThread();
	}
	
	@Test
	public void testGetPath() {
		JQueryBean bean = new JQueryBean();
		assertNotNull(bean.getPath());
	}
	
	@Test 
	public void testGetPathEffectsStandard() throws InterruptedException {
		
		final String PLUGIN_NAME = Constants.Plugin.JQueryEffectsStandard; // plugin name as given in .ctx.xml
		final String PLUGIN_DELIVERABLE_1 = "/jquery-ui/development-bundle/jquery-1.6.1.js";
		final String PLUGIN_DELIVERABLE_2 = "/jquery-ui/development-bundle/ui/jquery-ui-1.8.14.custom.js";
		
		TestHelpers.initPlugin(new JQueryEffectsStandard(), PLUGIN_NAME, new String[] {
			PluginConstants.FRONTEND_JQUERY_PATHS + ".*" , PluginConstants.FRONTEND_JQUERY_PATHS // plugin extension point(s)
		});

		TestHelpers.enablePlugin(PLUGIN_NAME, false);
		assertFalse("plugin is not enabled", TestHelpers.isPluginEnabled(PLUGIN_NAME));		
		JQueryBean bean = new JQueryBean();
		assertFalse(bean.getPath().contains(PLUGIN_DELIVERABLE_1));
		assertFalse(bean.getPath().contains(PLUGIN_DELIVERABLE_2));

		TestHelpers.enablePlugin(PLUGIN_NAME, true);

		Thread.sleep(PLUGIN_REFRESH_INTERVAL*2);
	
		assertTrue("plugin is not enabled", TestHelpers.isPluginEnabled(PLUGIN_NAME));
		assertTrue(bean.getPath().contains(PLUGIN_DELIVERABLE_1));
		assertTrue(bean.getPath().contains(PLUGIN_DELIVERABLE_2));
	}
	
	@Test 
	public void testGetPathEffectsCloudflare() throws InterruptedException {
		
		final String PLUGIN_NAME = Constants.Plugin.JQueryEffectsCloudflare; // plugin name as given in .ctx.xml
		final String PLUGIN_DELIVERABLE_1 = "//cdnjs.cloudflare.com/ajax/libs/jquery/1.8.1/jquery.js";
		final String PLUGIN_DELIVERABLE_2 = "//cdnjs.cloudflare.com/ajax/libs/jquery/1.8.1/jquery.min.js";

		
		TestHelpers.initPlugin(new JQueryEffectsCloudflare(), PLUGIN_NAME, new String[] {
			PluginConstants.FRONTEND_JQUERY_PATHS + ".*" , PluginConstants.FRONTEND_JQUERY_PATHS // plugin extension point(s)
		});

		TestHelpers.enablePlugin(PLUGIN_NAME, false);
		assertFalse("plugin is not enabled", TestHelpers.isPluginEnabled(PLUGIN_NAME));		
		JQueryBean bean = new JQueryBean();
		assertFalse(bean.getPath().contains(PLUGIN_DELIVERABLE_1));
		assertFalse(bean.getPath().contains(PLUGIN_DELIVERABLE_2));

		TestHelpers.enablePlugin(PLUGIN_NAME, true);

		Thread.sleep(PLUGIN_REFRESH_INTERVAL*2);
	
		assertTrue("plugin is not enabled", TestHelpers.isPluginEnabled(PLUGIN_NAME));
		assertTrue(bean.getPath().contains(PLUGIN_DELIVERABLE_1));
		assertTrue(bean.getPath().contains(PLUGIN_DELIVERABLE_2));
	}

	// This cleanup is necessary:
	// The alternative solution would be to always disable each plugin in each test method - after each test has been run.
	// However, if a particular test method fails, the plugin used by that method will not be disabled.
	// If we then run the whole JUnit class again, other test methods will fail because the last plugin
	// (the one that failed the test) is still active.
	
	private void cleanup () {
		final String PLUGIN_NAME_1 = Constants.Plugin.JQueryEffectsCloudflare;
		final String PLUGIN_NAME_2 = Constants.Plugin.JQueryEffectsStandard;
		TestHelpers.enablePlugin(PLUGIN_NAME_1, false);
		TestHelpers.enablePlugin(PLUGIN_NAME_2, false);
	}
	
}
