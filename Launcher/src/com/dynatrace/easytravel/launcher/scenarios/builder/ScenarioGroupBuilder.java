package com.dynatrace.easytravel.launcher.scenarios.builder;

import com.dynatrace.easytravel.launcher.scenarios.DefaultScenarioGroup;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.launcher.scenarios.ScenarioGroup;


public class ScenarioGroupBuilder {

	private ScenarioGroup group;


	public static ScenarioGroupBuilder group(String groupName) {
		return new ScenarioGroupBuilder(groupName);
	}

	private ScenarioGroupBuilder(String groupName) {
		this.group = new DefaultScenarioGroup(groupName);
	}

	public ScenarioGroupBuilder add(Scenario scenario) {
		group.addScenario(scenario);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T extends ScenarioGroup> T create() {
		return (T) group;
	}

}
