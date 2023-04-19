package com.dynatrace.easytravel.launcher.scenarios.builder;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.dynatrace.easytravel.config.InstallationType;
import com.dynatrace.easytravel.launcher.scenarios.DefaultProcedureMapping;
import com.dynatrace.easytravel.launcher.scenarios.DefaultScenarioGroup;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;


public class ScenarioBuilderTest {

	@Test
	public void testScenarioBuilder() {
		ScenarioBuilder scenario = ScenarioBuilder.scenario("myscen", "blabla");
		assertNotNull(scenario.set());
		assertNotNull(scenario.set(new SettingBuilder[] {}));

		assertNotNull(scenario.addIf(true, new DefaultProcedureMapping("map1")));
		assertNotNull(scenario.addIf(false, new DefaultProcedureMapping("map2")));
		assertNotNull(scenario.enable());

		DefaultScenarioGroup group = new DefaultScenarioGroup("mygroup");
		scenario.addToGroup(group);
		List<Scenario> scenarios = group.getScenarios();
		assertEquals(1, scenarios.size());
		Scenario result = scenarios.get(0);
		assertEquals("myscen", result.getTitle());

		assertEquals(1, result.getProcedureMappings(InstallationType.Classic).size());
		assertEquals("map1", result.getProcedureMappings(InstallationType.Classic).get(0).getId());
		assertTrue(result.isEnabled());
	}
}
