package com.dynatrace.easytravel.frontend.beans;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.JavascriptBootstrapAgent;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.spring.PluginList;
import com.dynatrace.easytravel.spring.PluginRefreshThread;
import com.dynatrace.easytravel.spring.SpringTestBase;
import com.dynatrace.easytravel.utils.TestHelpers;


/**
 * @author cwat-graudner
 *
 */
public class JavascriptBeanTest extends SpringTestBase {
	private static final int PLUGIN_REFRESH_INTERVAL = 500;

	private static final String PLUGIN_NAME = Constants.Plugin.JavascriptBootstrapAgent;
	private static final String PLUGIN_DELIVERABLE = "<script type=\"text/javascript\" src=\"/dtagent_bootstrap.js?app=easyTravel+portal\"></script>";

	@Before
	public void setup() {
		PluginRefreshThread.setRefreshInterval(PLUGIN_REFRESH_INTERVAL);
		JavascriptBean bean = new JavascriptBean();
		bean.getPluginList().setRefreshAllPlugins(true);
		bean.getBootstrapPluginList().setRefreshAllPlugins(true);
		cleanup();
	}

	@After
	public void tearDown() {
		PluginList.stopRefreshThread();
	}

	@Test
	public void testGetPath() {
		assertEquals("", new JavascriptBean().getBootstrapAgent());
		assertEquals("", new JavascriptBean().getJavascript());
	}

	@Test
	public void testValidResponses() throws InterruptedException {
		TestHelpers.initPlugin(new JavascriptBootstrapAgent(), PLUGIN_NAME, new String[] {
				PluginConstants.FRONTEND_JAVASCRIPT_BOOTSTRAP + ".*"// plugin extension point(s)
		});

		TestHelpers.enablePlugin(PLUGIN_NAME, false);
		assertFalse("plugin shouldn't be enabled", TestHelpers.isPluginEnabled(PLUGIN_NAME));
		JavascriptBean bean = new JavascriptBean();
		assertEquals(bean.getBootstrapAgent(), "");

		TestHelpers.enablePlugin(PLUGIN_NAME, true);

		Thread.sleep(PLUGIN_REFRESH_INTERVAL*2);

		assertTrue("plugin should be enabled", TestHelpers.isPluginEnabled(PLUGIN_NAME));
		assertEquals(PLUGIN_DELIVERABLE, bean.getBootstrapAgent().trim());
	}

	private void cleanup() {
		TestHelpers.enablePlugin(PLUGIN_NAME, false);
	}

}
