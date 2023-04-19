package com.dynatrace.easytravel.database;

import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.spring.SpringUtils;

public class DatabaseAccessExceedPoolTest extends DatabaseBase {
	static final Logger log = LoggerFactory.make();

    @Test
	public void testExecuteThreaded() throws Exception {
		DataAccess access = createNewAccess();

		try {
			DatabaseAccessExceedPool pool = new DatabaseAccessExceedPool();
			pool.setExtensionPoint(new String[] { PluginConstants.BACKEND_JOURNEY_SEARCH });
			pool.setEnabled(true);
			pool.setCount(5);
			pool.setMode("threaded");

			log.info("Starting database access in 5 threads concurrently.");
			pool.execute(PluginConstants.BACKEND_JOURNEY_SEARCH, database);
		} finally {
			log.info("Done, closing down.");
			access.close();
		}
	}

    @Test
	public void testExecuteNonThreaded() throws Exception {
		DataAccess access = createNewAccess();

		try {
			DatabaseAccessExceedPool pool = new DatabaseAccessExceedPool();
			pool.setExtensionPoint(new String[] { PluginConstants.BACKEND_JOURNEY_SEARCH });
			pool.setEnabled(true);
			pool.setCount(10);
			pool.setMode("nonthreaded");

			log.info("Starting database access in one thread with using 10 entity managers.");
			pool.execute(PluginConstants.BACKEND_JOURNEY_SEARCH, database);
		} finally {
			log.info("Done, closing down.");
			access.close();
		}
	}

	@Test
	public void testWithSpringFactory() {
		System.setProperty("com.dynatrace.easytravel.propertiesfile", Thread.currentThread().getContextClassLoader().getResource(EasyTravelConfig.PROPERTIES_FILE + ".properties").toString());
		System.setProperty(BaseConstants.SystemProperties.PERSISTENCE_MODE, BaseConstants.BusinessBackend.Persistence.JPA);
		SpringUtils.initBusinessBackendContext();
		try {
			DatabaseAccessExceedPool pool = new DatabaseAccessExceedPool();
			pool.setExtensionPoint(new String[] { PluginConstants.BACKEND_JOURNEY_SEARCH });
			pool.setEnabled(true);
			pool.setCount(7);	// use the same as in the *.ctx.xml here...
			pool.setMode("nonthreaded");

			log.info("Starting database access in one thread with using 7 entity managers via spring factory.");
			pool.execute(PluginConstants.BACKEND_JOURNEY_SEARCH);
		} finally {
			log.info("Done, closing down.");
			SpringUtils.disposeBusinessBackendContext();
		}
	}
}
