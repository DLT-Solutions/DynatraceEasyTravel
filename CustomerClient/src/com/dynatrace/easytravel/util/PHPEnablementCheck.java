package com.dynatrace.easytravel.util;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.Plugin;
import com.dynatrace.easytravel.spring.PluginList;

/**
 * Helper which provides information if the PHP Support is currently enabled.
 *
 * @author cwat-dstadler
 */
public class PHPEnablementCheck {
	private static final Logger LOGGER = LoggerFactory.make();

	private static final PluginList<?> plugins = new PluginList<Plugin>(Plugin.class);

	/**
	 * Check if the PHP Enablement Plugin is currently enabled.
	 *
	 * @return true if the plugin is enabled, false otherwise.
	 */
	private static boolean isPHPPluginEnabled() {
		for(Plugin plugin : plugins) {
			if(plugin.getName().equals(BaseConstants.Plugins.PHP_ENABLEMENT_PLUGIN)) {
				LOGGER.debug("Found PHP plugin in list of enabled plugins.");
				return true;
			}
		}

		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Did not find PHP plugin in list of enabled plugins.");
		}
		return false;
	}

	public static boolean isPHPEnabled() {
		return !EasyTravelConfig.read().PHPEnabledForAngularOnly && isPHPPluginEnabled();
	}

	public static boolean isPHPEnabledOnAngularFrontend() {
		return isPHPPluginEnabled();
	}
	
	/**
	 * Used in tests only
	 * @return 
	 */
	public static PluginList<?> getPluginList() {
		return plugins;
	}
}
