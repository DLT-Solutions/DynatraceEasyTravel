/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: UrlCache.java
 * @date: 27.11.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.diagnostics.uemload;

import static java.lang.String.format;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dynatrace.easytravel.annotation.TestOnly;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


/**
 * To save a URL string not several times, you can use this {@link UrlCache}
 *
 * @author stefan.moschinski
 */
class UrlCache {

	private static final Logger log = Logger.getLogger(UrlCache.class.getName());

	private final Cache<String, String> urlCache;

	/**
	 *
	 * @param timeout specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after the entry's creation, the most recent replacement of its value, or its last access.
	 * 			(see {@link CacheBuilder#expireAfterAccess(long, TimeUnit)})
	 * @param unit {@link TimeUnit} of the timeout
	 * @return new {@link UrlCache} instance
	 * @author stefan.moschinski
	 */
	static UrlCache createWithExpiryTime(int timeout, TimeUnit unit)
	{
		return new UrlCache(timeout, unit);
	}

	private UrlCache(int timeout, TimeUnit unit)
	{
		urlCache = CacheBuilder.newBuilder().maximumSize(100).expireAfterAccess(timeout, unit).build();
	}


	/**
	 *
	 * @param url 
	 * @return the given string if it was not cached previously or an equal instance
	 * @author stefan.moschinski
	 */
	public String cashify(final String url)
	{
		try
		{
			return urlCache.get(url, new Callable<String>()
			{
				// the callable actually adds the given url to the cache
				@Override
				public String call() throws Exception
				{
					return url;
				}
			});
		} catch (ExecutionException e)
		{
			log.log(Level.WARNING, format("Could not load '%s' from cache", url), e.getCause());
			return url;
		}
	}

	@TestOnly
	String get(String url)
	{
		return urlCache.getIfPresent(url);
	}
}
