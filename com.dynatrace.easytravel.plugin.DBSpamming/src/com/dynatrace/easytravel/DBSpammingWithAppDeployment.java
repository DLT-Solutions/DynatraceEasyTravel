package com.dynatrace.easytravel;

import java.util.concurrent.TimeUnit;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.PluginConstants;

import ch.qos.logback.classic.Logger;

/**
 * @author cwpl-rpsciuk
 * Start WebApplicationDeployment & and DBSpamming in one problem pattern 
 */
public class DBSpammingWithAppDeployment extends DBSpamming {
	
	private static Logger log = LoggerFactory.make();
	
	private final static long DEFAULT_SPAMMING_DELAY = TimeUnit.SECONDS.toMillis(60);
	//not final because it is modified in tests
	private WarDeploymentPlugin webAppDeploymentdPlugin = new WarDeploymentPlugin();
	//not final because it is modified in tests
	private long spammingDelay = DEFAULT_SPAMMING_DELAY;
	private volatile long pluginStartTime = 0;
	private volatile Boolean dbSpammingEnabled = Boolean.FALSE;
	
	@Override
	public Object doExecute(String location, Object... context) {
		
		if (PluginConstants.LIFECYCLE_PLUGIN_ENABLE.equals(location)) {
			log.debug("start WebApplicationDeployment");
			pluginStartTime = System.currentTimeMillis();
			webAppDeploymentdPlugin.doExecute(location, context);
			return null;
			
		} else if (PluginConstants.LIFECYCLE_PLUGIN_DISABLE.equals(location)){
			log.info("stop DBSpammingWithAppDeployment");
			dbSpammingEnabled = Boolean.FALSE;
			pluginStartTime = 0;
			super.doExecute(location, context);
			return null;
		}
		
		//check current lcoation, we should start dbspamming only if location is set to backend.authenticationservice.authenticate.getuser
		if (!"backend.authenticationservice.authenticate.getuser".equals(location)){
			return null;
		}
		
		//check if we should start dbspamming
		if (System.currentTimeMillis() < pluginStartTime + spammingDelay) {
			return null;
		}

		if (!dbSpammingEnabled) { // enable db spamming
			log.debug("start DBSpamming enabled");
			super.doExecute(PluginConstants.LIFECYCLE_PLUGIN_ENABLE, context);
			dbSpammingEnabled = Boolean.TRUE;
		}			
		
		super.doExecute(location, context);

		return null;
	}

	@TestOnly
	public void setWebAppDeploymentdPlugin(WarDeploymentPlugin webAppDeploymentdPlugin) {
		this.webAppDeploymentdPlugin = webAppDeploymentdPlugin;
	}

	@TestOnly
	public void setSpammingDelay(long spammingDelay) {
		this.spammingDelay = spammingDelay;
	}
}
