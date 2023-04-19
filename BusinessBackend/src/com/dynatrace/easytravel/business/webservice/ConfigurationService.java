package com.dynatrace.easytravel.business.webservice;

import com.dynatrace.easytravel.components.ComponentManagerProxy;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.spring.PluginStateProxy;
import com.dynatrace.easytravel.spring.SpringUtils;

public class ConfigurationService implements Configuration, PluginStateProxy, ComponentManagerProxy {
	private String externalUrlAccess;
	private boolean frontendDeadlockEnabled;
	private boolean dBSpammingEnabled;
	private boolean memoryLeakEnabled;
	private boolean dBSpammingAuthEnabled;

	public String ping() {
		return "pong";
	}

	/* (non-Javadoc)
	 * @see com.dynatrace.easytravel.business.webservice.Configuration#setAccessExternalUrl(boolean)
	 */
    @Override
	public void setExternalUrl(String external) {
    	this.externalUrlAccess = external;
    }

    /* (non-Javadoc)
	 * @see com.dynatrace.easytravel.business.webservice.Configuration#getAccessExternalUrl()
	 */
    @Override
	public String getExternalUrl() {
    	return externalUrlAccess;
    }


    /* (non-Javadoc)
     * @see com.dynatrace.easytravel.business.webservice.Configuration#getAccessExternalUrl()
     */
    @Override
    public void setMemoryLeakEnabled(boolean memoryLeakEnabled) {
        this.memoryLeakEnabled = memoryLeakEnabled;
    }

    /* (non-Javadoc)
     * @see com.dynatrace.easytravel.business.webservice.Configuration#getAccessExternalUrl()
     */
    @Override
    public boolean isMemoryLeakEnabled() {
        return memoryLeakEnabled;
    }

    /* (non-Javadoc)
     * @see com.dynatrace.easytravel.business.webservice.Configuration#getAccessExternalUrl()
     */
    @Override
    public void setDBSpammingEnabled(boolean dBSpammingEnabled) {
        this.dBSpammingEnabled = dBSpammingEnabled;
    }

    /* (non-Javadoc)
     * @see com.dynatrace.easytravel.business.webservice.Configuration#getAccessExternalUrl()
     */
    @Override
    public boolean isDBSpammingEnabled() {
        return dBSpammingEnabled;
    }

	/* (non-Javadoc)
     * @see com.dynatrace.easytravel.business.webservice.Configuration#getAccessExternalUrl()
     */
    @Override
    public void setFrontendDeadlockEnabled(boolean frontendDeadlockEnabled) {
        this.frontendDeadlockEnabled = frontendDeadlockEnabled;
    }

    /* (non-Javadoc)
     * @see com.dynatrace.easytravel.business.webservice.Configuration#getAccessExternalUrl()
     */
    @Override
    public boolean isFrontendDeadlockEnabled() {
        return frontendDeadlockEnabled;
    }

    @Override
	public void setBackendCPUCycleTime(long timeMS) {
		EasyTravelConfig.read().backendCPUCycleTime = timeMS;
	}

	@Override
	public long getBackendCPUCycleTime() {
		return EasyTravelConfig.read().backendCPUCycleTime;
	}

	@Override
	public long getCPULoadJourneyServiceWaitTime() {
		return EasyTravelConfig.read().cpuLoadJourneyServiceWaitTime;
	}

    @Override
	public void setCPULoadJourneyServiceWaitTime(long timeMS) {
		EasyTravelConfig.read().cpuLoadJourneyServiceWaitTime = timeMS;
	}

    @Override
	public void setBackendCPUCalibration(double calibration) {
		EasyTravelConfig.read().CPUCalibration = calibration;
	}

	@Override
	public double getBackendCPUCalibration() {
		return EasyTravelConfig.read().CPUCalibration;
	}

	@Override
	public boolean isDBSpammingAuthEnabled() {
		return dBSpammingAuthEnabled;
	}

	@Override
	public void setDBSpammingAuthEnabled(boolean dbSpammingSearchEnabled) {
		this.dBSpammingAuthEnabled = dbSpammingSearchEnabled;
	}

	/************************************************************************
     * These methods are included here, but not called anywhere in code because
     * we want to provide them as Web Service/REST interfaces for other procedures
     * to call in and request information about available and enabled plugins.
     */

	@Override
	public void setPluginEnabled(String name, boolean enabled) {
    	SpringUtils.getPluginStateProxy().setPluginEnabled(name, enabled);
    }

	@Override
	public void setPluginHosts(String name, String[] hosts) {
    	SpringUtils.getPluginStateProxy().setPluginHosts(name, hosts);
    }

	@Override
	public String[] getAllPluginNames() {
    	return SpringUtils.getPluginStateProxy().getAllPluginNames();
    }

	@Override
	public String[] getEnabledPluginNames() {
    	return SpringUtils.getPluginStateProxy().getEnabledPluginNames();
    }

	@Override
	public String[] getEnabledPluginNamesForHost(String host) {
    	return SpringUtils.getPluginStateProxy().getEnabledPluginNamesForHost(host);
    }

	@Override
	public String[] getAllPlugins() {
		return SpringUtils.getPluginStateProxy().getAllPlugins();
	}

	@Override
	public String[] getEnabledPluginsForHost(String host) {
		return SpringUtils.getPluginStateProxy().getEnabledPluginsForHost(host);
	}

	@Override
	public String[] getEnabledPlugins() {
		return SpringUtils.getPluginStateProxy().getEnabledPlugins();
	}

	@Override
	public void registerPlugins(String[] pluginData) {
		SpringUtils.getPluginStateProxy().registerPlugins(pluginData);
	}

	@Override
	public void setDBSpammingAuthSize(int spamSize) {
		EasyTravelConfig.read().authServiceSpamSize = spamSize;
	}

	@Override
	public void setDBSpammingAuthDelay(int delay) {
		EasyTravelConfig.read().authServiceGetUserDelay = delay;
	}

	@Override
	public int getDBSpammingAuthSize() {
		return EasyTravelConfig.read().authServiceSpamSize;
	}

	@Override
	public int getDBSpammingAuthDelay() {
		return EasyTravelConfig.read().authServiceGetUserDelay;
	}

	@Override
	public boolean isFullAuthServiceSpammingEnabled() {
		return EasyTravelConfig.read().isFullAuthServiceSpammingEnabled;
	}

	@Override
	public void setFullAuthServiceSpamming(boolean full) {
		EasyTravelConfig.read().isFullAuthServiceSpammingEnabled = full;
	}

    @Override
    public void setDatabaseSlowDownDelay(int delay) {
        EasyTravelConfig.read().databaseSlowdownDelay = delay;
    }

    @Override
    public int getDatabaseSlowDownDelay() {
        return EasyTravelConfig.read().databaseSlowdownDelay;
    }

	@Override
	public void setComponent(String ip, String[] params) {
		SpringUtils.getComponentsManagerProxy().setComponent(ip, params);

	}

	@Override
	public String[] getComponentsIPList(String type) {
		return SpringUtils.getComponentsManagerProxy().getComponentsIPList(type);
	}

	@Override
	public void removeComponent(String ip) {
		SpringUtils.getComponentsManagerProxy().removeComponent(ip);
	}

	@Override
	public void setPluginTemplateConfiguration(String configuration) {
		SpringUtils.getPluginStateProxy().setPluginTemplateConfiguration(configuration);
	}
}
