package com.dynatrace.easytravel.pluginscheduler;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import com.dynatrace.easytravel.utils.TestEnvironment;

/**
 * Base class for all Quartz scheduler tests
 * @author cwpl-rpsciuk
 *
 */

public class SchedulerTestBase {

	protected Scheduler scheduler;

	private static final String TEST_QUARTZ_PROPERTIES = "quartz.properties";

	protected String testQuartzPropertiesPath;

		@Before
		public void setup ()throws SchedulerException {
			testQuartzPropertiesPath = new File(TestEnvironment.TEST_DATA_PATH, TEST_QUARTZ_PROPERTIES).getPath();
			scheduler = new StdSchedulerFactory(testQuartzPropertiesPath).getScheduler();
			assertEquals("There should be no jobs in the scheduler", 0, getJobsCount(scheduler));
		}

		@After
		public void teardown ()throws SchedulerException {
			if (scheduler != null) {
				scheduler.shutdown();
			}
		}

		/**
		 * @param scheduler
		 * @return jobs count in the scheduler
		 * @throws SchedulerException
		 */

	protected int getJobsCount(Scheduler scheduler) throws SchedulerException {
		int cnt = 0;
		for (String groupName : scheduler.getJobGroupNames()) {
			cnt += scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)).size() ;
		}
		return cnt;
	}
	
	protected void waitForJobsCompletion() throws SchedulerException, InterruptedException {				
		Thread.sleep(1000);		
		while(scheduler.getCurrentlyExecutingJobs().size() > 0) {
			Thread.sleep(1000);
		}
	}

	/**
	 * Create a trigger that contain parameters to start chainJob after specified delay
	 * @param delay delay in seconds
	 * @param jobDetail
	 * @return trigger
	 */
	public static Trigger getChainTrigger(JobDetail jobDetail, JobKey chainJobKey, int delay) {
		TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder
				.newTrigger()
				.forJob(jobDetail)
				.usingJobData(ChainJobListener.CHAIN_JOB_NAME, chainJobKey.getName())
				.usingJobData(ChainJobListener.CHAIN_JOB_GROUP, chainJobKey.getGroup());
		if (delay > 0) {
			triggerBuilder.usingJobData(ChainJobListener.CHAIN_JOB_DELAY, new Integer(delay));
		}
		return triggerBuilder.build();
	}
}
