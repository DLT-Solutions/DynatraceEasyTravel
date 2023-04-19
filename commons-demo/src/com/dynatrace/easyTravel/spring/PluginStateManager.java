package com.dynatrace.easytravel.spring;

import java.util.Arrays;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.google.common.base.Strings;

import ch.qos.logback.classic.Logger;

/**
 * Manages enabled plugins by their names. It holds a list of available plugins and a
 * separate list of enabled plugins to be able to answer questions for both all
 * available plugins as well as only enabled plugins.
 *
 *  This is the {@link PluginStateProxy} implementation which actually keeps the
 *  information locally, all others only forward the calls in one way or the other.
 *
 * This class is meant as app-wide singleton.
 *
 * @author philipp.grasboeck
 */
public class PluginStateManager implements PluginStateProxy
{
    private static final Logger log = LoggerFactory.make();

    private final PluginInfoList allPlugins;
    private final PluginInfoList enabledPlugins;
    private final PluginEventNotifier pluginEventNotifier;

    public PluginStateManager() {
    	EasyTravelConfig config = EasyTravelConfig.read();

        allPlugins = new PluginInfoList();
        enabledPlugins = new PluginInfoList();
        pluginEventNotifier = new PluginEventNotifier();

    	allPlugins.addData(config.bootPlugins);
    	enabledPlugins.addData(config.bootPlugins);

    	log.info("bootPlugins: " + allPlugins);
    }

    public void initializeTemplates(String pluginTemplateConfiguration) {
    	if (!Strings.isNullOrEmpty(pluginTemplateConfiguration)) {
    		log.info("Initializing event notifications from template.");
    		pluginEventNotifier.initializeTemplatesFromJson(pluginTemplateConfiguration);
    		log.info("Initializing event notifications from template finished.");
    	}
    	else {
    		log.info("There is no notification template to load - not initializing event notifications");
    	}
    }

    @Override
    public void setPluginHosts(String name, String[] hosts) {
		Plugin plugin = allPlugins.get(name);
    	if (plugin == null) {
			log.warn("tried to change the enabled state by host for unregistered plugin: " + name);
    		return;
    	}

    	log.info("set plugin hosts: " + Arrays.toString(hosts) + " for plugin " + name);
    	plugin.setHosts(hosts);

    	plugin = enabledPlugins.get(name);
    	if (plugin != null) {
    		plugin.setHosts(hosts);
    	}
    }

	@Override
	public void setPluginEnabled(String name, boolean enabled) {
		Plugin plugin = allPlugins.get(name);
    	if (plugin == null) {
    		log.warn("tried to enable unregistered plugin: " + name);
    		return;
    	}
    	if (enabled) {
    		if (!enabledPlugins.contains(plugin)) {
    			pluginEventNotifier.sendPluginStateChangeEvent(new PluginChangeInfo(plugin.getName(), enabled));
    		}

        	enabledPlugins.add(plugin);
        	log.info("enabled plugin: " + name);
    	} else {
    		if (enabledPlugins.contains(plugin)) {
    			pluginEventNotifier.sendPluginStateChangeEvent(new PluginChangeInfo(plugin.getName(), enabled));
    		}

        	enabledPlugins.remove(plugin);
        	log.info("disabled plugin: " + name);
    	}
	}

	@Override
	public String[] getAllPlugins() {
		return allPlugins.getData();
	}

	@Override
	public String[] getEnabledPlugins() {
		return enabledPlugins.getData();
	}

	@Override
	public String[] getEnabledPluginsForHost(String host) {
		return enabledPlugins.getData(host);
	}

	@Override
	public String[] getAllPluginNames() {
		return allPlugins.getNames();
	}

	@Override
	public String[] getEnabledPluginNames() {
		return enabledPlugins.getNames();
	}

	@Override
	public String[] getEnabledPluginNamesForHost(String host) {
		return enabledPlugins.getNames(host);
	}

	@Override
	public void registerPlugins(String[] pluginData) {
		log.info("register plugin " + Arrays.toString(pluginData));
		allPlugins.addData(pluginData);
		enabledPlugins.updateData(pluginData);
	}

	@Override
	public void setPluginTemplateConfiguration(String configuration) {
		log.debug("setPluginTemplateConfiguration of: " + configuration);
		initializeTemplates(configuration);
	}
}
