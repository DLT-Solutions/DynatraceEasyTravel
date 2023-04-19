package com.dynatrace.easytravel.spring;

import ch.qos.logback.classic.Logger;

import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;

/**
 * Helper class which will decide during construction time
 * to which implementation of {@link PluginStateProxy} it should delegate.
 *
 * If a pluginServiceHost is configured, it will delegate via {@link RemotePluginService},
 * otherwise it will use a {@link PluginStateManager} directly.
 *
 * This class is instantiated via the Spring-context.
 *
 * @author cwat-dstadler
 */
public class SpringPluginStateProxy implements PluginStateProxy {
    private static final Logger log = LoggerFactory.make();

    private final PluginStateProxy delegate;

	public SpringPluginStateProxy() {
		super();

    	EasyTravelConfig config = EasyTravelConfig.read();

    	String pluginServiceHost = config.pluginServiceHost;

    	if (StringUtils.isNotBlank(pluginServiceHost)) {
    		log.info("Using remote plugin service instance via RemotePluginService via " + pluginServiceHost + ":" + config.pluginServicePort);
    		delegate = new RemotePluginService(pluginServiceHost, config.pluginServicePort);
    	} else {
    		log.info("Using local plugin service via PluginStateManager");
    		delegate = new PluginStateManager();
    	}
	}

	@Override
	public String[] getAllPluginNames() {
		return delegate.getAllPluginNames();
	}

	@Override
	public String[] getEnabledPluginNames() {
		return delegate.getEnabledPluginNames();
	}

	@Override
	public String[] getEnabledPluginNamesForHost(String host) {
		return delegate.getEnabledPluginNamesForHost(host);
	}

	@Override
	public String[] getAllPlugins() {
		return delegate.getAllPlugins();
	}

	@Override
	public String[] getEnabledPlugins() {
		return delegate.getEnabledPlugins();
	}

	@Override
	public String[] getEnabledPluginsForHost(String host) {
		return delegate.getEnabledPluginsForHost(host);
	}

	@Override
	public void registerPlugins(String[] pluginData) {
		delegate.registerPlugins(pluginData);
	}

	@Override
	public void setPluginEnabled(String name, boolean enabled) {
		delegate.setPluginEnabled(name, enabled);
	}

	@Override
	public void setPluginHosts(String name, String[] hosts) {
		delegate.setPluginHosts(name, hosts);
	}

	@Override
	public void setPluginTemplateConfiguration(String configuration) {
		delegate.setPluginTemplateConfiguration(configuration);
	}
}
