package com.dynatrace.easytravel.launcher.plugin.restore;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import java.util.Arrays;

import ch.qos.logback.classic.Logger;

/**
 * Singleton class holds bootPlugins and scenario specific plugins
 * enabled in scenarios.xml or userScenarios.xml files and name
 * of the first runned scenario
 *
 * cwpl-rorzecho
 */

public class RestorePointHolder {
    private static final Logger LOGGER = LoggerFactory.make();

    private static RestorePointHolder INSTANCE;

    private String[] pluginRestorePoint;
    private String scenarioRestorePoint;

    private RestorePointHolder(String[] pluginRestorePoint, String scenarioRestorePoint ) {
        this.pluginRestorePoint = new String[pluginRestorePoint.length];
        this.pluginRestorePoint = pluginRestorePoint;
        this.scenarioRestorePoint = scenarioRestorePoint;

        LOGGER.info(TextUtils.merge("Default restore point is initialized: bootPlugins: {0} defaultScenario: {1}", Arrays.toString(pluginRestorePoint), scenarioRestorePoint));
    }

    public static RestorePointHolder getInstance() {
        return INSTANCE;
    }

    public static void init(String[] pluginRestorePoint, String scenarioResotrePoint) {
        if (INSTANCE == null) {
            INSTANCE = new RestorePointHolder(pluginRestorePoint, scenarioResotrePoint);
        }
    }

    public String[] getPluginRestorePoint() {
        return pluginRestorePoint;
    }

    public String getScenarioRestorePoint() {
        return scenarioRestorePoint;
    }

}
