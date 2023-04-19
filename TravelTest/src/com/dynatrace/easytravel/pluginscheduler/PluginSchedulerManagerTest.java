package com.dynatrace.easytravel.pluginscheduler;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import com.dynatrace.easytravel.launcher.engine.Batch;
import com.dynatrace.easytravel.launcher.engine.BatchStateListener;
import com.dynatrace.easytravel.launcher.engine.ProcedureStateListener;
import com.dynatrace.easytravel.launcher.pluginscheduler.JobGroupFactory;
import com.dynatrace.easytravel.launcher.pluginscheduler.PluginSchedulerManager;
import com.dynatrace.easytravel.launcher.scenarios.DefaultScenario;

/**
 * Test for {@link PluginSchedulerManager} class
 * @author cwpl-rorzecho
 */
public class PluginSchedulerManagerTest extends SchedulerTestBase {
	private static final Logger LOGGER = Logger.getLogger(PluginSchedulerManager.class.getName());

	private TriggerKey triggerKey;

	@Before
	public void testAddJobsToScheduler() throws SchedulerException {
		// define group name for plugin jobs
		String pluginGroupName = "Scenario";

		// define GroupMacher for plugin jobs group
		GroupMatcher<JobKey> pluginJobGroup = JobGroupFactory.getScenarioGroup();

		// check if defined pluginGroupName equals
		assertEquals(pluginGroupName, pluginJobGroup.getCompareToValue());

		// create jobKeys
		JobKey jobKey1 = new JobKey("testJob1", pluginGroupName);
		JobKey jobKey2 = new JobKey("testJob2", pluginGroupName);
		JobKey jobKey3 = new JobKey("testJob3", pluginGroupName);

		// create jobs for jobKeys
		JobDetail job1 = createPluginJob(jobKey1);
		JobDetail job2 = createPluginJob(jobKey2);
		JobDetail job3 = createPluginJob(jobKey3);

		// add jobs to scheduler
		scheduler.addJob(job1, true);
		scheduler.addJob(job2, true);

		// check if scheduler contains jobs with specified jobKey
		assertEquals(jobKey1, scheduler.getJobDetail(jobKey1).getKey());
		assertEquals(jobKey2, scheduler.getJobDetail(jobKey2).getKey());

		// check if scheduler contains specified jobs
		assertEquals(job1, scheduler.getJobDetail(jobKey1));
		assertEquals(job2, scheduler.getJobDetail(jobKey2));

		// create triggerKey
		triggerKey = new TriggerKey("testJob3Trigger", pluginGroupName);

		// create CronTrigger for triggerKey identity
		CronTrigger cronTrigger = createCronTrigger(triggerKey);

		// schedule job with specified trigger
		scheduler.scheduleJob(job3, cronTrigger);

		// check if scheduler contains job with specified jobKey
		assertEquals(job3, scheduler.getJobDetail(jobKey3));

		// check if scheduler contains specified cronTrigger
		assertEquals(cronTrigger, scheduler.getTrigger(triggerKey));

		// check if there is only one group
		assertEquals(1, scheduler.getJobGroupNames().size());

		// check if scheduler contains PluginJob group
		assertEquals(pluginGroupName, scheduler.getJobGroupNames().get(0));

		// check if there are three jobs in PluginJob group
		assertEquals(3, scheduler.getJobKeys(pluginJobGroup).size());

		// pause jobs for PluginJob group
		scheduler.pauseJobs(pluginJobGroup);

		// start scheduler
		scheduler.start();

		// check if scheduler is started
		assertTrue(scheduler.isStarted());

		// check if trigger is in PAUSED state
		assertEquals(Trigger.TriggerState.PAUSED, scheduler.getTriggerState(triggerKey));
	}

	@Test
	public void testSchedulerWhenBatchStateChanged() throws SchedulerException, InterruptedException {
		PluginSchedulerManager pluginSchedulerManager = new PluginSchedulerManager(scheduler);

		assertNotNull(pluginSchedulerManager);

		assertTrue(pluginSchedulerManager instanceof BatchStateListener);

		// cleate BatchStateListener collection
		Collection<BatchStateListener> batchStateListeners = new ArrayList<BatchStateListener>();
		batchStateListeners.add(pluginSchedulerManager);

		assertTrue("BatchStateListener contains PluginSchedulermanager", batchStateListeners.contains(pluginSchedulerManager));

		List<ProcedureStateListener> list = Collections.emptyList();
		Batch batch = new Batch(new DefaultScenario(), list);
		batch.addBatchStateListeners(batchStateListeners);

		assertEquals("One BatchStateListener should be available", 1, batch.getBatchStateListeners().size());
		assertTrue(batch.getBatchStateListeners().get(0) instanceof PluginSchedulerManager);

		assertTrue("Scheduler should be started", scheduler.isStarted());

		// start batch
		batch.start();

		// wait for two seconds for TestJob cronTrigger,
		Thread.sleep(TimeUnit.SECONDS.toMillis(2));

		// trigger should be in normal state
		assertEquals(Trigger.TriggerState.NORMAL, scheduler.getTriggerState(triggerKey));

		// stop batch
		batch.stop();

		// now again trigger should be in paused state
		assertEquals(Trigger.TriggerState.PAUSED, scheduler.getTriggerState(triggerKey));
	}

	private JobDetail createPluginJob(JobKey jobKey) throws SchedulerException {
		JobDetail jobDetail = JobBuilder.newJob(TestJob.class).withIdentity(jobKey.getName(), jobKey.getGroup()).storeDurably().build();
		return jobDetail;
	}

	private Trigger createTrigger(String triggerName, String jobGroup) {
		Trigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity(triggerName, jobGroup)
				.startNow()
				.build();
		return trigger;
	}

	private CronTrigger createCronTrigger(TriggerKey triggerKey) {
		CronTrigger cronTrigger = TriggerBuilder
				.newTrigger()
				.withIdentity(triggerKey)
				.withSchedule(CronScheduleBuilder.cronSchedule("0/1 * * * * ?"))
				.build();
		return cronTrigger;
	}
}
