package com.dynatrace.easytravel.spring;

import java.util.Timer;
import java.util.TimerTask;

import com.dynatrace.easytravel.annotation.TestOnly;


/**
 * A list of generic plugins for a specific pluginPoint.
 * One GenericPluginList contains all plugins that are interested in this pluginPoint.
 * This class also provides methods to execute the enabled plugins.
 *
 * @author philipp.grasboeck
 */
public class GenericPluginList extends PluginList<GenericPlugin> {

	private volatile static Timer timer = createPeriodicPluginTimer();

	private static Timer createPeriodicPluginTimer() {
		final GenericPluginList list = new GenericPluginList(PluginConstants.PERIODIC_EXECUTE);
		Timer timer = new Timer("Periodic Plugin Execution Timer", true);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					list.execute(PluginConstants.PERIODIC_EXECUTE, (Object[]) null);
				} catch (IllegalStateException e) {
					// ignore
				}
			}

		}, 10000, 10000);
		return timer;
	}

	private final String pluginPoint;

	public GenericPluginList(String pluginPoint) {
		super(GenericPlugin.class);
		if (pluginPoint == null) {
			throw new IllegalArgumentException("pluginPoint must not be null");
		}
		this.pluginPoint = pluginPoint;
	}

	@Override
	protected boolean interested(Plugin plugin) {
		if (!super.interested(plugin)) {
			return false; // it's not a GenericPlugin
		}

		// check if the plugin is registered for this extension point
		return PluginLifeCycle.pathMatches((GenericPlugin) plugin, pluginPoint, true);
	}

	/**
	 * Executes all enabled plugins with the given location.
	 *
	 * @param location  Location passed to the plugins. Note: This must be equal to or a subpath of this
	 * GenericPluginList's pluginPoint.
	 * @param context
	 * @return
	 * @author philipp.grasboeck
	 */
	public Iterable<Object> execute(String location, Object... context) {
		if (!PluginLifeCycle.pathMatches(pluginPoint, location)) {
			throw new IllegalStateException("Plugin location '" + location + "' is not a subpath of '" + pluginPoint + "'");
		}
		return PluginLifeCycle.executePlugins(getEnabledPlugins(), location, context);
	}

	@Override
	public String toString() {
		return "GenericPluginList [pluginPoint=" + pluginPoint + "]";
	}

	/**
	 * Helper method used in tests to ensure a clean state
	 *
	 * @author cwat-dstadler
	 */
	@TestOnly
	public static void cancelTimer() {
		// use local variable to be thread safe
		Timer lTimer = timer;
		if(lTimer != null) {
			lTimer.cancel();
			timer = null;
		}
	}
}
