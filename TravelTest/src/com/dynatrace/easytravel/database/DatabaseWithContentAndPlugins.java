package com.dynatrace.easytravel.database;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.dynatrace.easytravel.DummyNativeApplication;
import com.dynatrace.easytravel.DummyPaymentService;
import com.dynatrace.easytravel.plugin.PluginTestBase;
import com.dynatrace.easytravel.spring.Plugin;
import com.dynatrace.easytravel.spring.SpringUtils;

/**
 * Base test class for tests that require database and
 * useful content in the database as well as a basic spring-related
 * setup for plugins to be in place.
 *
 * This class will ensure that a Database is available, has at least
 * the default content and will also start the Spring support for
 * plugins via "webapp/WEB-INF/spring/unit-test-root-context.xml"
 *
 * @author dominik.stadler
 */
public class DatabaseWithContentAndPlugins extends DatabaseWithContent {
	@BeforeClass
	public static void setUpClass() throws IOException {
		// have to start plugins as well here, but cannot have two "extends" for this class
		PluginTestBase.setUpPluginTestBase();

		// start database if necessary
		DatabaseWithContent.setUpClass();

        //System.setProperty("com.dynatrace.easytravel.propertiesfile", Thread.currentThread().getContextClassLoader().getResource(EasyTravelConfig.PROPERTIES_FILE + ".properties").toString());
        SpringUtils.initBusinessBackendContextForTest();
        Plugin dummyNativeApplication = new DummyNativeApplication();
        SpringUtils.getPluginHolder().addPlugin(dummyNativeApplication);
        SpringUtils.getPluginStateProxy().setPluginEnabled(dummyNativeApplication.getName(), true);
        Plugin dummyPaymentService = new DummyPaymentService();
        SpringUtils.getPluginHolder().addPlugin(dummyPaymentService);
        SpringUtils.getPluginStateProxy().setPluginEnabled(dummyPaymentService.getName(), true);
    }

	@AfterClass
	public static void tearDownClass() throws IOException {
        SpringUtils.disposeBusinessBackendContext();

        DatabaseBase.tearDownClass();

        PluginTestBase.tearDownPluginTestBase();
    }
}
