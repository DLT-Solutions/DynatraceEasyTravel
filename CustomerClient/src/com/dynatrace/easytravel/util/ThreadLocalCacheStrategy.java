package com.dynatrace.easytravel.util;

import java.util.HashMap;
import java.util.Map;


class ThreadLocalCacheStrategy extends AbstractMapStrategy
{
	private final ThreadLocal<Map<String, Object>> stubCacheLocal = new ThreadLocal<Map<String,Object>>()
	{
		@Override
		protected Map<String,Object> initialValue() {
			return new HashMap<String, Object>();
		}
	};

	@Override
	protected Map<String, Object> getCacheMap() {
		return stubCacheLocal.get();
	}

	@Override
	protected String getKey(String serviceName) {
		return serviceName;
	}
}