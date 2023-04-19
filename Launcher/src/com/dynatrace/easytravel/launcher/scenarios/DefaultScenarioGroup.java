package com.dynatrace.easytravel.launcher.scenarios;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.configuration.tree.ConfigurationNode;

import com.dynatrace.easytravel.config.ConfigurationException;
import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.config.ConfigurationReader;
import com.dynatrace.easytravel.launcher.config.NodeFactory;
import com.dynatrace.easytravel.launcher.misc.Constants.ConfigurationXml;
import com.dynatrace.easytravel.util.DtVersionDetector;


public class DefaultScenarioGroup implements ScenarioGroup {

    private static final String NODE_SCENARIO = "scenario";

    private String title;
    private final List<Scenario> scenarios = new CopyOnWriteArrayList<Scenario>();

    public DefaultScenarioGroup() {
        this(null);
    }

    public DefaultScenarioGroup(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public List<Scenario> getScenarios() {
    	if (DtVersionDetector.isClassic()) {
    		return Collections.unmodifiableList(getScenarios(InstallationType.Classic));
    	}
		return Collections.unmodifiableList(getScenarios(InstallationType.APM));
    }

    private List<Scenario> getScenarios(InstallationType compatibility) {
    	ArrayList<Scenario> result = new ArrayList<Scenario>(this.scenarios.size());
    	for (Scenario scenario : this.scenarios) {
			if (scenario == null) {
				continue;
			}
			if (compatibility.matches(scenario.getCompatibility())) {
				result.add(scenario);
			}
		}
        return result;
    }

    @Override
    public void addScenario(Scenario scenario) {
        this.scenarios.add(scenario);

        scenario.setGroup(title);
    }

    @Override
    public void addScenario(int index, Scenario scenario) {
        this.scenarios.add(index, scenario);

        scenario.setGroup(title);
    }

    @Override
    public void addScenarios(Collection<Scenario> scenarios) {
        this.scenarios.addAll(scenarios);

        for(Scenario scenario : scenarios) {
	        scenario.setGroup(title);
        }
    }

    @Override
    public void read(ConfigurationNode node, ConfigurationReader reader) throws ConfigurationException {
        title = reader.readStringAttribute(node, ConfigurationXml.ATTRIBUTE_TITLE);
        scenarios.addAll(readScenarios(node, reader));

        for(Scenario scenario : scenarios) {
	        scenario.setGroup(title);
        }
    }

    /**
     * Read the scenarios of a node representing the scenario group.
     *
     * @param scenarioGroup
     * @return a list of scenarios configured within this scenario group that must not be
     *         <code>null</code>
     * @throws ConfigurationException if a problem occur to read the XML nodes
     * @author martin.wurzinger
     */
    private static List<Scenario> readScenarios(ConfigurationNode scenarioGroup, ConfigurationReader reader) throws ConfigurationException {
        List<Scenario> results = new ArrayList<Scenario>();

        for (ConfigurationNode scenarioNode : reader.getChildren(scenarioGroup, DefaultScenarioGroup.NODE_SCENARIO)) {
            Scenario scenario = new DefaultScenario();
            scenario.read(scenarioNode, reader);

            results.add(scenario);
        }

        return results;
    }

    @Override
    public void write(ConfigurationNode node, NodeFactory factory) {
        node.addAttribute(factory.createNode(ConfigurationXml.ATTRIBUTE_TITLE, title));

        for (Scenario scenario : scenarios) {
            ConfigurationNode scenarioNode = factory.createNode(DefaultScenarioGroup.NODE_SCENARIO);

            scenario.write(scenarioNode, factory);

            node.addChild(scenarioNode);
        }
    }
}
