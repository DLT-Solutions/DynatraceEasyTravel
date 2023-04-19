package com.dynatrace.easytravel.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.PHPEnablement;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.spring.*;
import com.dynatrace.easytravel.utils.PrivateConstructorCoverage;
import com.dynatrace.easytravel.utils.TestHelpers;


/**
 * @author rafal.psciuk
 *
 */
public class PHPEnablementCheckTest extends SpringTestBase {

	private static final int PLUGIN_REFRESH_INTERVAL = 500;

	@Before
	public void setup() {
		PluginRefreshThread.setRefreshInterval(PLUGIN_REFRESH_INTERVAL);
		PHPEnablementCheck.getPluginList().setRefreshAllPlugins(true);
		PHPEnablement plugin = new PHPEnablement();
		TestHelpers.initPlugin(plugin, BaseConstants.Plugins.PHP_ENABLEMENT_PLUGIN, new String[] {"somepoint"});
		SpringUtils.getPluginHolder().addPlugin(plugin);
	}
	
	@After
	public void tearDown() {
		PluginList.stopRefreshThread();
	}
	
	@Test
	public void testIsPHPEnabled() throws IOException, InterruptedException {
		TestHelpers.enablePlugin(BaseConstants.Plugins.PHP_ENABLEMENT_PLUGIN, true);
		assertTrue("php shuld be enabled", PHPEnablementCheck.isPHPEnabled());
		TestHelpers.enablePlugin(BaseConstants.Plugins.PHP_ENABLEMENT_PLUGIN, false);
		Thread.sleep(PLUGIN_REFRESH_INTERVAL*2);
		assertFalse("php should be disabled", PHPEnablementCheck.isPHPEnabled());
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(PHPEnablementCheck.class);
	}
}
