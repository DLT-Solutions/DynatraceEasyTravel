package com.dynatrace.easytravel.spring;

import java.util.LinkedList;
import java.util.List;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;

/**
 * Collection of plugin life cycle methods and algorithm helpers.
 *
 * @author philipp.grasboeck
 */
class PluginLifeCycle {

	private static final Logger log = LoggerFactory.make();

    private static final String PATH_SEPARATOR = ".";
    private static final String PATH_WILDCARD  = ".*";

	static void executeAll(String pluginPoint) {
		executePlugins(new GenericPluginList(pluginPoint).getAllPlugins(), pluginPoint);
	}

	static void enable(Plugin plugin) {
		log.info("Plugin has been enabled: " + plugin.getName());
		executeIfGeneric(plugin, PluginConstants.LIFECYCLE_PLUGIN_ENABLE);
	}

	static void disable(Plugin plugin) {
		log.info("Plugin has been disabled: " + plugin.getName());
		executeIfGeneric(plugin, PluginConstants.LIFECYCLE_PLUGIN_DISABLE);
	}

	private static void executeIfGeneric(Plugin plugin, String location) {
		if (plugin instanceof GenericPlugin) {
			((GenericPlugin) plugin).execute(location);
		}
	}

	/**
	 * Return true if the extesionPoint, that can contain wildcards, matches the pluginPoint.
	 * @param plugin TODO
	 * @param pluginPoint
	 *
	 * @return
	 * @author philipp.grasboeck
	 */
	static boolean pathMatches(GenericPlugin plugin, String pluginPoint, boolean bottomUp) {
		String[] extensionPoints = plugin.getExtensionPoint();
		if (extensionPoints == null) {
			throw new IllegalStateException("No extension point defined for plugin: " + plugin);
		}
		for (String extensionPoint : extensionPoints) {
			if (pluginPoint.equals(extensionPoint)) { // exact match
				return true;
			} else if (extensionPoint.endsWith(PATH_WILDCARD) && pathMatches(extensionPoint.substring(0, extensionPoint.length() - PATH_WILDCARD.length()), pluginPoint)) {
				return true;
			} else if (bottomUp && pathMatches(pluginPoint, extensionPoint)) { // bottomUp for extension point matching
				return true;
			}
		}
		return false;
	}

	// master = "backend"    detail="backend.authenticationservice.getuser"  returns true
	static boolean pathMatches(String master, String detail) {
		return detail.equals(master) ? true : !detail.startsWith(master) ? false : detail.substring(master.length()).startsWith(PATH_SEPARATOR);
	}

	static List<Object> executePlugins(Iterable<GenericPlugin> plugins, String location, Object... context) {
    	// call all plugins and collect results
    	List<Object> returnValues = new LinkedList<Object>();

    	for (GenericPlugin plugin : plugins) {
    		Object result = plugin.execute(location, context);
    		if (result != null) {
    			returnValues.add(result);
    		}
    	}

    	return returnValues;
	}

	static void logPluginList(String type, Object list, Object plugins) {
		if (log.isDebugEnabled()) {
			String message = type + ": " + list + ", plugins=" + plugins;
			System.out.println(message);
			log.debug(message);
		}
	}

	static void logPluginExecute(String location, Object plugin) {
		if (log.isDebugEnabled()) {
			String message  = "Execute plugin at " + location + ": " + plugin;
			System.out.println(message);
			log.debug(message);
		}
	}
}
