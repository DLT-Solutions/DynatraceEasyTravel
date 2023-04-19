package com.dynatrace.easytravel.util;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.Plugin;
import com.dynatrace.easytravel.spring.PluginList;

/**
 * Plugin which generates slow transactions for PHP  
 *
 * @author cwpl-mpankows
 */
public class SlowTransactionsCheck {
	private static final Logger LOGGER = LoggerFactory.make();

	private static final PluginList<?> plugins = new PluginList<Plugin>(Plugin.class);
	
	private SlowTransactionsCheck() {}
	
	public static boolean isPHPBlogEnabled() {
		for(Plugin plugin : plugins) {
			if(plugin.getName().equals(BaseConstants.Plugins.SLOW_TRANSACTION_FOR_PHP_BLOG)) {
				LOGGER.debug("Found Slow Transaction for PHP Blog plugin in list of enabled plugins.");
				return true;
			}
		}

		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Did not find Slow Transaction for PHP Blog plugin in list of enabled plugins.");
		}
		return false;
	}
}
