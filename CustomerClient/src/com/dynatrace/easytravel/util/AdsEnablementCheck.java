package com.dynatrace.easytravel.util;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.Plugin;
import com.dynatrace.easytravel.spring.PluginList;

/**
 * Helper which provides information if the Ads plugin is currently enabled.
 *
 * @author cwpl-mpankows
 */
public class AdsEnablementCheck {
	private static final Logger LOGGER = LoggerFactory.make();

	private static final PluginList<?> plugins = new PluginList<Plugin>(Plugin.class);

	// private constructor for utility class
	private AdsEnablementCheck() {

	}

	/**
	 * Check if the Ads http error plugin is currently enabled.
	 *
	 * @return true if the plugin is enabled, false otherwise.
	 * @throws Exception
	 */
	public static boolean isADSEnabled() throws Exception {
		for(Plugin plugin : plugins) {
			if(plugin.getName().equals(BaseConstants.Plugins.ADS_ENABLEMENT_PLUGIN)) {
				// this plugin causes an exception whenever it is activated...
				LOGGER.debug("Found ads plugin in list of enabled plugins.");
				throw new Exception("Ads functionality not enabled");
			}
		}

		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Did not find ads plugin in list of enabled plugins.");
		}
		return false;
	}
}
