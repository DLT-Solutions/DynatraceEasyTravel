package com.dynatrace.easytravel.pluginscheduler;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;
import org.quartz.Scheduler;

/**
 * cwpl-rorzecho
 */
public class QuartzTest extends SchedulerTestBase {

	@Test
	public void initializeScheduler() throws FileNotFoundException {
		Quartz.initialize(testQuartzPropertiesPath, "");
		Scheduler scheduler1 = Quartz.getScheduler();
		assertNotNull(scheduler1);
		Scheduler scheduler2 = Quartz.getScheduler();
		assertSame(scheduler1, scheduler2);
	}

	@Test
	public void testFindQuartzDataFile() {
		Properties properties = Quartz.getQuartzProperties();
		assertNotNull(properties);
	}

	@Test
	public void testQuartzPropertyFile() {
		Quartz.initialize(testQuartzPropertiesPath, "");
		Properties properties = Quartz.getQuartzProperties();
		Set<String> propertyNames = properties.stringPropertyNames();

		// testing property names
		assertEquals(15, propertyNames.size());
		assertTrue(propertyNames.contains("org.quartz.scheduler.instanceName"));
		assertTrue(propertyNames.contains("org.quartz.jobStore.class"));
		assertTrue(propertyNames.contains("org.quartz.scheduler.jobFactory.class"));

		// for this test there is no jobInitializer.class to load quartz_data.xml file
		assertFalse(propertyNames.contains("org.quartz.plugin.jobInitializer.class"));
		assertFalse(propertyNames.contains("org.quartz.plugin.jobInitializer.fileNames"));

		assertTrue(propertyNames.contains("org.quartz.threadPool.class"));
		assertTrue(propertyNames.contains("org.quartz.scheduler.skipUpdateCheck"));
		assertTrue(propertyNames.contains("org.quartz.threadPool.threadCount"));

		// testing property values
		assertEquals(properties.getProperty("org.quartz.scheduler.instanceName"), "TestPluginScheduler");
		assertEquals(properties.getProperty("org.quartz.jobStore.class"), "org.quartz.simpl.RAMJobStore");
		assertEquals(properties.getProperty("org.quartz.scheduler.jobFactory.class"), "org.quartz.simpl.SimpleJobFactory");
		assertEquals(properties.getProperty("org.quartz.threadPool.class"), "org.quartz.simpl.SimpleThreadPool");
		assertEquals(properties.getProperty("org.quartz.scheduler.skipUpdateCheck"), "true");
		assertEquals(properties.getProperty("org.quartz.threadPool.threadCount"), "3");
	}

}
