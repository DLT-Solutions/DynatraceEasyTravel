package com.dynatrace.easytravel.launcher;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.TestUtil;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.config.ScenarioConfiguration;
import com.dynatrace.easytravel.launcher.engine.BatchStateListener;
import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.fancy.MenuPage;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;



public class ScenarioMenuFactoryTest {
	static {
		TestUtil.setInstallDirCorrection();
    }

	@Before
	public void setUp() {
		// make sure no Batch is running...
		LaunchEngine.stop();

		assertNull("No Batch should be running before starting this test",
				LaunchEngine.getRunningBatch());

		// ensure that Autostart is not set here
		EasyTravelConfig.read().autostart = "";

		ScenarioMenuFactory.getInstance().reset();
	}

	@Test
	public void testScenarioMenuFactory() {
		ScenarioMenuFactory factory = ScenarioMenuFactory.getInstance();
		assertNotNull(factory);

		List<MenuPage> pages = factory.createMenuPages(null, new Autostart()).getMenuPages();
		assertNotNull(pages);
		assertTrue(pages.toString(), pages.size() > 0);

		assertNull(factory.getActiveMenuItem());
	}

	@Test
	public void testScenarioMenuFactoryWithRunningBatch() {
        ScenarioConfiguration scenarioConfig = new ScenarioConfiguration();
        scenarioConfig.loadOrCreate();

        LaunchEngine.getNewInstance().run(scenarioConfig.getScenarioGroups().get(0).getScenarios().get(0));

		assertNotNull(LaunchEngine.getRunningBatch());
		assertNotNull(LaunchEngine.getRunningBatch().getScenario());

		ScenarioMenuFactory factory = ScenarioMenuFactory.getInstance();
		assertNotNull(factory);

		List<MenuPage> pages = factory.createMenuPages(null, new Autostart()).getMenuPages();
		assertNotNull(pages);
		assertTrue(pages.toString(), pages.size() > 0);
		assertTrue(pages.get(0).checkVisible());
		assertTrue(pages.get(0).checkEnabled());

		assertNotNull(factory.getActiveMenuItem());
	}

	@Test
	public void testWithBatchStateListener() {
		ScenarioMenuFactory factory = ScenarioMenuFactory.getInstance();
		assertNotNull(factory);

		List<MenuPage> pages = factory.createMenuPages(null, new Autostart(), new BatchStateListener() {
			@Override
			public void notifyBatchStateChanged(Scenario scenario, State oldState, State newState) {
			}
		}).getMenuPages();
		assertNotNull(pages);
		assertTrue(pages.toString(), pages.size() > 0);

		assertNull(factory.getActiveMenuItem());
	}
}
