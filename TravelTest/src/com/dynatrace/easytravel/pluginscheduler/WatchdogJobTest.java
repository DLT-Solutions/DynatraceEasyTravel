package com.dynatrace.easytravel.pluginscheduler;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import com.dynatrace.easytravel.launcher.engine.Batch;
import com.dynatrace.easytravel.launcher.engine.ProcedureStateListener;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.pluginscheduler.WatchdogJob;
import com.dynatrace.easytravel.launcher.scenarios.DefaultScenario;
import com.dynatrace.easytravel.utils.TestEnvironment;

/**
 * @author cwpl-rorzecho
 */
public class WatchdogJobTest extends SchedulerTestBase {

	private static final String TEST_QUARTZ_PROPERTIES = "quartz.properties";
	private static final String WATCHDOG_JOB_CONFIGURATION_FILE = "TestWatchdogJob.xml";

	private static final String path = TestEnvironment.TEST_DATA_PATH;

	String quartzWatchdogJobConfiguration = "<job-scheduling-data xmlns=\"http://www.quartz-scheduler.org/xml/JobSchedulingData\">\n" +
			"        <schedule>\n" +
			"            <job>\n" +
			"                <name>WatchdogJob</name>\n" +
			"                <group>Availability</group>\n" +
			"                <description>Watchdog job to ensure 27/7 availability</description>\n" +
			"                <job-class>com.dynatrace.easytravel.launcher.pluginscheduler.WatchdogJob</job-class>\n" +
			"                <durability>true</durability>\n" +
			"                <recover>false</recover>\n" +
			"                <job-data-map>\n" +
			"                    <entry>\n" +
			"                        <key>unavailableHosts</key>\n" +
			"                        <value>http://172.18.0.1:8080,http://172.18.0.2:8080,http://172.18.0.3:8080,http://172.18.0.4:8080</value>\n" +
			"                    </entry>\n" +
			"                </job-data-map>\n" +
			"            </job>\n" +
			"            <trigger>\n" +
			"                <cron>\n" +
			"                    <name>WatchdogJobTrigger</name>\n" +
			"                    <job-name>WatchdogJob</job-name>\n" +
			"                    <job-group>Availability</job-group>\n" +
			"                    <cron-expression>0/1 * * * * ?</cron-expression>\n" +
			"                </cron>\n" +
			"            </trigger>\n" +
			"        </schedule>\n" +
			"    </job-scheduling-data>\n";


	private Batch batch;
	private WatchdogJob watchdogJob;
	private JobKey watchdogJobKey;

	@Before
	public void setUp() throws SchedulerException {
		DefaultScenario scenario = new DefaultScenario();
		scenario.setTitle("TestScenario");
		scenario.setGroup("TestGroup");

		List<ProcedureStateListener> list = Collections.emptyList();
		batch = new Batch(scenario, list);

		assertEquals(State.STOPPED, batch.getState());


		watchdogJobKey = new JobKey("WatchdogJob", "Availability");
		TriggerKey watchdogTriggerKey = new TriggerKey("WatchdogJob", "Availability");

		CronTrigger cronTrigger = TriggerBuilder
				.newTrigger()
				.withIdentity(watchdogTriggerKey)
				.withSchedule(CronScheduleBuilder.cronSchedule("0/1 * * * * ?"))
				.build();

		JobDetail watchdogJobDetail = JobBuilder.newJob(WatchdogJob.class).withIdentity(watchdogJobKey.getName(), watchdogJobKey.getGroup()).storeDurably().build();

		scheduler.scheduleJob(watchdogJobDetail, cronTrigger);

		assertEquals(watchdogJobKey, scheduler.getJobDetail(watchdogJobKey).getKey());

		watchdogJob = new WatchdogJob();

		scheduler.start();
	}

	@Test
	@Ignore("Sometimes fails and we can't reproduce it")
	public void testBatchIsStopped() throws SchedulerException, InterruptedException {
		// set Batch for WatchdogJob monitoring
		watchdogJob.setBatch(batch);

		// first Batch should be STOPPED
		assertEquals(State.STOPPED, batch.getState());

		// WatchdogJob should have the same Batch instance
		assertSame(batch, watchdogJob.getBatch());

		// WatchdogJob Batch state should also be STOPPED
		assertEquals(State.STOPPED, watchdogJob.getBatch().getState());

		// let execute WatchdogJob several times
		Thread.sleep(TimeUnit.SECONDS.toMillis(2));

		// after execution Batch instance should be the same

		// unstable check needs to be investigate, from time to time it is failing
		assertSame(batch, watchdogJob.getBatch());

		// set Batch to null
		watchdogJob.setBatch(null);

		// check if there is no Batch available
		assertEquals(null, watchdogJob.getBatch());
	}

