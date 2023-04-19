package com.dynatrace.easytravel.pluginscheduler;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.utils.HTTPResponseRunnableImpl;
import com.dynatrace.easytravel.utils.MockRESTServer;


/**
 * Test for {@link PluginJob} class
 * @author cwpl-rpsciuk
 *
 */
public class PluginJobTest extends SchedulerTestBase {

	@Override
	@Before
	public void setup() throws SchedulerException {
		super.setup();
		scheduler.start();
	}

	@Test
	public void testPluginJobEnable() throws SchedulerException, InterruptedException, IOException {
		String[] responses = { "ok", "ok" };
		String[][] requests = { 
				{"/services/ConfigurationService/registerPlugins", "pluginData=somePlugin" },
				{"/services/ConfigurationService/setPluginEnabled", "name=somePlugin", "enabled=true"}, 
		};

		runPluginJob(responses, requests, PluginJob.START_ACTION);
	}
	
	@Test
	public void testPluginJobDisable() throws SchedulerException, InterruptedException, IOException {
		String[] responses = { "ok"};
		String[][] requests = { 
				{"/services/ConfigurationService/setPluginEnabled", "name=somePlugin", "enabled=false"}, 
		};

		runPluginJob(responses, requests, PluginJob.STOP_ACTION);
	}

	@Ignore("Integration test only")
	@Test
	public void testPluginJobException() throws InterruptedException, SchedulerException, IOException {				
		JobDetail job = JobBuilder.newJob(PluginJob.class).withIdentity("testJob", "testGroup").storeDurably().build();
		job.getJobDataMap().put(PluginJob.PLUGIN_NAME_KEY, "somePlugin");			
		job.getJobDataMap().put(PluginJob.PLUGIN_ACTION_KEY, PluginJob.STOP_ACTION);
		scheduler.addJob(job, true);
		scheduler.triggerJob(job.getKey());
							
		waitForJobsCompletion();
	}		

	/**
	 * Helper method that tests if job enables/disables plugin
	 * @param enablePlugin
	 * @throws SchedulerException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private void runPluginJob(String[] responses, String[][] requests, String pluginAction) throws SchedulerException, InterruptedException, IOException {		
		JobDetail job = JobBuilder.newJob(PluginJob.class).withIdentity("testJob", "testGroup").storeDurably().build();
		job.getJobDataMap().put(PluginJob.PLUGIN_NAME_KEY, "somePlugin");			
		job.getJobDataMap().put(PluginJob.PLUGIN_ACTION_KEY, pluginAction);
		scheduler.addJob(job, true);
		
		HTTPResponseRunnableImpl resp = new HTTPResponseRunnableImpl(requests, responses);
		MockRESTServer server = new MockRESTServer(resp);						
		try {
			EasyTravelConfig.read().backendPort = server.getPort();
			EasyTravelConfig.read().webServiceBaseDir="http://localhost:" + server.getPort() + "/services/";

			scheduler.triggerJob(job.getKey());				
			assertEquals("There should be 1 job in the scheduler", 1, getJobsCount(scheduler));			
			//wait for scheduled job to be called
			waitForJobsCompletion();
			resp.checkFailure();
		} finally {
			server.stop();
			EasyTravelConfig.resetSingleton();
		}
	}
	
	/**
	 * Test the whole run of PluginJob:
	 * - job is executed and enabling plugin
	 * - chain job is scheduled
	 * - job is executed and disabling plugin
	 * @throws SchedulerException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testEnableDisableWithScheduler() throws SchedulerException, IOException, InterruptedException {
		String[] responses = { "ok", "ok", "ok" };
		String[][] requests = { 
				{"/services/ConfigurationService/registerPlugins", "pluginData=somePlugin" },
				{"/services/ConfigurationService/setPluginEnabled", "name=somePlugin", "enabled=true"}, 
				{"/services/ConfigurationService/setPluginEnabled", "name=somePlugin", "enabled=false"},
		};		
		
		JobDetail job = JobBuilder.newJob(PluginJob.class).withIdentity("testJob", "testGroup").storeDurably().build();
		job.getJobDataMap().put(PluginJob.PLUGIN_NAME_KEY, "somePlugin");			
		scheduler.addJob(job, true);		
		scheduler.getListenerManager().addJobListener(new ChainJobListener());

		Trigger trigger = getChainTrigger(job, job.getKey(), 1);
		trigger.getJobDataMap().put(PluginJob.PLUGIN_ACTION_KEY, PluginJob.START_ACTION);

		HTTPResponseRunnableImpl resp = new HTTPResponseRunnableImpl(requests, responses);
		MockRESTServer server = new MockRESTServer(resp);						
		try {
			EasyTravelConfig.read().backendPort = server.getPort();
			EasyTravelConfig.read().webServiceBaseDir="http://localhost:" + server.getPort() + "/services/";

			scheduler.scheduleJob(trigger);				
			assertEquals("There should be 1 jobs in the scheduler", 1, getJobsCount(scheduler));			
			//wait for scheduled job & chain job to be called
			Thread.sleep(5*1000);			
			resp.checkFailure();				
			assertEquals("There should be 3 requests to ConfigurationService", 3, resp.getNumberOfRequests());
		} finally {
			server.stop();
			EasyTravelConfig.resetSingleton();
		}
	}	
}
