package com.dynatrace.easytravel.launcher.jobs;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.KeyMatcher;
import org.quartz.listeners.JobListenerSupport;

import com.dynatrace.diagnostics.uemload.UemLoadScheduler;
import com.dynatrace.diagnostics.uemload.http.base.HostAvailability;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.engine.Batch;
import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.pluginscheduler.SchedulerTestBase;
import com.dynatrace.easytravel.util.DtVersionDetector;
import com.dynatrace.easytravel.utils.ThreadTestHelper;

import ch.qos.logback.classic.Logger;

public class ScenarioJobTest extends SchedulerTestBase{
	private static final Logger log = LoggerFactory.make();
	ScenarioJobListener jobListener;

	@Override
	@Before
	public void setup() throws SchedulerException {
		super.setup();
		scheduler.start();
		jobListener = new ScenarioJobListener();
		scheduler.getListenerManager().addJobListener(jobListener, KeyMatcher.keyEquals(new JobKey("testScenarioJob", "testGroup")));
		//assure that no scenario is running
		stopCurrentScenario();
	}

	private void stopCurrentScenario() {
		Batch runningBatch = LaunchEngine.getRunningBatch();
		if (runningBatch != null && runningBatch.getState() != State.STOPPED) {
			runningBatch.stop();
		}
	}

	@Test
	public void testStartScenario() throws SchedulerException, InterruptedException {
		// we need classic here to have the UEM/Standard Scenario available
		DtVersionDetector.enforceInstallationType(InstallationType.Classic);

		//not existing scenario
		runScenarioJob("somegroup", "some scenario");
		//scenario is not running
		ThreadTestHelper.waitForThreadToFinishSubstring("Scenario Runner");
		assertNull("Did find a scenario running but shouldn't!", LaunchEngine.getRunningBatch());
		jobListener.reset();

		runScenarioJob("Production", "Standard");
		// ensure that a scenario is running now, first wait for the thread
		ThreadTestHelper.waitForThreadToFinishSubstring("Scenario Runner");
		assertNotNull("Did not find a scenario running after starting one!", LaunchEngine.getRunningBatch());

		// first wait for non-STOPPED state
		log.info("Waiting 10 seconds for Batch to be not stopped any more");
		for(int i = 0;i < 100 && LaunchEngine.getRunningBatch().getState() == State.STOPPED;i++)  {
			Thread.sleep(100);
		}
		assertFalse("Expected Batch-State to be different than STOPPED, but still was STOPPED",
				LaunchEngine.getRunningBatch().getState() == State.STOPPED);

		log.info("Stopping all threads");
		LaunchEngine.stop();
		UemLoadScheduler.shutdownNow();
		PluginChangeMonitor.shutdown();
		HostAvailability.INSTANCE.shutdown();

		// make sure all related threads are stopped
		ThreadTestHelper.waitForThreadToFinish("Scenario Runner Standard");
		ThreadTestHelper.waitForThreadToFinishSubstring("Uem-Load");
		ThreadTestHelper.waitForThreadToFinishSubstring("Executor");
		ThreadTestHelper.waitForThreadToFinishSubstring("WATCHDOG");
	}

	private void runScenarioJob(String groupName, String scenarioName) throws SchedulerException, InterruptedException {
		JobDetail job = JobBuilder.newJob(ScenarioJob.class).withIdentity("testScenarioJob", "testGroup").storeDurably().build();
		job.getJobDataMap().put(ScenarioJob.GROUP, groupName);
		job.getJobDataMap().put(ScenarioJob.SCENARIO, scenarioName);
		scheduler.addJob(job, true);
		scheduler.triggerJob(job.getKey());

		long timeout = System.currentTimeMillis() + 100000;
		while(!jobListener.wasExecuted() && System.currentTimeMillis() < timeout) {
			Thread.sleep(500);
		}
		if (!jobListener.wasExecuted() ){
			fail("Scenario job was not executed");
		}

	}

	class ScenarioJobListener extends JobListenerSupport {
		boolean exectuted = false;

		@Override
		public String getName() { return "scenarioJobListener"; }

		@Override
		public synchronized void jobWasExecuted(JobExecutionContext context,
				JobExecutionException jobException) {
			exectuted = true;
			super.jobWasExecuted(context, jobException);
		}

		public synchronized boolean wasExecuted() {
			return exectuted;
		}

		public synchronized void reset() {
			exectuted = false;
		}

	}
}

