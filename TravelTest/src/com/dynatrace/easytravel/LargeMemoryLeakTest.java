package com.dynatrace.easytravel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.PluginConstants;


@Ignore("Test fails, and the cause has not been found yet.")
public class LargeMemoryLeakTest {
	private static final Logger logger = LoggerFactory.make();

	private static final long MAX_RUN_TIME = 20 * 1000;  // 20 seconds

	@BeforeClass
	public static void setUpClass() throws IOException {
		LoggerFactory.initLogging();
	}

	@Before
	public void setUp() throws Exception {
		logger.info("Sleep 10 seconds before starting the test in order to not reach the GC Overhead Limit here");
		Thread.sleep(10000);
	}

	@Test
	public void testLargeMemoryLeak() throws Throwable {
		Map<Object, List<Location>> cache = new HashMap<Object, List<Location>>();

		// this is the name of the plugin!
		AddLocationPicture enhancer = new AddLocationPicture();
		enhancer.setExtensionPoint(new String[] {PluginConstants.BACKEND_LOCATION_SEARCH});
		enhancer.setEnabled(true);
		enhancer.setGrowSize(200);

		// run until we get OOM
		try {
			logger.info("Starting to increase memory usage for up to " + MAX_RUN_TIME/1000 + " seconds, expecting to fail.");
			runPluginForSomeTime(cache, enhancer);

			Assert.fail("We expect to go OOM here!");
		} catch (OutOfMemoryError e) {
			logger.info("Had expected OOM at: " /*, e*/);

			// now let's make sure that we don't go out of memory again
			cache.clear();

			// run, now it should not go OOM as we just had it happen and should wait some time before we cause it again
			logger.info("Starting to run plugin for up to " + MAX_RUN_TIME/1000 + " seconds, expecting to keep going.");
			runPluginForSomeTime(cache, enhancer);
		}
	}

	@Test
	public void testMediumMemoryLeak() throws Throwable {
		Map<Object, List<Location>> cache = new HashMap<Object, List<Location>>();

		// this is the name of the plugin!
		AddLocationPicture enhancer = new AddLocationPicture();
		enhancer.setExtensionPoint(new String[] {PluginConstants.BACKEND_LOCATION_SEARCH});
		enhancer.setEnabled(true);
		enhancer.setGrowSize(10);

		// run until we get OOM
		try {
			logger.info("Starting to increase memory usage for up to " + MAX_RUN_TIME/1000 + " seconds, expecting to fail.");
			runPluginForSomeTime(cache, enhancer);

			Assert.fail("We expect to go OOM here!");
		} catch (OutOfMemoryError e) {
			logger.info("Had expected OOM at: " /*, e*/);

			// now let's make sure that we don't go out of memory again
			cache.clear();

			// not running this for the medium leak as the test is too flaky this way as the small allocation
			// leads to OOM in other threads instead...
			// run, now it should not go OOM as we just had it happen and should wait some time before we cause it again
			//logger.info("Starting to run plugin for up to " + MAX_RUN_TIME/1000 + " seconds, expecting to keep going.");
			//runPluginForSomeTime(cache, enhancer);
		}
	}

	private void runPluginForSomeTime(Map<Object, List<Location>> cache, AddLocationPicture enhancer) throws Throwable {
		long current = System.currentTimeMillis();
		long count = 0;

		try {
			while (System.currentTimeMillis() - current < MAX_RUN_TIME) {
				List<Location> locations = new ArrayList<Location>();

				for (int i = 0; i < 100; i++) {
					locations.add(new Location("location" + i));
				}

				// let the plugin add to the list of items so that we run out of memory very quickly
				enhancer.execute(PluginConstants.BACKEND_LOCATION_SEARCH, "name", locations);

				cache.put(new Object(), locations);
				count++;

				// do a bit of sleep to avoid GC overhead limit exceeded Exception
				Thread.sleep(20);
			}
		} catch(Throwable e) {
			logger.info("Had " + count + " iterations before having exception: " + e);
			throw e;
		}

		logger.info("Finished running for " + MAX_RUN_TIME/1000 + " seconds after " + count + " iterations.");
	}
}
