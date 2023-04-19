package com.dynatrace.easytravel.util;

import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;


/**
 * Base class for map-based caches.
 */
abstract class AbstractMapStrategy implements ServiceStubStrategy
{
	private static final Logger log = LoggerFactory.make();

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getServiceStub(Class<T> clazz) throws Exception {
		String serviceName = ServiceStubFactory.getServiceName(clazz);
		String key = getKey(serviceName);
		Map<String, Object> cache = getCacheMap();
		T stub = (T) cache.get(key);
		if (stub == null) {
			stub = ServiceStubFactory.makeStub(clazz, serviceName);
			cache.put(key, stub);
			if (log.isDebugEnabled()) log.debug("Put to cache: " + key);
		} else {
			if (log.isDebugEnabled()) log.debug("Cache hit for service key: " + key);
		}

		return stub;
	}

	@Override
	public <T> void returnServiceStub(T stub) throws Exception {
	}


	@Override
	public <T> void invalidateServiceStub(T stub) throws Exception {
		if(stub == null) {
			return;
		}

		String serviceName = ServiceStubFactory.getServiceName(stub.getClass());
		String key = getKey(serviceName);
		Map<String, Object> cache = getCacheMap();

		// clear this element
		cache.remove(key);
	}

	@Override
	public void clear() {
		getCacheMap().clear();
	}

	/**
	 * the cache map that is used.
	 */
	protected abstract Map<String, Object> getCacheMap();

	/**
	 * they key into the cache map.
	 */
	protected abstract String getKey(String serviceName);
}
