package com.dynatrace.easytravel.pluginscheduler;

import java.util.Date;
import java.util.List;

import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Job instance not related with easyTravel plugins
 * to initiate with Quartz-Scheduling job configuration
 *
 * cwpl-rorzecho
 */
public class SchedulerStatus implements Job {
	private static Logger LOGGER = LoggerFactory.make();

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOGGER.info("--------------------------> SCHEDULER  STATUS - Start <--------------------------");

		try {
			for (String groupName : Quartz.getScheduler().getJobGroupNames()) {

			     for (JobKey jobKey : Quartz.getScheduler().getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

				  String jobName = jobKey.getName();
				  String jobGroup = jobKey.getGroup();

				  //get job's trigger
				  List<? extends Trigger> triggers = Quartz.getScheduler().getTriggersOfJob(jobKey);
				  int triggersSize = triggers.size();
				  for (int i=0; i<triggersSize; i++) {
				  Date nextFireTime = triggers.get(i).getNextFireTime();
					LOGGER.info("[jobName] : <" + jobName + "> [groupName] : <" + jobGroup + "> - NEXT fire time: <" + nextFireTime + ">");
				  }
				  }

			 }
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LOGGER.info("-------------------------->  SCHEDULER  STATUS - End  <--------------------------");
	}
}
