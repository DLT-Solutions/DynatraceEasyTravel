package com.dynatrace.easytravel.validatename;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractPagePlugin;
import com.dynatrace.easytravel.spring.PluginConstants;
import com.dynatrace.easytravel.util.LocationParser;

import ch.qos.logback.classic.Logger;

public class ValidateName extends AbstractPagePlugin {

	private static final Logger log = LoggerFactory.make();

	@Override
	public Object doExecute(String location, Object... context) {
		
		if (PluginConstants.BACKEND_JOURNEY_VALIDATENAME.equals(location)) {
			log.trace("calling busyWait...");
			LocationParser.parseSection(EasyTravelConfig.read().cpuLoadJourneyServiceWaitTime); 
		}
		
		return super.doExecute(location, context);
	}
}
