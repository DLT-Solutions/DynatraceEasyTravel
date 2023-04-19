package com.dynatrace.easytravel.pluginscheduler;

import java.util.logging.Logger;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.dynatrace.easytravel.util.TextUtils;

/**
 * @author cwpl-rpsciuk
 * Test helper class. 
 * Job throws exception in the execute method
 */
public class TestJobWithException implements Job {

	private static final Logger LOGGER = Logger.getLogger(TestJobWithException.class.getName());
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		LOGGER.info(TextUtils.merge("Exectue job. key: {0}", context.getJobDetail().getKey()));
		throw new JobExecutionException("some exception");
	}
}
