package com.dynatrace.easytravel.spring;

import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;

import com.dynatrace.easytravel.config.Version;

/**
 * Class contains information needed to send a new event notification about plugin change
 * 
 * @author tomasz.wieremjewicz
 * @date 20 lis 2017
 *
 */
public class PluginChangeInfo {
	private static final FastDateFormat DATE_FORMATTER = FastDateFormat.getInstance("yyyyMMdd-HHmmss");
	
	public PluginChangeInfo(String name, boolean isEnabled) {
		pluginName = name;
		enabled = Boolean.toString(isEnabled);
		version = Version.read().toString();
		timestamp = DATE_FORMATTER.format(new Date());
	}
	
	public String pluginName; // NOSONAR - public on purpose
	public String enabled; // NOSONAR - public on purpose
	public String version; // NOSONAR - public on purpose
	public String timestamp; // NOSONAR - public on purpose
}
