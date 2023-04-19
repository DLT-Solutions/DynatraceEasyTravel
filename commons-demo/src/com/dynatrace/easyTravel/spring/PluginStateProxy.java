package com.dynatrace.easytravel.spring;

/**
 * Defines the proxy methods needed for PluginStateManager to work correctly.
 * On back end, typically the PluginStateManager itself will be used.
 * On front end, there must be some remote call to the PluginStateManger to get the shared state.
 *
 * All String[] array returned and accepted by methods in this interface can be wrapped
 * into a PluginInfoList for convenience.
 *
 * @author philipp.grasboeck
 */
public interface PluginStateProxy
{
	/**
	 * Retrieve the names of all registered plugins.
	 *
	 * @return
	 * @author philipp.grasboeck
	 */
	public String[] getAllPluginNames();

	/**
	 * Retrieve the names of all enabled plugins.
	 *
	 * @return
	 * @author philipp.grasboeck
	 */
	public String[] getEnabledPluginNames();

	/**
	 * Retrieve the names of all enabled plugins for a given host.
	 *
	 * @param host the host to query the enabled plugins for
	 * @return the names of the plugins enabled for the given host
	 *
	 * @author cwat-rpilz
	 */
	public String[] getEnabledPluginNamesForHost(String host);

	/**
	 * Get all registered plugins with groupName and description.
	 *
	 * @return
	 * @author philipp.grasboeck
	 */
	public String[] getAllPlugins();

	/**
	 * Get all enabled plugins with groupName and description.
	 *
	 * @return
	 * @author philipp.grasboeck
	 */
	public String[] getEnabledPlugins();

	/**
	 * Get all enabled plugins with groupName and description
	 * for a given host.
	 *
	 * @param host the host to query the enabled plugins for
	 * @return the enabled plugin names, group names, and descriptions
	 *
	 * @author cwat-rpilz
	 */
	public String[] getEnabledPluginsForHost(String host);

	/**
	 * Register a list of plugins.
	 * If desired, groupName and description can be defined here, too.
	 * Just use the form the PluginInfoList suggests
	 * (strings separated by ':').
	 *
	 * @param pluginData
	 * @author philipp.grasboeck
	 */
	public void registerPlugins(String[] pluginData);

	/**
	 * Set a plugin's enabled state.
	 * Note that this has no effect if there is no registered
	 * plugin with the given name.
	 *
	 * @param name
	 * @param enabled
	 * @author philipp.grasboeck
	 */
	public void setPluginEnabled(String name, boolean enabled);

	/**
	 * Defines the host this plugin is explicitly enable for
	 *
	 * @param name the name of the plugin
	 * @param hosts the hosts to enable the plugin for or <tt>null</tt>
	 * 				in order to enable it for all hosts
	 *
	 * @author cwat-rpilz
	 */
	void setPluginHosts(String name, String[] hosts);

	/**
	 * Sets the template configuration
	 * @param configuration The new template configuration to set
	 *
	 * @author tomasz.wieremjewicz
	 */
	public void setPluginTemplateConfiguration(String configuration);

}
