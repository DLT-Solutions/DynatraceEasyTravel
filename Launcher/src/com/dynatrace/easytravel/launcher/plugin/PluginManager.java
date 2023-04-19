package com.dynatrace.easytravel.launcher.plugin;

import java.util.Collection;


/**
 *
 * @author stefan.moschinski
 */
public interface PluginManager {

	/**
	 * Performs the actual enabling/disabling of the previously passed plugins.
	 * You can define which plugins should be enabled/disabled by {@link PluginManager#addPluginsToEnable(Collection)}
	 * and {@link PluginManager#addPluginsToDisable(Collection)}.
	 * @author stefan.moschinski
	 */
	void start();

	/**
	 * Defines which plugins should be enabled when the {@link PluginManager#start()} method is called
	 * @param pluginsToEnable collection with the names of the plugins that should be enabled
	 * @throws IllegalStateException if the method is invoked after the {@link PluginManager#start()} method is already called
	 * @author stefan.moschinski
	 */
	void addPluginsToEnable(Collection<String> pluginsToEnable);

	/**
	 * Defines which plugins should be disabled when the {@link PluginManager#start()} method is called
	 * @param pluginsToDisable collection with the names of the plugins that should be disabled
	 * @throws IllegalStateException if the method is invoked after the {@link PluginManager#start()} method is already called
	 * @author stefan.moschinski
	 */
	void addPluginsToDisable(Collection<String> pluginsToDisable);
}
