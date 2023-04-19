package com.dynatrace.easytravel.components;
import org.apache.commons.lang3.StringUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;

public class SpringComponentManagerProxy implements ComponentManagerProxy {
	 
	    private final ComponentManagerProxy delegate;

		public SpringComponentManagerProxy() {
			super();

	    	EasyTravelConfig config = EasyTravelConfig.read();

	    	String pluginServiceHost = config.pluginServiceHost;

	    	if (StringUtils.isNotBlank(pluginServiceHost)) {
	    		delegate = new RemoteComponentManager(pluginServiceHost, config.pluginServicePort);
	    	} else {
	    		delegate = new ComponentManager();
	    	}

		}

	@Override
	public void setComponent(String ip, String[] params) {
		delegate.setComponent(ip, params);
	}

	@Override
	public String[] getComponentsIPList(String type) {
		return delegate.getComponentsIPList(type);
	}

	@Override
	public void removeComponent(String ip) {
		delegate.removeComponent(ip);
	}

}
