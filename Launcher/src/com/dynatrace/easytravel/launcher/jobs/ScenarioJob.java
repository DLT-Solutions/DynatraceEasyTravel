package com.dynatrace.easytravel.launcher.jobs;

import java.util.logging.Logger;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * @author cwpl-rpsciuk
 * Quartz job for starting scenarios.
 * Parameters (read from job and trigger):
 *  group, scenario - group and scenario name to be started    
 */
public class ScenarioJob implements Job{

	private static Logger LOGGER = Logger.getLogger(ScenarioJob.class.getName());
	
	public static final String SCENARIO ="scenario";
	public static final String GROUP ="group";
		
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
	
		String scenarioName = context.getMergedJobDataMap().getString(SCENARIO);
		String scenarioGroup = context.getMergedJobDataMap().getString(GROUP);

		LOGGER.info(TextUtils.merge("Scenario job started. Starting scenario {0} from group {1}", scenarioName, scenarioGroup));
		
		Scenario scenario = LaunchEngine.findScenario(scenarioGroup, scenarioName);
		if (scenario != null) {			
			LaunchEngine.getNewInstance().run(scenario);
			LOGGER.fine(TextUtils.merge("Sceanrio {0} from group {1} started", scenarioName, scenarioGroup));
		} else {
			LOGGER.warning(TextUtils.merge("Scenario {0} not found for group {1}", scenarioName, scenarioGroup));
		}
	}

}
