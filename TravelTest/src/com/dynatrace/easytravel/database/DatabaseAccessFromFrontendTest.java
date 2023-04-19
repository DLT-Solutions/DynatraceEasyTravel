package com.dynatrace.easytravel.database;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.constants.BaseConstants.BusinessBackend;
import com.dynatrace.easytravel.constants.BaseConstants.SystemProperties;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.utils.ThreadTestHelper;

public class DatabaseAccessFromFrontendTest extends DatabaseWithContent {
	static final Logger log = LoggerFactory.make();

	private static final int NUMBER_OF_THREADS = 10;
	private static final int TEST_COUNT = 10;

    @Test
	public void testExecuteLight() throws Exception {
		log.info("Starting database access - light");
		QueryLocations access = new QueryLocations();
		access.setExtensionPoint(new String[] { PluginConstants.FRONTEND_JOURNEY_SEARCH });
		access.setEnabled(true);


		access.setMode("light");
		access.setLightQuery("select b from Location b where lower(b.name) like '%' || :name || '%' order by b.name desc");

		for(int i = 0;i < TEST_COUNT;i++) {
			@SuppressWarnings("unchecked")
			List<String> locations = (List<String>) access.execute(PluginConstants.FRONTEND_JOURNEY_SEARCH, "new", null, null);
			assertTrue(locations.toString(), locations.contains("New York"));
		}
	}

    @Test
	public void testExecuteHeavy() throws Exception {
		log.info("Starting database access - heavy");
		QueryLocations access = new QueryLocations();
		access.setExtensionPoint(new String[] { PluginConstants.FRONTEND_JOURNEY_SEARCH });
		access.setEnabled(true);

		access.setMode("heavy");

		executePlugin(access);
	}

    @Ignore("Does not run without Cassandra being up")
    @Test
	public void testExecuteHeavyCassandra() throws Exception {
    	System.setProperty(SystemProperties.PERSISTENCE_MODE, BusinessBackend.Persistence.CASSANDRA);
    	try {
			log.info("Starting database access - heavy - Cassandra");
			QueryLocations access = new QueryLocations();
			access.setExtensionPoint(new String[] { PluginConstants.FRONTEND_JOURNEY_SEARCH });
			access.setEnabled(true);

			access.setMode("heavy");

			executePlugin(access);
    	} finally {
    		System.clearProperty(SystemProperties.PERSISTENCE_MODE);
    	}
	}

    @Test
	public void testExecuteRandom() throws Exception {
		log.info("Starting database access - random");
		QueryLocations access = new QueryLocations();
		access.setExtensionPoint(new String[] { PluginConstants.FRONTEND_JOURNEY_SEARCH });
		access.setEnabled(true);

		access.setMode("random");

		for(int i = 0;i < TEST_COUNT;i ++) {
			executePlugin(access);
			System.out.print(".");
			if(i % 100 == 0) {
				System.out.println(i);
			}
		}
	}

    @Ignore("Takes too long to execute...")
    @Test
    public void testExecuteRandomMultipleThreads() throws Throwable {
		log.info("Starting database access - random - threaded");
		final QueryLocations access = new QueryLocations();
		access.setExtensionPoint(new String[] { PluginConstants.FRONTEND_JOURNEY_SEARCH });
		access.setEnabled(true);

		access.setMode("random");

        ThreadTestHelper helper =
            new ThreadTestHelper(NUMBER_OF_THREADS, TEST_COUNT);

        helper.executeTest(new ThreadTestHelper.TestRunnable() {
            @Override
            public void doEnd(int threadnum) throws Exception {
                // do stuff at the end ...
            }

            @Override
            public void run(int threadnum, int iter) throws Exception {
            	executePlugin(access);
            }
        });
    }

	private void executePlugin(QueryLocations access) {
		@SuppressWarnings("unchecked")
		List<String> locations = (List<String>) access.execute(PluginConstants.FRONTEND_JOURNEY_SEARCH, "New", null, null);
		assertTrue(locations.toString(), locations.contains("New York"));
	}

    @Test
	public void testOtherPluginLocation() {
		// nothing happens on other plugin points
		QueryLocations access = new QueryLocations();
		access.setExtensionPoint(new String[] { PluginConstants.FRONTEND_JOURNEY_SEARCH });
		access.setEnabled(true);
		access.execute(PluginConstants.BACKEND_JOURNEY_ADD);
	}

    @Test
	public void testCleanupAfterRandom() {
    	log.info("Starting database access - random");
    	QueryLocations access = new QueryLocations();
    	access.setExtensionPoint(new String[] { PluginConstants.FRONTEND_JOURNEY_SEARCH });
    	access.setEnabled(true);

    	access.setMode("random");

    	for(int i = 0;i < TEST_COUNT;i ++) {
    		executePlugin(access);
    		System.out.print(".");
    		if(i % 100 == 0) {
    			System.out.println(i);
    		}
    	}

    	// cleanup when we disable the plugin
		access.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE);
	}

    @Test
	public void testCleanup() {
    	// cleanup when we disable the plugin
		QueryLocations access = new QueryLocations();
		access.doExecute(PluginConstants.LIFECYCLE_PLUGIN_DISABLE);
	}

}
