package com.dynatrace.easytravel.launcher.fancy.custom;

import static org.junit.Assert.assertNotNull;

import org.easymock.EasyMock;
import org.junit.Test;

import com.dynatrace.easytravel.launcher.ScenarioController;
import com.dynatrace.easytravel.launcher.engine.BatchStateListener;
import com.dynatrace.easytravel.launcher.engine.ProcedureStateListener;
import com.dynatrace.easytravel.launcher.fancy.MenuVisibilityCallback;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;


public class ScenarioMenuItemTest {

	@Test
	public void testCreateComponent() {
		Scenario scenario = EasyMock.createMock(Scenario.class);
		ScenarioController scenarioController = new ScenarioController(scenario);
		MenuVisibilityCallback visibility = EasyMock.createMock(MenuVisibilityCallback.class);

		EasyMock.replay(scenario, visibility);
		ScenarioMenuItem item = new ScenarioMenuItem(Constants.Images.FANCY_MENU_BUTTON_BG_SELECTED_HOVER,
				"Title", "Description", scenarioController, visibility);
		assertNotNull(item);

		// cannot be tested without SWT: item.createComponent(parent, page, layoutCallback)

		EasyMock.verify(scenario, visibility);
	}

	@Test
	public void testAddBatchStateListener() {
		Scenario scenario = EasyMock.createMock(Scenario.class);
		ScenarioController scenarioController = new ScenarioController(scenario);
		MenuVisibilityCallback visibility = EasyMock.createMock(MenuVisibilityCallback.class);
		BatchStateListener listener = EasyMock.createMock(BatchStateListener.class);

		EasyMock.replay(scenario, visibility, listener);
		ScenarioMenuItem item = new ScenarioMenuItem(Constants.Images.FANCY_MENU_BUTTON_BG_SELECTED_HOVER,
				"Title", "Description", scenarioController, visibility);

		item.addBatchStateListener(listener);

		EasyMock.verify(scenario, visibility, listener);
	}

	@Test
	public void testAddProcedureStateListener() {
		Scenario scenario = EasyMock.createMock(Scenario.class);
		ScenarioController scenarioController = new ScenarioController(scenario);
		MenuVisibilityCallback visibility = EasyMock.createMock(MenuVisibilityCallback.class);
		ProcedureStateListener listener = EasyMock.createMock(ProcedureStateListener.class);

		EasyMock.replay(scenario, visibility, listener);
		ScenarioMenuItem item = new ScenarioMenuItem(Constants.Images.FANCY_MENU_BUTTON_BG_SELECTED_HOVER,
				"Title", "Description", scenarioController, visibility);

		item.addProcedureStateListener(listener);

		EasyMock.verify(scenario, visibility, listener);
	}

	@Test
	public void testStopScenario() {

		Scenario scenario = EasyMock.createMock(Scenario.class);
		ScenarioController scenarioController = new ScenarioController(scenario);
		EasyMock.expect(scenario.getTitle()).andReturn("title").atLeastOnce();
		MenuVisibilityCallback visibility = EasyMock.createMock(MenuVisibilityCallback.class);

		ProcedureStateListener listener = EasyMock.createMock(ProcedureStateListener.class);
		BatchStateListener blistener = EasyMock.createMock(BatchStateListener.class);

		EasyMock.replay(scenario, visibility, listener, blistener);
		ScenarioMenuItem item = new ScenarioMenuItem(Constants.Images.FANCY_MENU_BUTTON_BG_SELECTED_HOVER,
				"Title", "Description", scenarioController, visibility);

		item.addProcedureStateListener(listener);
		item.addBatchStateListener(blistener);

		item.stopScenario();

		EasyMock.verify(scenario, visibility, listener, blistener);
	}

	@Test
	public void testRestartScenario() {

		Scenario scenario = EasyMock.createMock(Scenario.class);
		ScenarioController scenarioController = new ScenarioController(scenario);
		EasyMock.expect(scenario.getTitle()).andReturn("title").atLeastOnce();
		MenuVisibilityCallback visibility = EasyMock.createMock(MenuVisibilityCallback.class);

		ProcedureStateListener listener = EasyMock.createMock(ProcedureStateListener.class);
		BatchStateListener blistener = EasyMock.createMock(BatchStateListener.class);

		EasyMock.replay(scenario, visibility, listener, blistener);
		ScenarioMenuItem item = new ScenarioMenuItem(Constants.Images.FANCY_MENU_BUTTON_BG_SELECTED_HOVER,
				"Title", "Description", scenarioController, visibility);

		item.addProcedureStateListener(listener);
		item.addBatchStateListener(blistener);

		item.restartScenario();

		EasyMock.verify(scenario, visibility, listener, blistener);
	}

}
