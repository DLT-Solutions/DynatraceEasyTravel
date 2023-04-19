package com.dynatrace.easytravel.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


class ThreadGlobalCacheStrategy extends AbstractMapStrategy
{
	private final Map<String, Object> stubCacheGlobal = new ConcurrentHashMap<String, Object>();

	@Override
	protected Map<String, Object> getCacheMap() {
		return stubCacheGlobal;
	}

	@Override
	protected String getKey(String serviceName) {
		return serviceName + "@" + Thread.currentThread().getId();
	}
}