package com.dynatrace.easytravel.pluginscheduler;

import java.util.Map;
import java.util.logging.Logger;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.dynatrace.easytravel.util.TextUtils;

/**
 * Simple job for tests
 * @author cwpl-rpsciuk
 *
 */
public class TestJob implements Job{

	private static final Logger LOGGER = Logger.getLogger(TestJob.class.getName());
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		String triggerParams = printParameters(context.getTrigger().getJobDataMap());
		String jobParams = printParameters(context.getJobDetail().getJobDataMap());
		LOGGER.info(TextUtils.merge("Exectue job. key: {0}", context.getJobDetail().getKey()));		
		LOGGER.info(TextUtils.merge("Exectue job. Trigger parameters: {0}", triggerParams));
		LOGGER.info(TextUtils.merge("Exectue job. Job parameters: {0}", jobParams));
	}
	
	private String printParameters(JobDataMap map) {
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, Object> entry : map.entrySet()){
			sb.append(entry.getKey())
			.append("=")
			.append(entry.getValue())
			.append(",");
		}
		return sb.toString();
	}

}
