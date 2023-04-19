package com.dynatrace.easytravel.spring;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * Manages a list of plugins and their state.
 *
 * Instances of this class can be created at any locations where plugins are needed.
 * It handles the correct enablement state and the life cycle automatically.
 *
 * One PluginRefreshThread is kept for refreshing the plugin list periodically.
 *
 * Keeping instances somewhere is only helping performance (enabled-check only within intervals),
 * but not for correctness.
 *
 * @author philipp.grasboeck
 *
 * @param <T>
 */
public class PluginList<T extends Plugin> implements Iterable<T> {
	private static final Logger log = LoggerFactory.make();
	private static volatile PluginRefreshThread refreshThread;

	//used in tests only; if true method fetchAllPlugins will always try to refresh list of plugins
	@TestOnly
	private volatile boolean refreshAllPlugins = false;

	private volatile String[] lastEnabledPluginsArr;

	/**
	 * The class of plugins that this PluginList holds subclasses of
	 */
	private final Class<T> pluginClass;

	/**
	 * All plugins that this plugin list is interested in.
	 */
	private List<T> allPlugins;

	private volatile List<T> lastEnabledPlugins;

	public PluginList(Class<T> pluginClass) {
		if (pluginClass == null) {
			throw new IllegalArgumentException("pluginClass must not be null");
		}
		this.pluginClass = pluginClass;
	}

	/**
	 * Convenience method for getting all plugins, including disabled plugins.
	 * This does not ask the plugin state manager for enabled state.
	 */
	protected Iterable<T> getAllPlugins() {
		return fetchAllPlugins();
	}

	/**
	 * Convenience method for getting the enabled plugins,
	 * this refreshes the state.
	 */
	protected Iterable<T> getEnabledPlugins() {
		return refreshEnabledPlugins();
	}

	/**
	 * Get the enabled plugins of this PluginList.
	 */
	@Override
	public Iterator<T> iterator() {
		return getEnabledPlugins().iterator();
	}

	/**
	 * Returns the total number of plugins contained in this list, including disabled plugins.
	 *
	 * @return
	 * @author philipp.grasboeck
	 */
	public int size() {
		return fetchAllPlugins().size();
	}

	@SuppressWarnings("unchecked")
	private List<T> fetchAllPlugins() {
		List<T> allPlugins = this.allPlugins;
		if (allPlugins == null || refreshAllPlugins) {
			refreshAllPlugins = false;
			List<T> newAllPlugins = new LinkedList<T>();

			for (Plugin p : SpringUtils.getPluginHolder().getPlugins()) {
				T plugin = (T) p;
				if (interested(plugin)) {
					newAllPlugins.add(plugin);
				}
			}

			this.allPlugins = allPlugins = newAllPlugins;
			PluginLifeCycle.logPluginList("Fetched", this, allPlugins);
		}
		return allPlugins;
	}

	/**
	 * Return true if this plugin list is interested in keeping the
	 * denoted plugin.
	 *
	 * @param plugin
	 * @return
	 * @author philipp.grasboeck
	 */
	protected boolean interested(Plugin plugin) {
		return pluginClass.isAssignableFrom(plugin.getClass()); // i.e. instanceof
	}

	/**
	 * Ensure that the refresh thread is not running any more.
	 *
	 * @author cwat-dstadler
	 */
	public static void stopRefreshThread() {
		if(refreshThread != null) {
			// double locking to avoid synchronized access but still prevent NPE when double-shutdown of the thread
			synchronized (PluginList.class) {
				if(refreshThread != null) {
					refreshThread.shouldStop();

					refreshThread = null;
				}
			}
		}
	}

	/**
	 * Refreshes the list of enabled plugins by querying the PluginCheckProxy,
	 * if the at last REFRESH_INTERVAL milliseconds have past since the last check.
	 */

	private List<T> refreshEnabledPlugins() {
		// first ensure that the refreshing thread is started
		ensureRefreshThreadIsRunning();

		List<T> enabledPlugins;
		String[] enabledPluginsArr = refreshThread.getEnabledPlugins();
		if( lastEnabledPlugins != null && enabledPluginsArr == lastEnabledPluginsArr ) {
			log.debug("Plugin list didn't changed. Return last value");
			enabledPlugins = lastEnabledPlugins;
		} else {
			lastEnabledPluginsArr = enabledPluginsArr;
			log.debug("Plugin list changed. Sync plugin states");

			List<T> pluginsEnabled = new LinkedList<T>();
			List<T> pluginsDisabled = new LinkedList<T>();
			enabledPlugins = syncPluginStates(pluginsEnabled, pluginsDisabled);
			invokeLifecyclePluginPoints(pluginsEnabled, pluginsDisabled);
		}

		return enabledPlugins;
	}

	private void ensureRefreshThreadIsRunning() {
		if(refreshThread == null) {
			// double locking to avoid synchronized access but still prevent double creation of the thread
			synchronized (PluginList.class) {
				if(refreshThread == null) {
					refreshThread = new PluginRefreshThread();
					refreshThread.start();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<T> syncPluginStates(List<T> pluginsEnabled, List<T> pluginsDisabled) {
		// invoke the WebService getEnabledPluginNames() BEFORE the sync
		// sync so that parallel plugin lists don't get mangled
		// sync statically because one plugin can be contained in many plugin lists
		PluginInfoList receivedEnabledPluginList = new PluginInfoList(refreshThread.getEnabledPlugins());

		log.debug("Scanning plugins");

		List<T> allPluginsLocal = fetchAllPlugins();
		List<T> newEnabledPlugins = new LinkedList<T>();
		synchronized (getClass()) {
			for (Plugin p : allPluginsLocal) {
				T plugin = (T) p;
				// TODO: should we do the enable/disable notification in the Refresh Thread?
				// This would avoid potential multiple notifications if there are multiple PluginList
				if (receivedEnabledPluginList.contains(plugin)) {
					newEnabledPlugins.add(plugin);
					if (!plugin.isEnabled()) {
						plugin.setEnabled(true);
						pluginsEnabled.add(plugin);
					}
				} else if (plugin.isEnabled()) {
					plugin.setEnabled(false);
					pluginsDisabled.add(plugin);
				}
			}
			PluginLifeCycle.logPluginList("Enabled", this, newEnabledPlugins);
		}

		lastEnabledPlugins = newEnabledPlugins;
		return newEnabledPlugins;
	}

	private void invokeLifecyclePluginPoints(List<T> pluginsEnabled, List<T> pluginsDisabled) {
		// invoke the LIFECYCLE plugin points AFTER sync
		for (Plugin plugin : pluginsEnabled) {
			PluginLifeCycle.enable(plugin);
		}
		for (Plugin plugin : pluginsDisabled) {
			PluginLifeCycle.disable(plugin);
		}
	}

	@Override
	public String toString() {
		return "PluginList [pluginClass=" + pluginClass + "]";
	}

	/**
	 * for tests purposes. See refreshAllPlugins description.
	 * @param refreshAllPlugins
	 */
	@TestOnly
	public void setRefreshAllPlugins(boolean refreshAllPlugins) {
		this.refreshAllPlugins = refreshAllPlugins;
	}
}
