package com.dynatrace.easytravel.spring;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.dynatrace.easytravel.plugin.PluginTestBase;


public class SpringTestBase extends PluginTestBase {

	@BeforeClass
	public static void init() {
		SpringUtils.initBusinessBackendContextForTest();
	}

	@AfterClass
	public static void dispose() {
		// stop the refresh thread
		PluginList.stopRefreshThread();

		SpringUtils.disposeBusinessBackendContext();
	}
}
