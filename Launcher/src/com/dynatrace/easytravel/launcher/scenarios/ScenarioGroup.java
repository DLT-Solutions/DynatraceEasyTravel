package com.dynatrace.easytravel.launcher.scenarios;

import java.util.Collection;
import java.util.List;

import com.dynatrace.easytravel.launcher.config.Persistable;

/**
 * Base interface for handling a list of scenarios in a group, e.g. "Production", "UEM", ...
 *
 * A group has a title and an ordered list of scenarios.
 */
public interface ScenarioGroup extends Persistable {

    /**
     * The title of the scenario group.
     *
     * @return the group title
     * @author martin.wurzinger
     */
    String getTitle();

    /**
     * An ordered list of scenarios this group contains.
     *
     * @return an order list of scenarios that must not be <code>null</code>.
     * @author martin.wurzinger
     */
    List<Scenario> getScenarios();

    /**
     * Append the given scenario to the list of scenarios in this group.
     *
     * @param scenario
     */
	void addScenario(Scenario scenario);

	/**
	 * Add the given scenario at the given position (0-based) to the list of scenarios in this group
	 *
	 * @param index
	 * @param scenario
	 */
	void addScenario(int index, Scenario scenario);

	/**
	 * Append all given scenarios to the list of scenarios in this group.
	 *
	 * @param scenarios
	 */
	void addScenarios(Collection<Scenario> scenarios);
}
