package com.dynatrace.easytravel.launcher.plugin.restore;

import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.engine.ScenarioListener;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import ch.qos.logback.classic.Logger;

/**
 * Class for tracking if scenario has been changed
 *
 * cwpl-rorzecho
 */
public class ScenarioStateRestore implements ScenarioListener {
    private static final Logger LOGGER = LoggerFactory.make();

    private static AtomicBoolean isScenarioChanged;

    public ScenarioStateRestore() {
        this(false);
    }

    public ScenarioStateRestore(Boolean restore) {
        isScenarioChanged = new AtomicBoolean(restore);
    }

    @Override
    public void notifyScenarioChanged(Scenario scenario) {
        if (DefaultRestorePoint.isLoaded() && scenario != null) {
            if (!isDefaultScenario(scenario)) {
                isScenarioChanged = new AtomicBoolean(true);
                LOGGER.info(TextUtils.merge("The scenario has been changed to {0}", scenario.getTitle()));
            } else {
                LOGGER.info(TextUtils.merge("The restore point scenario {0} is running", scenario.getTitle()));
                isScenarioChanged = new AtomicBoolean(false);
            }
        }
    }

    public static boolean isScenarioChagned() {
        return isScenarioChanged.get();
    }

    /**
     * For testing only
     * @param changed
     */
    public void setScenarioChanged(boolean isChanged) {
        isScenarioChanged = new AtomicBoolean(isChanged);
    }

    /**
     * Check if the changed scenario is the restore point scenario
     * @param scenario
     * @return
     */
    public boolean isDefaultScenario(Scenario scenario) {
        return scenario.getTitle().equals(getRestorePointScenario().getTitle());
    }

    /**
     * Switch active scenario to restore point scenario
     */
    public static void revertScenario() {
        LaunchEngine.getNewInstance().runAsync(getRestorePointScenario());
    }

    public static Scenario setRestorePointScenario(String restorePointScenario) {
        String[] tokens = restorePointScenario.split(",");
        Scenario scenario = LaunchEngine.findScenario(tokens[0], tokens[1]);
        return scenario;
    }

    private static Scenario getRestorePointScenario() {
        String[] tokens = RestorePointHolder.getInstance().getScenarioRestorePoint().split(",");
        Scenario scenario = LaunchEngine.findScenario(tokens[0], tokens[1]);
        return scenario;
    }
}
