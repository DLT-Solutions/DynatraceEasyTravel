package com.dynatrace.easytravel;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dynatrace.easytravel.jpa.JpaAccessUtils;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;
import com.dynatrace.easytravel.spring.PluginConstants;

import ch.qos.logback.classic.Logger;

public class DBSpamming extends AbstractGenericPlugin  {
	private static Logger log = LoggerFactory.make();

	private static final String JOURNEY_CACHE = "com.dynatrace.easytravel.jpa.business.Journey";
	private static final int MAX_ENTRIES_IN_LOCAL_HEAP = 500;

	private String mode;
	private String cache = JOURNEY_CACHE;
	private int cacheEntries = MAX_ENTRIES_IN_LOCAL_HEAP;

	private long previousMaxEntries = -1;

	@Override
	public Object doExecute(String location, Object... context) {
		log.info(getName() + " Plugin for DBSpamming in business backend is enabled.");
		if( log.isDebugEnabled()) {
			log.debug("cache: " + cache);
			log.debug("cacheEntries: " + cacheEntries);
		}

		// reduce cache size when this plugin is enable to increase number of db statements even more
		if (PluginConstants.LIFECYCLE_PLUGIN_ENABLE.equals(location)) {
			log.info("dbspamming starting");

			previousMaxEntries = JpaAccessUtils.changeMaxEntriesInLocalHeap(cache, cacheEntries);

			return null;
		} else if (PluginConstants.LIFECYCLE_PLUGIN_DISABLE.equals(location)){
			log.info("dbspamming stopping");

			if(previousMaxEntries != -1) {
				JpaAccessUtils.changeMaxEntriesInLocalHeap(cache, previousMaxEntries);
			}

			return null;
		}

		if(mode.equalsIgnoreCase("light")) {
		    // TODO:
		} else {
			((AtomicBoolean)context[0]).set(true);
		}

		return null;
	}
	
	public void setMode(final String mode) {
		this.mode = mode;
	}
	
	public void setCache(final String cache) {
		this.cache = cache;
	}

	public void setCacheEntries(int cacheEntries) {
		this.cacheEntries = cacheEntries;
	}	
}
