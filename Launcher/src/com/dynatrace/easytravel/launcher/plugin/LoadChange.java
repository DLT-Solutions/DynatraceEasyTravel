package com.dynatrace.easytravel.launcher.plugin;

import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeListener;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.PluginChangeMonitor;
import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.Launcher;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

/**
 * @author Rafal.Psciuk
 *
 */
public class LoadChange implements PluginChangeListener {

	private static final Logger LOGGER = LoggerFactory.make();
	volatile private boolean pluginEnabled = false;
	volatile private int lastLoadValue = 0;

	@Override
	public void pluginsChanged() {
		boolean enabled = isPluginEnabled();
		if (enabled != pluginEnabled) {
			pluginEnabled = enabled;
			if (pluginEnabled) {
				//plugin enabled; increase the load
				int newLoad = EasyTravelConfig.read().baseLoadIncreased;
				lastLoadValue =  getBaseLoadValue();
				LOGGER.debug(TextUtils.merge("Load change plugin enabled. Setting load to {0}. Previoud value: {1}", newLoad, lastLoadValue));
				setLoadValue(newLoad);

			} else {
				//plugin disabled decrease the load
				if (lastLoadValue != 0 && getBaseLoadValue() == EasyTravelConfig.read().baseLoadIncreased) { //restore previous load only if user didn't changed anything
					LOGGER.debug(TextUtils.merge("Load change plugin disabled. Setting load to {0}.", lastLoadValue));
					setLoadValue(lastLoadValue);
				}
				lastLoadValue = 0;
			}
		}
	}

	//methods used in test; this is needed to mock static methods
	boolean isPluginEnabled() {
		return PluginChangeMonitor.isPluginEnabled(PluginChangeMonitor.Plugins.LOAD_CHANGE_PLUGIN);
	}

	void setLoadValue(int value) {
		Launcher.setLoadValue(value);
	}

	int getBaseLoadValue() {
		return Launcher.getBaseLoadValue();
	}
}