	@Test
	public void testBachIsStarted() throws InterruptedException {
		// start Batch
		batch.start();

		watchdogJob.setBatch(batch);

		// let execute WatchdogJob several times
		Thread.sleep(TimeUnit.SECONDS.toMillis(2));

		// new instance of Bach is set to watchdogJob
		assertSame(batch, watchdogJob.getBatch());

		// the Batch should be in OPERATING state
		assertEquals(State.OPERATING, watchdogJob.getBatchState());

		// stop Batch
		batch.stop();

		// let execute WatchdogJob several times
		Thread.sleep(TimeUnit.SECONDS.toMillis(2));

		assertEquals(State.STOPPED, watchdogJob.getBatchState());
	}

	@Test
	public void testHostUnavailability() throws InterruptedException, SchedulerException {
		String UNAVAILABLE_HOSTS_KEY = "unavailableHosts";
		String UNAVAILABLE_HOSTS = "http://xxx:7777";

		watchdogJob.setBatch(batch);

		batch.start();

		assertEquals(State.OPERATING, watchdogJob.getBatchState());


		// get JobDataMap for WatchdogJob
		JobDetail watchdogJobDetail = scheduler.getJobDetail(watchdogJobKey);
		JobDataMap watchdogJobDataMap = watchdogJobDetail.getJobDataMap();

		// update JobDataMap for register unexpected unavailableHosts for monitoring
		watchdogJobDataMap.put(UNAVAILABLE_HOSTS_KEY, UNAVAILABLE_HOSTS);
		scheduler.addJob(watchdogJobDetail, true);

		// check if thre are any hosts monitored for unespected unanavailable
		// for now there should be no unavailable hosts
		assertFalse(watchdogJob.getHostAvailability().isAnyUnexpectedUnavailable());

		// let execute WatchdogJob several times
		// during next execution of WatchdogJob host unavailability is checked
		for(int i = 0; i < 10;i++) {
			Thread.sleep(TimeUnit.SECONDS.toMillis(2));
			if(watchdogJob.getHostAvailability().isAnyUnexpectedUnavailable()) {
				break;
			}
		}

		// there should be host unavavailable
		assertTrue("Expected to have unexpected unavailable hosts, but had none",
				watchdogJob.getHostAvailability().isAnyUnexpectedUnavailable());

		// thres should be one unexpected host unavailable
		assertEquals(1, watchdogJob.getHostAvailability().getUnexpectedHostUvavailable().size());
		assertEquals("http://xxx:7777", watchdogJob.getHostAvailability().getUnexpectedHostUvavailable().toArray()[0]);

		// hard to detect Batch state change from STOPPED to OPERATING during host unavalibility
		// during the WatchdogJob execution the Batch changes its state from stopped to operating again
		// so after this test execution the Batch state is OPERATING again beceause restart is performed
		// and for now cannot detect Batch STOPPED state

		// after restart Batch should be in OPERATING state
		assertEquals(State.OPERATING, watchdogJob.getBatchState());

	}

	@Test
	public void textExecuteWatchdogJobFromXMLDefinition() throws IOException, SchedulerException, InterruptedException {
		// create WatchdogJob configuration
		File watchdogJobcConfig = new File(path, WATCHDOG_JOB_CONFIGURATION_FILE);
		FileUtils.writeStringToFile(watchdogJobcConfig, quartzWatchdogJobConfiguration);

		Properties quartzProperties = new Properties();
		String testQuartzPropertiesPath = new File(path, TEST_QUARTZ_PROPERTIES).getPath();
		String testJobFileNamePath = new File(path, WATCHDOG_JOB_CONFIGURATION_FILE).getPath();

		FileInputStream fileInputStream = new FileInputStream(testQuartzPropertiesPath);
		quartzProperties.load(fileInputStream);

		quartzProperties.put("org.quartz.plugin.jobInitializer.class","org.quartz.plugins.xml.XMLSchedulingDataProcessorPlugin");
		quartzProperties.put("org.quartz.plugin.jobInitializer.fileNames", testJobFileNamePath);

		scheduler = new StdSchedulerFactory(quartzProperties).getScheduler();

		assertNotNull(scheduler);

		scheduler.start();
		Thread.sleep(TimeUnit.SECONDS.toMillis(2));
		assertEquals(1, scheduler.getJobKeys(GroupMatcher.jobGroupEquals("Availability")).size());

		FileUtils.deleteQuietly(watchdogJobcConfig);
	}

}
