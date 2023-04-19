package com.dynatrace.easytravel.launcher.engine;

import com.dynatrace.easytravel.launcher.scenarios.Scenario;


/**
 * Instances of this class can be used to keep track of the current scenario.
 *
 * @author stefan.moschinski
 */
public interface ScenarioListener {

	/**
	 * Notifies the listening objects about the current scenario.
	 *
	 * @param scenario the scenario that is currently active or null if no scenario is running.
	 * @author stefan.moschinski
	 */
	void notifyScenarioChanged(Scenario scenario);
}
