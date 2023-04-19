package com.dynatrace.easytravel.pluginscheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import com.dynatrace.easytravel.launcher.pluginscheduler.JobGroupFactory;
import com.dynatrace.easytravel.utils.TestEnvironment;

/**
 * cwpl-rorzecho
 */
public class JobSchedulingDataProcessorPluginTest {

	private static final String TEST_QUARTZ_PROPERTIES = "quartz.properties";
	private static final String JOB_FILE_NAME = "scenarios.xml";

	private Scheduler scheduler;

	@Before
	public void setup () throws SchedulerException, IOException {

		Properties quartzProperties = new Properties();

		String testQuartzPropertiesPath = new File(TestEnvironment.TEST_DATA_PATH, TEST_QUARTZ_PROPERTIES).getPath();
		String testJobFileNamePath = new File(TestEnvironment.TEST_DATA_PATH, JOB_FILE_NAME).getPath();

		FileInputStream fileInputStream = new FileInputStream(testQuartzPropertiesPath);
		quartzProperties.load(fileInputStream);

		quartzProperties.put("org.quartz.plugin.jobInitializer.class","com.dynatrace.easytravel.pluginscheduler.JobSchedulingDataProcessorPlugin");
		quartzProperties.put("org.quartz.plugin.jobInitializer.fileNames", testJobFileNamePath);

		scheduler = new StdSchedulerFactory(quartzProperties).getScheduler();

		assertNotNull(scheduler);

		assertEquals("There should be no jobs in the scheduler", 0, getJobsCount(scheduler));
	}

	@Test
	public void testInitSchedulerWithScenarioDefinedJobs() throws SchedulerException, InterruptedException {
		scheduler.start();

		// pause scenario jobs
		scheduler.pauseJobs(JobGroupFactory.getScenarioGroup());

		assertEquals("Scheduler should be initialized with jobs from scenarios.xml file", 2, getJobsCount(scheduler));

		/*TriggerKey dummyTrigger = new TriggerKey("DummyJobTrigger", "DummyGroup");
		TriggerKey scenarioTrigger = new TriggerKey("ScenarioJobTrigger","Scenario");*/

		assertEquals("Scheduler should be started", true, scheduler.isStarted());

		assertEquals("There should be two group names", 2, scheduler.getJobGroupNames().size());

		assertEquals("Scenario group should be available", "Scenario", scheduler.getJobGroupNames().get(0));
		assertEquals("DummyGroup group should be available", "DummyGroup", scheduler.getJobGroupNames().get(1));
	}

	protected int getJobsCount(Scheduler scheduler) throws SchedulerException {
		int cnt = 0;
		for (String groupName : scheduler.getJobGroupNames()) {
			cnt += scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)).size() ;
		}
		return cnt;
	}
}
