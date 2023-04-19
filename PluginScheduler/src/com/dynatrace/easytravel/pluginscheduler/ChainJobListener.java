package com.dynatrace.easytravel.pluginscheduler;

import org.quartz.*;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.listeners.JobListenerSupport;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

/**
 * {@link JobListener} that provides chainJob functionality. A chain job is a job started after execution of other job.
 * Chain job is defined in trigger's JobDataMap: 
 * chainJobName, chainJobGroup - job identity
 * chainJobDelay - delay in seconds for scheduling chainJob. 
 * A chainJob must be defined in the scheduler to be started. 
 * If original job has an exception in it's execute method, a chain job will not be started.
 * A chainJob is scheduled only for single execution.
 *  
 * @author cwpl-rpsciuk
 *
 */
public class ChainJobListener extends JobListenerSupport {

	private static Logger LOGGER = LoggerFactory.make();
	public static final String CHAIN_JOB_NAME = "chainJobName";
	public static final String CHAIN_JOB_GROUP = "chainJobGroup";
	public static final String CHAIN_JOB_DELAY = "chainJobDelay";
	
	@Override
	public String getName() {
		return "ChainJobListener";
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		try {
			JobKey orgJob = context.getJobDetail().getKey();
			JobKey jobKey = getChainJobKey(context);
			if (jobKey == null ) { //no chain job in the paramters, return
				return;
			}		

			LOGGER.debug(TextUtils.merge("Chain job parameter {0} foun for job {1}", jobKey, orgJob));
			//check if org job finished its execution withou error
			if (jobException != null ) {
				LOGGER.info(TextUtils.merge("Job {0} finished with exception. Its chain job {1} will not be scheduled.",  orgJob, jobKey));
				return;
			}

			Scheduler scheduler = context.getScheduler(); 
			JobDetail chainJob = null;
			try {
				chainJob = scheduler.getJobDetail(jobKey);
			} catch (SchedulerException e) {
				LOGGER.error(TextUtils.merge("Error getting job from scheduler for key: {0}", jobKey),e);
			}
			
			if (chainJob == null) {
				LOGGER.error(TextUtils.merge("Cannot find chain job in the scheduler for key: {0}",jobKey));
				return;
			}
			

			int delay = getChainJobDelay(context);		
			LOGGER.info(TextUtils.merge("Scheduling chain job {0} for {1} job with delay {2}s", jobKey, orgJob, delay));		
			Trigger chainTrigger = getChainTrigger(chainJob, delay);		

			try{
				scheduler.scheduleJob(chainTrigger);
			} catch (SchedulerException e) {
				LOGGER.error(TextUtils.merge("Error scheduling chain job {0} for job {1}", jobKey, orgJob), e);
			}
		} catch (Exception e) { //catch all exceptions to allow other listeners to execute
			LOGGER.error("Unknown expcetion occured in ChainJobListener", e);
		}
	}
	
	/**
	 * Create trigger for a chainJob
	 * @param chainJob
	 * @param delay
	 * @return
	 */
	private Trigger getChainTrigger(JobDetail chainJob, int delay) {
		Trigger chainTrigger = TriggerBuilder
				.newTrigger()
				.forJob(chainJob)
				.withIdentity(chainJob.getKey().toString()+"Trigger")
				.startAt(DateBuilder.futureDate(delay, IntervalUnit.SECOND))
				.withSchedule(
						SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow()
						.withRepeatCount(0) //this means that job will be started 2 times
						)
				.withPriority(1)
				.build();
		
		return chainTrigger;
	}
	
	private JobKey getChainJobKey(JobExecutionContext context) {
		//check trigger parameters
		String group = null;
		String name = context.getTrigger().getJobDataMap().getString(CHAIN_JOB_NAME);		
		if(name != null) {
			group = context.getTrigger().getJobDataMap().getString(CHAIN_JOB_GROUP);
			return new JobKey(name, group);
		}
		
		return null;
	}
	
	private int getChainJobDelay(JobExecutionContext context) {
		if (context.getTrigger().getJobDataMap().get(CHAIN_JOB_DELAY) != null) {
			return context.getTrigger().getJobDataMap().getInt(CHAIN_JOB_DELAY);
		}
		return 0;
	}		
}
