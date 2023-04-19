package com.dynatrace.easytravel.launcher.pluginscheduler;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.launcher.engine.BatchStateListener;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * @author cwpl-rorzecho
 */
public class PluginSchedulerManager implements BatchStateListener {
	private static final Logger LOGGER = Logger.getLogger(PluginSchedulerManager.class.getName());

	private final Scheduler scheduler;

	private static GroupMatcher<JobKey> pluginJobGroup = JobGroupFactory.getScenarioGroup();

	public PluginSchedulerManager(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public void notifyBatchStateChanged(Scenario scenario, State oldState, State newState) {
		if (State.STARTING == oldState && State.OPERATING == newState) {
			resumePluginJobGroup(pluginJobGroup);
		} else if (State.OPERATING == oldState && State.STOPPING == newState) {
			pausePluginJobGroup(pluginJobGroup);
		}
	}

	/**
	 * Pause jobs for defined jobGroup
	 * @param jobGroup
	 */
	private void pausePluginJobGroup(GroupMatcher<JobKey> jobGroup) {
		String jobGroupName = jobGroup.getCompareToValue();
		try {
			scheduler.pauseJobs(jobGroup);
			LOGGER.log(Level.INFO, TextUtils.merge("Jobs {0} for {1} group has been paused", getJobNamesForJobGroup(jobGroup).toString(), jobGroupName));
		} catch (SchedulerException e) {
			LOGGER.log(Level.WARNING, TextUtils.merge("Jobs for {0} group cannot be paused", jobGroupName), e);
		}
	}

	/**
	 * Resume paused jobs for defined jobGroup
	 * @param jobGroup
	 */
	private void resumePluginJobGroup(GroupMatcher<JobKey> jobGroup) {
		String jobGroupName = jobGroup.getCompareToValue();
		try {
			scheduler.resumeJobs(jobGroup);
			LOGGER.log(Level.INFO, TextUtils.merge("Jobs {0} for {1} group has been resumed", getJobNamesForJobGroup(jobGroup).toString(), jobGroupName));
		} catch (SchedulerException e) {
			LOGGER.log(Level.WARNING, TextUtils.merge("Cannot resume jobs for {0} group", jobGroupName), e);
		}
	}

	/**
	 * Get job names assigned to jobGroup
	 * @param jobGroup
	 * @return
	 */
	private Set<String> getJobNamesForJobGroup(GroupMatcher<JobKey> jobGroup) {
		Set<String> jobNames = new TreeSet<String>();
		try {
			Set<JobKey> pluginsForGroup = scheduler.getJobKeys(jobGroup);
			for (JobKey jobKey : pluginsForGroup) {
				jobNames.add(jobKey.getName());
			}
		} catch (SchedulerException e) {
			LOGGER.log(Level.WARNING, TextUtils.merge("Cannot get jobs for {} group", jobGroup.getCompareToValue()), e);
		}
		return jobNames;
	}

	@TestOnly
	public void setPluginJobGroup(GroupMatcher<JobKey> jobGroup) {
		PluginSchedulerManager.pluginJobGroup = jobGroup;
	}
}
