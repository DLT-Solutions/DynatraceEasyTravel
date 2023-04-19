package com.dynatrace.easytravel;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

import ch.qos.logback.classic.Logger;

public class SmallMemoryLeak extends AbstractGenericPlugin  {

    private static Logger log = LoggerFactory.make();

	@Override
	public Object doExecute(String location, Object... context) {
		// only act on this extension point

		log.info("Plugin for memory leak in business backend is enabled.");

		// expect an AtomicBoolean for this extension point,
		// simply set this to true as we are only called here if the plugin is enabled
		((AtomicBoolean)context[0]).set(true);

		return null;
	}
}
