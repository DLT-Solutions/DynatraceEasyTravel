package com.dynatrace.easytravel.launcher.plugin.restore;

import com.dynatrace.easytravel.launcher.engine.BatchStateListener;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.plugin.RemotePluginController;
import com.dynatrace.easytravel.util.TextUtils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.qos.logback.classic.Logger;

/**
 * Class for initializing {@link RestorePointHolder}
 *
 * cwpl-rorzecho
 */
public class DefaultRestorePoint implements BatchStateListener {
    private static final Logger LOGGER = LoggerFactory.make();

    private final RemotePluginController controller;

    private static AtomicBoolean isLoaded = new AtomicBoolean(false);

    public DefaultRestorePoint() {
        this(new RemotePluginController());
    }

    public DefaultRestorePoint(RemotePluginController controller) {
        this.controller = controller;
    }

    @Override
    public void notifyBatchStateChanged(Scenario scenario, State oldState, State newState) {
        if (State.STARTING.equals(oldState) && State.OPERATING.equals(newState)) {
            String[] enabledPluginNames = null;
            try {
                enabledPluginNames = controller.requestEnabledPluginNames();
            } catch (IOException e) {
                LOGGER.error(TextUtils.merge("Cannot get enabled plugins for scenario: {0}", scenario.getTitle()), e);
            }

            RestorePointHolder.init(enabledPluginNames, createScenarioToken(scenario.getGroup(), scenario.getTitle()));

            isLoaded = new AtomicBoolean(true);
        }
    }

    public static String createScenarioToken(String group, String name) {
        StringBuilder builder = new StringBuilder();
        return builder.append(group).append(",").append(name).toString();
    }

    public static boolean isLoaded() {
        return isLoaded.get();
    }
}
