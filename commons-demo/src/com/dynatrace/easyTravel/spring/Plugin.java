package com.dynatrace.easytravel.spring;

/**
 * Root interface for all plugins.
 * 
 * @author philipp.grasboeck
 * 
 */
public interface Plugin {

	/**
	 * The unique name of the plugin. This is shown to the user of the demo applicatoin.
	 **/
	public String getName();

    /**
     * The installation type name which defines plugin applicable
     *
     * Installation type names:
     * - dynaTraceClassic
     * - APM
     */
    public String getCompatibility();

    public void setCompatibility(String compatibility);

	/** The group name of the plugin. */
	public String getGroupName();

	/**
	 * The description of the plugin. This is shown to the user of the demo application.
	 **/
	public String getDescription();

	/**
	 * Return if this plugin is enabled.
	 * 
	 * @return
	 * @author philipp.grasboeck
	 */
	public boolean isEnabled();

	/**
	 * Set this plugin's enabled state.
	 * This is invoked in PluginList, don't try to enable plugins here.
	 * 
	 * @param enabled
	 * @author philipp.grasboeck
	 */
	public void setEnabled(boolean enabled);

	/**
	 * 
	 * @return all {@link PluginDependency}s of the plugin, if the plugin has none then the method
	 *         returns an empty {@link Iterable}, but <b>never</b> <code>null</code>
	 * @author stefan.moschinski
	 */
	Iterable<PluginDependency> getPluginDependencies();

	/**
	 * 
	 * @return all dependencies of the plugin as string array, if the plugin has no dependency the method returns
	 *         an empty array
	 * @author stefan.moschinski
	 */
	String[] getDependencies();

	/**
	 * 
	 * @return <code>true</code> if the the plugin can be activated (e.g., if all dependencies are fulfilled)
	 * @author stefan.moschinski
	 */
	boolean isActivatable();
	
	/**
	 * Defines the hosts this plugin is explicitly enabled for
	 * 
	 * @param host an array of host names of <tt>null</tt> if this
	 * 				plugin is enabled for any host
	 */
	void setHosts(String[] hosts);

	/**
	 * @return an array of hosts for which this plugin has
	 * 			been explicitly enabled for or <tt>null</tt>
	 * 			if this plugin is enabled on any host
	 * 
	 * @author cwat-rpilz
	 */
	String[] getHosts();
	
	/**
	 * Checks if the plugin is enabled for the given host
	 * 
	 * @param host the host for which to check if the
	 * 				plugin is enabled for it 
	 * @return <tt>true</tt> if the plugin is enabled for the
	 * 			given host, <tt>false</tt> otherwise.
	 */
	boolean isEnabledFor(String host);

}
