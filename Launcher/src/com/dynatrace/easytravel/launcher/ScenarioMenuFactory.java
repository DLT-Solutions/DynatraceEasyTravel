package com.dynatrace.easytravel.launcher;

import java.util.ArrayList;
import java.util.List;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.config.ScenarioConfiguration;
import com.dynatrace.easytravel.launcher.engine.BatchStateListener;
import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.engine.ProcedureStateListener;
import com.dynatrace.easytravel.launcher.fancy.MenuPage;
import com.dynatrace.easytravel.launcher.fancy.MenuVisibilityCallback;
import com.dynatrace.easytravel.launcher.fancy.MenuVisibilityCallbackAdapter;
import com.dynatrace.easytravel.launcher.fancy.custom.ScenarioMenuItem;
import com.dynatrace.easytravel.launcher.fancy.custom.ScenarioMenuPage;
import com.dynatrace.easytravel.launcher.misc.MessageConstants;
import com.dynatrace.easytravel.launcher.plugin.PluginStateListener;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.launcher.scenarios.ScenarioGroup;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class ScenarioMenuFactory {
	private static final Logger LOGGER = LoggerFactory.make();

	private ScenarioMenuFactory() {
	}

	private static final ScenarioMenuFactory INSTANCE = new ScenarioMenuFactory();

	public static ScenarioMenuFactory getInstance() {
		return INSTANCE;
	}

	private ScenarioMenuItem activeMenuItem;

	List<MenuPage> result = new ArrayList<MenuPage>();

    public MenuPagesAndListeners createMenuPages(final ProcedureStateListener listener, Autostart autostart, final BatchStateListener... batchListeners) {
    	INSTANCE.result.clear();
		INSTANCE.reset();
		List<ScenarioConfiguration> resultListeners = new ArrayList<ScenarioConfiguration>();

        ScenarioConfiguration scenarioConfig = new ScenarioConfiguration();
        ScenarioConfiguration defultScenarioConfig =  scenarioConfig.loadOrCreate();

        if (defultScenarioConfig != null) {
        	resultListeners.add(defultScenarioConfig);
        }
        resultListeners.add(scenarioConfig);

        Scenario runningScenario = LaunchEngine.getRunningBatch() != null ? LaunchEngine.getRunningBatch().getScenario() : null;

        EasyTravelConfig config = EasyTravelConfig.read();
        for (ScenarioGroup group : scenarioConfig.getScenarioGroups()) {
            List<Scenario> scenarios = group.getScenarios();
        	if (!scenarios.isEmpty() && (!group.getTitle().equals(MessageConstants.SCENARIO_GROUP_MAINFRAME_TITLE) || config.enableMainframeDemo)) {
	            ScenarioMenuPage groupPage = new ScenarioMenuPage(group.getTitle(), null, MenuVisibilityCallbackAdapter.VISIBLE_AND_ENABLED);

	            for (Scenario scenario : scenarios) {
	            	ScenarioController controller = new ScenarioController(scenario);
					controller.addProcedureStateListener(listener);
	                ScenarioMenuItem menuItem = new ScenarioMenuItem(null, scenario.getTitle(), scenario.getDescription(), controller, new ScenarioEnablement(scenario));
					groupPage.addItem(menuItem);
					// potentially automatically start this later if configured
			        autostart.set(scenario, controller);

			        if (runningScenario != null && runningScenario.equals(scenario)) {
		        		activeMenuItem = menuItem;
			        }

			        if(batchListeners != null) {
				        for(BatchStateListener batchListener : batchListeners) {
				        	controller.addBatchStateListener(batchListener);
				        }
			        }
	            }

	            result.add(groupPage);
        	}
        }

        // add a page for plugins which states that no plugin information is available yet
        // this will be replaced by the actual list of plugins as soon as the backend is available
        result.add(PluginStateListener.createNoPluginsPage());

        return new MenuPagesAndListeners(result, resultListeners);
    }

	private static final class ScenarioEnablement implements MenuVisibilityCallback {

        private final Scenario scenario;

        public ScenarioEnablement(Scenario scenario) {
            this.scenario = scenario;
        }

        @Override
        public boolean isEnabled() {
            return scenario.isEnabled();
        }

        @Override
        public boolean isVisible() {
            return true;
        }
    }

	/**
	 * Gets the ScenarioMenuItem of the currently running scenario.
	 *
	 * @return
	 * @author philipp.grasboeck
	 */
	public ScenarioMenuItem getActiveMenuItem() {
		return activeMenuItem;
	}

	/**
	 * Only used for testing to reset the active menu item and other internal state
	 */
	@TestOnly
	public void reset() {
		activeMenuItem = null;
	}
}
