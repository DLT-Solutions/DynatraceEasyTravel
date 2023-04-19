package com.dynatrace.easytravel.pluginscheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Job instance not related with easyTravel plugins
 * to initiate with Quartz-Scheduling job configuration
 *
 * cwpl-rorzecho
 */
public class DummyJob implements Job {
	private static Logger LOGGER = LoggerFactory.make();

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOGGER.info("+++ Dummy Job +++");
	}
}
