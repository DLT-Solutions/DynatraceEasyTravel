package com.dynatrace.diagnostics.uemload;

import static java.lang.String.format;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class BrowserCache {

	private static final Logger log = Logger.getLogger(BrowserCache.class.getName());
	private static final UrlCache URL_CACHE = UrlCache.createWithExpiryTime(5, TimeUnit.MINUTES);


	public static final long DO_NOT_CACHE = -1;

	private static String CACHED_FILE_TYPES = "htm|html|png|css|js|jpe?g";
	private static Pattern CACHED_FILE_TYPES_PATTERN = Pattern.compile("([^\\s]+(\\.(" + CACHED_FILE_TYPES + ")(/*))$)",
			Pattern.CASE_INSENSITIVE);

	private ConcurrentMap<String, Long> cache = new ConcurrentHashMap<String, Long>();

	public void addResource(String url, long maxAgeInSeconds)
	{
		if (maxAgeInSeconds == DO_NOT_CACHE)
		{
			if (log.isLoggable(Level.FINE))
				log.fine(format("Resource '%s' should not be cached, its max age is set to %d", url, maxAgeInSeconds));
			return;
		}
		if (isNoCachedFiletype(url))
		{
			if (log.isLoggable(Level.FINE))
				log.fine(format("Resource '%s' is not supported by Browser cache", url));
			return;
		}
		String cachedUrl = URL_CACHE.cashify(url);

		Long expiryTime = getExpiryTime(maxAgeInSeconds);
		cache.putIfAbsent(cachedUrl, expiryTime);
	}


	public boolean isLoadOfResourceNecessary(String url)
	{
		if (isNoCachedFiletype(url))
			return true;

		String cachedUrl = URL_CACHE.cashify(url);
		Long expiryTime = cache.get(cachedUrl);

		if (isLoadRequired(expiryTime))
		{
			removeOutdatedCacheEntry(cachedUrl, expiryTime);
			return true;
		}
		return false;
	}

	public static boolean isNoCachedFiletype(String url)
	{
		Matcher cachedFileTypeMatcher = CACHED_FILE_TYPES_PATTERN.matcher(url);
		return !cachedFileTypeMatcher.find();
	}

	private void removeOutdatedCacheEntry(String url, Long expiryTime)
	{
		cache.remove(url, expiryTime);
	}

	private boolean isLoadRequired(Long expiryTime)
	{
		return expiryTime == null || System.currentTimeMillis() > expiryTime;
	}

	private static Long getExpiryTime(Long maxAgeInSeconds)
	{
		return System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(maxAgeInSeconds);
	}
}
