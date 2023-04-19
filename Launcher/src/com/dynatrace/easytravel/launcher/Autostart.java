package com.dynatrace.easytravel.launcher;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.fancy.MenuActionCallback;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.util.TextUtils;

/**
 * Simple handler for automatically starting a scenario if configured.
 *
 * @author dominik.stadler
 */
public class Autostart {
    private static final EasyTravelConfig CONFIG = EasyTravelConfig.read();

    private static final Logger LOGGER = Logger.getLogger(Autostart.class.getName());

	private MenuActionCallback controller;
	private static final AtomicBoolean scenarioStarted = new AtomicBoolean(false);

	public void set(Scenario scenario, MenuActionCallback controller) {
		if(scenarioStarted.get()) {
			return;
		}
		// no autostart defined
		// not the scenario that we want to autostart?
		if(CONFIG.autostart == null || !CONFIG.autostart.equals(scenario.getTitle())) {
			return;
		}

		// only check group if specified
		if(CONFIG.autostartGroup != null && !"".equals(CONFIG.autostartGroup) && !CONFIG.autostartGroup.equals(scenario.getGroup())) {
			return;
		}

		this.controller = controller;
	}

	public void execute() {
		if(controller != null && !scenarioStarted.getAndSet(true)) {
			if(CONFIG.autostartGroup != null) {
				LOGGER.info(TextUtils.merge("Trying to autostart scenario ''{0}'' in group ''{1}''", CONFIG.autostart, CONFIG.autostartGroup));
			} else {
				LOGGER.info(TextUtils.merge("Trying to autostart scenario ''{0}''", CONFIG.autostart));
			}
			controller.run();

			// TODO: switch to the menu page that has this group!
		}
	}

	//for tests only
	public void setscenarioStartedValue(boolean newValue) {
		scenarioStarted.set(newValue);
	}
}
