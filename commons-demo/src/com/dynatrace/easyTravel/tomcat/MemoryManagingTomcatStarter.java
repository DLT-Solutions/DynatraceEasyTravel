package com.dynatrace.easytravel.tomcat;

import java.util.logging.Logger;

import org.apache.catalina.Context;

import com.dynatrace.easytravel.config.EasyTravelConfig;


public class MemoryManagingTomcatStarter extends Tomcat7Starter {

	private static final Logger log = Logger.getLogger(MemoryManagingTomcatStarter.class.getName());

	@Override
	protected void createPersistentManager(String workDir, Context context) {
		AutomaticMemoryManager manager = new AutomaticMemoryManager(getServerName());
		setManager(manager);
		context.setManager(manager);

		EasyTravelConfig config = EasyTravelConfig.read();
		manager.setMinSessionLifetime(config.minSessionLifetimeInMillis);
		log.info("Created session manager with minimum session life time of " + config.minSessionLifetimeInMillis + " ms.");

	}
}
