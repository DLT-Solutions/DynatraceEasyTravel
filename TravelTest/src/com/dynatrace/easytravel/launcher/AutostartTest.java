package com.dynatrace.easytravel.launcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.fancy.MenuActionCallback;
import com.dynatrace.easytravel.launcher.scenarios.DefaultScenario;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;


public class AutostartTest {
	boolean called = false;

	@Before
	public void setUp() {
		// ensure that Autostart is not set here
		EasyTravelConfig.read().autostart = "";
		EasyTravelConfig.read().autostartGroup = "";
	}

	MenuActionCallback controller = new MenuActionCallback() {

		@Override
		public void stop() {
			throw new UnsupportedOperationException("Should not get here!");
		}

		@Override
		public void setText(String text) {
			throw new UnsupportedOperationException("Should not get here!");
		}

		@Override
		public void run() {
			called = true;
		}

		@Override
		public String getText() {
			throw new UnsupportedOperationException("Should not get here!");
		}

		@Override
		public Boolean isEnabled() {
			return null;
		}
	};

	@Test
	public void testSetScenarioOnly() {
		EasyTravelConfig CONFIG = EasyTravelConfig.read();

		Autostart start = new Autostart();
		start.setscenarioStartedValue(false);
		Scenario scenario = new DefaultScenario("test1", "somedesc");
		start.set(scenario, controller);

		called = false;
		start.execute();
		assertFalse("Should not match initially as autostart is empty", called);

		CONFIG.autostart = "test1";

		start = new Autostart();
		start.setscenarioStartedValue(false);
		start.set(scenario, controller);

		called = false;
		start.execute();
		assertTrue("Should be called when title matches", called);


		CONFIG.autostart = "test2";

		start = new Autostart();
		start.setscenarioStartedValue(false);
		start.set(scenario, controller);

		called = false;
		start.execute();
		assertFalse("Should not be called as title is different", called);


		CONFIG.autostart = null;

		start = new Autostart();
		start.setscenarioStartedValue(false);
		start.set(scenario, controller);

		called = false;
		start.execute();
		assertFalse("Should not be called as title is different", called);
	}

	@Test
	public void testSetWithGroup() {
		EasyTravelConfig CONFIG = EasyTravelConfig.read();

		CONFIG.autostart = "test1";
		CONFIG.autostartGroup = null;

		Autostart start = new Autostart();
		start.setscenarioStartedValue(false);
		Scenario scenario = new DefaultScenario("test1", "somedesc");
		scenario.setGroup("group1");
		start.set(scenario, controller);

		called = false;
		start.execute();
		assertTrue("Should be called when title matches and group is null", called);


		CONFIG.autostart = "test1";
		CONFIG.autostartGroup = "";

		start = new Autostart();
		start.setscenarioStartedValue(false);
		start.set(scenario, controller);

		called = false;
		start.execute();
		assertTrue("Should be called when title matches and group is empty", called);


		CONFIG.autostart = "test1";
		CONFIG.autostartGroup = "group1";

		start = new Autostart();
		start.setscenarioStartedValue(false);
		start.set(scenario, controller);

		called = false;
		start.execute();
		assertTrue("Should be called when title and group match", called);


		CONFIG.autostart = "test1";
		CONFIG.autostartGroup = "group2";

		start = new Autostart();
		start.setscenarioStartedValue(false);
		start.set(scenario, controller);

		called = false;
		start.execute();
		assertFalse("Should not be called when title matches and group does not match", called);
	}
}
