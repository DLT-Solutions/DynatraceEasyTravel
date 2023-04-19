package com.dynatrace.easytravel.launcher.pluginscheduler;

import org.quartz.JobKey;
import org.quartz.impl.matchers.GroupMatcher;

/**
 * Factory class for creating specific GroupMatchers
 *
 * cwpl-rorzecho
 */
public class JobGroupFactory {

	enum JobGroup {
		PLUGINS ("PluginJob"),
		SCENARIO("Scenario");

		private String name;

		private JobGroup(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private JobGroupFactory() {
	}

	public static GroupMatcher<JobKey> getPluginGroup() {
		return createJobGroup(JobGroup.PLUGINS.getName());
	}

	public static GroupMatcher<JobKey> getScenarioGroup() {
		return createJobGroup(JobGroup.SCENARIO.getName());
	}

	private static GroupMatcher<JobKey> createJobGroup(String jobGroupName) {
		return GroupMatcher.jobGroupEquals(jobGroupName);
	}
}
