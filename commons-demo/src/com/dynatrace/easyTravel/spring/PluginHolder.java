package com.dynatrace.easytravel.spring;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Holder for all plugins of the runtime componenet (e.g. frontend, backend...)
 * 
 * @author dominik.stadler
 */
public class PluginHolder {

	/**
	 * All plugins, generic and non-generic, enabled and disabled.
	 * 
	 * Note: these are injected by Spring!
	 **/
	private List<Plugin> plugins;

	/**
	 * Returns all plugins that are registered, enabled or not, generic or not.
	 * 
	 * @return
	 */
	public List<Plugin> getPlugins() {
		return plugins;
	}

	public void setPlugins(List<Plugin> plugins) {
		this.plugins = Lists.newArrayList(Iterables.filter(plugins, new Predicate<Plugin>() {

			// filters out the plugins that not be activated (e.g., because one of their dependencies is not fulfilled)
			@Override
			public boolean apply(Plugin input) {
				return input.isActivatable();
			}
			
			//@Override
			public boolean test(Plugin input) {
				return apply(input);
			}
		}));
	}

	public void registerPlugins() {
		SpringUtils.getPluginStateProxy().registerPlugins(new PluginInfoList(plugins).getData());
	}

	public void addPlugin(Plugin plugin) {
		if (!plugin.isActivatable()) {
			return;
		}
		plugins.add(plugin);
		SpringUtils.getPluginStateProxy().registerPlugins(new PluginInfoList(plugin).getData());
	}
}
