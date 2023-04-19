package com.dynatrace.easytravel.launcher.plugin;

import com.dynatrace.easytravel.spring.PluginInfoList;


/**
 * Listener for plugin enabled state.
 *
 * @author dominik.stadler
 */
public interface PluginEnabledListener {

    /**
     * <p>
     * Notifies implementors about the current list of enabled plugins
     * </p>
     * <p>
     * Please note: This is not only called upon changes, but also on a periodic basis!
     * </p>
     */
    void notifyEnabledPlugins(PluginInfoList enabledPlugins);
}
