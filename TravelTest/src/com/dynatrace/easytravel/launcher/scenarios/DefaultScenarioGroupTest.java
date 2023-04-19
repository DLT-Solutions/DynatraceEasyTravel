/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: DefaultScenarioGroupTest.java
 * @date: 01.07.2011
 * @author: dominik.stadler
 */
package com.dynatrace.easytravel.launcher.scenarios;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.util.DtVersionDetector;


/**
 *
 * @author dominik.stadler
 */
public class DefaultScenarioGroupTest {

	/**
	 * Test method for {@link com.dynatrace.easytravel.launcher.scenarios.DefaultScenarioGroup#DefaultScenarioGroup()}.
	 */
	@Test
	public void testDefaultScenarioGroup() {
		DefaultScenarioGroup group = new DefaultScenarioGroup();
		Collection<Scenario> scenarios = new ArrayList<Scenario>();
		scenarios.add(new DefaultScenario("title1", "desc"));
		scenarios.add(new DefaultScenario("title2", "desc"));
		group.addScenarios(scenarios);

		assertEquals(2, group.getScenarios().size());
		checkScenarios(group.getScenarios(), "title1", "title2");

		group.addScenario(0, new DefaultScenario("title0", "desc"));
		group.addScenario(2, new DefaultScenario("title2a", "desc"));
		checkScenarios(group.getScenarios(), "title0", "title1", "title2a", "title2");

		group.addScenario(new DefaultScenario("title3", "desc"));
		checkScenarios(group.getScenarios(), "title0", "title1", "title2a", "title2", "title3");

		try {
			group.addScenario(0, null);
		} catch (NullPointerException e) {
			// ignore the NPE that is happening here to still have a null-scenario
		}
		checkScenarios(group.getScenarios(), "title0", "title1", "title2a", "title2", "title3");
	}

	private void checkScenarios(List<Scenario> scenarios, String... string) {
		int i = 0;
		for(Scenario scenario : scenarios) {
			assertEquals("Expected to have " + string[i] + " at index " + i + ", but had: " + scenario.getTitle(),
					scenario.getTitle(), string[i]);

			i++;
		}
	}

	@Test
	public void testAPMMode() throws Exception {
		DefaultScenarioGroup group = new DefaultScenarioGroup();
		Collection<Scenario> scenarios = new ArrayList<Scenario>();
		DefaultScenario classicScenario = new DefaultScenario("classic", "classic", true, InstallationType.Classic);
		scenarios.add(classicScenario);
		DefaultScenario apmScenario = new DefaultScenario("apm", "apm", true, InstallationType.APM);
		scenarios.add(apmScenario);
		group.addScenarios(scenarios);

		DtVersionDetector.enforceInstallationType(InstallationType.APM);
		scenarios = group.getScenarios();
		assertEquals(1, scenarios.size());
		assertEquals(apmScenario, scenarios.iterator().next());

		DtVersionDetector.enforceInstallationType(InstallationType.Classic);
		scenarios = group.getScenarios();
		assertEquals(1, scenarios.size());
		assertEquals(classicScenario, scenarios.iterator().next());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testGetScenariosIsUnmodifieable() throws Exception {
		DefaultScenarioGroup group = new DefaultScenarioGroup();
		Collection<Scenario> scenarios = new ArrayList<Scenario>();
		scenarios.add(new DefaultScenario("title1", "desc"));
		scenarios.add(new DefaultScenario("title2", "desc"));
		group.addScenarios(scenarios);

		group.getScenarios().add(new DefaultScenario());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testGetScenariosIsUnmodifieable2() throws Exception {
		DefaultScenarioGroup group = new DefaultScenarioGroup();
		Collection<Scenario> scenarios = new ArrayList<Scenario>();
		scenarios.add(new DefaultScenario("title1", "desc"));
		scenarios.add(new DefaultScenario("title2", "desc"));
		group.addScenarios(scenarios);

		group.getScenarios().remove(0);
	}
}
