package com.dynatrace.easytravel.pluginscheduler;

import static org.junit.Assert.*;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.*;
import org.quartz.listeners.JobListenerSupport;

import com.dynatrace.easytravel.pluginscheduler.ChainJobListener;

/**
 * Test for {@link ChainJobListener} class
 * @author cwpl-rpsciuk
 *
 */
public class ChainJobListenerTest extends SchedulerTestBase {

	/**
	 * @author cwpl-rpsciuk
	 * Test listener used to validate if jobs are executed
	 */
	class TestJobListener extends JobListenerSupport {		
		@Override
		public String getName() {
			return "testListener";
		}
		
		@Override
		public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
			JobKey key = context.getJobDetail().getKey();
			Integer count = jobExecutionCount.get(key);
			if (count == null) {
				count = new Integer(0);
			} 
			jobExecutionCount.put(key, new Integer(count.intValue()+1));
			
			
			if (jobException != null) {
				jobExecutionError.put(key, new Boolean(true));
			}
	    }	
	}
	
	private final TestJobListener testJobListener = new TestJobListener();
	private final ConcurrentHashMap<JobKey, Integer> jobExecutionCount = new ConcurrentHashMap<JobKey, Integer>();
	private final ConcurrentHashMap<JobKey, Boolean> jobExecutionError = new ConcurrentHashMap<JobKey, Boolean>();
	
	//test jobs definitions
	private JobDetail testJob = JobBuilder.newJob(TestJob.class)
			.withIdentity("testJob")
			.build();

	private JobDetail chainJob = JobBuilder.newJob(TestJob.class)
			.withIdentity("chainJob")
			.usingJobData("chainJob", "true")
			.storeDurably()
			.build();
	
	
	@Override
	@Before
	public void setup() throws SchedulerException {
		super.setup();
		scheduler.start();
		//register listeners
		scheduler.getListenerManager().addJobListener(new ChainJobListener());
		scheduler.getListenerManager().addJobListener(testJobListener);

	}

	
	@Override
	@After
	public void teardown() throws SchedulerException {
		super.teardown();
		jobExecutionCount.clear();
		jobExecutionError.clear();
	}
		
	/**
	 * Test if chainJob was executed without delay
	 * @throws SchedulerException
	 * @throws InterruptedException
	 */
	@Test
	public void testJob() throws SchedulerException, InterruptedException {
		//add chainJob
		scheduler.addJob(chainJob, true);
		//create trigger without delay
		Trigger trigger = getChainTrigger(testJob, chainJob.getKey(), 0);
		//add and schedule test job		
		scheduler.scheduleJob(testJob, trigger);

		Thread.sleep(1000);
		
		assertEquals("Job executions count should be correct " + testJob.getKey(), 1, getJobExecutionCount(testJob.getKey()));
		assertEquals("Job executions count should be correct " + chainJob.getKey(), 1, getJobExecutionCount(chainJob.getKey()));
		assertFalse("Exception in job " + testJob.getKey(), hasJobException(testJob.getKey()));
		assertFalse("Exception in job " + chainJob.getKey(), hasJobException(chainJob.getKey()));
	}
	
	/**
	 * Test if chainJob was executed with the delay
	 * @throws SchedulerException
	 * @throws InterruptedException
	 */
	@Test
	public void testJobDelay() throws SchedulerException, InterruptedException {	
		//add chainJob
		scheduler.addJob(chainJob, true);
		//create trigger that will start chainJob after 5 seconds
		Trigger trigger = getChainTrigger(testJob, chainJob.getKey(), 5);
		//add and schedule test job
		scheduler.scheduleJob(testJob, trigger);

		Thread.sleep(1000);		
		assertEquals("Job executions count should be correct " + testJob.getKey(), 1, getJobExecutionCount(testJob.getKey()));
		assertEquals("Job executions count should be correct " + chainJob.getKey(), 0, getJobExecutionCount(chainJob.getKey()));
		assertFalse("Exception in job " + testJob.getKey(), hasJobException(testJob.getKey()));
		//check if job is scheduled
		assertFalse("Chain job should be scheduled", scheduler.getTriggersOfJob(chainJob.getKey()).isEmpty());
		
		Thread.sleep(5000);
		assertEquals("Job executions count should be correct " + testJob.getKey(), 1, getJobExecutionCount(testJob.getKey()));
		assertEquals("Job executions count should be correct " + chainJob.getKey(), 1, getJobExecutionCount(chainJob.getKey()));
		assertFalse("Exception in job " + testJob.getKey(), hasJobException(testJob.getKey()));
		assertFalse("Exception in job " + chainJob.getKey(), hasJobException(chainJob.getKey()));
	}
	
	@Test
	public void testExceptionInTestJob() throws SchedulerException, InterruptedException {
		//add chainJob
		scheduler.addJob(chainJob, true);
		//crete TestJobException
		JobDetail testJobException = JobBuilder.newJob(TestJobWithException.class)
				.withIdentity("testJobException")
				.build();
		//create trigger without delay
		Trigger trigger = getChainTrigger(testJobException, chainJob.getKey(), 0);
		//add and schedule test job		
		scheduler.scheduleJob(testJobException, trigger);
		Thread.sleep(1000);		

		assertEquals("Job executions count should be correct " + testJobException.getKey(), 1, getJobExecutionCount(testJobException.getKey()));
		assertEquals("Job executions count should be correct " + chainJob.getKey(), 0, getJobExecutionCount(chainJob.getKey()));
		assertTrue("Exception in job " + testJobException.getKey(), hasJobException(testJobException.getKey()));
		assertFalse("Exception in job " + chainJob.getKey(), hasJobException(chainJob.getKey()));
	}		
	
	private int getJobExecutionCount(JobKey key) {
		Integer count = jobExecutionCount.get(key);
		return (count == null ? 0 : count.intValue());
	}
	
	private boolean hasJobException(JobKey key) {
		Boolean b = jobExecutionError.get(key);
		return (b == null ? false : b.booleanValue());
	}	
}
