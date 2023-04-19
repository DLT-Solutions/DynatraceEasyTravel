package com.dynatrace.easytravel;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.cache.AbstractMemoryCache;
import com.dynatrace.easytravel.logging.LoggerFactory;

/**
 * Only supports "eager". Always runs as a singleton.
 *
 * @author philipp.grasboeck
 *
 */
public class SimpleMemoryCache extends AbstractMemoryCache {

	private static final long serialVersionUID = 5363962397356003121L;

	private static final Logger log = LoggerFactory.make();

	private Map<GenericKey, Object> cacheMap;

	public SimpleMemoryCache()
	{
		if (eager)
		{
			cacheMap = new HashMap<GenericKey, Object>();
			log.info("Initialize an eager singleton.");
		}
	}

	@Override
	protected Map<GenericKey, Object> getCacheMap(String type)
	{
		if (cacheMap == null)
		{
			cacheMap = new HashMap<GenericKey, Object>();
		}
		return cacheMap;
	}
}
