package com.dynatrace.easytravel.jpa;

import java.util.Map;
import java.util.Properties;

import ch.qos.logback.classic.Logger;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;

import com.dynatrace.easytravel.logging.LoggerFactory;

/**
 * Helper defines/methods for JPA related things.
 *
 * @author dominik.stadler
 */
public class JpaAccessUtils {
	private static Logger log = LoggerFactory.make();

	// defines for the properties that JPA uses for the database access
	public static final String JPA_DRIVER = "javax.persistence.jdbc.driver";
	public static final String JPA_URL = "javax.persistence.jdbc.url";
	public static final String JPA_USER = "javax.persistence.jdbc.user";
	public static final String JPA_PASSWORD = "javax.persistence.jdbc.password";

	/**
	 * Set system properties for JPA initialization.
	 *
	 * @param driver
	 * @param url
	 * @param user
	 * @param password
	 */
	public static void setProperties(String driver, String url, String user,
			String password) {
		Properties map = System.getProperties();

		if(!map.containsKey(JPA_DRIVER)) {
			map.setProperty(JPA_DRIVER, driver);
		}
		if(!map.containsKey(JPA_URL)) {
			map.setProperty(JPA_URL, url);
		}
		if(!map.containsKey(JPA_USER)) {
			map.setProperty(JPA_USER, user);
		}
		if(!map.containsKey(JPA_PASSWORD)) {
			map.setProperty(JPA_PASSWORD, password);
		}
	}

	/**
	 * Return the ehcache CacheConfiguration for the given cache
	 *
	 * @param cache The name of the cache as used in the ehcache.xml configuration file.
	 * @return A CacheConfiguration or null if this cache is not known.
	 */
	public static CacheConfiguration getCacheConfiguration(String cache) {
		CacheManager cacheManager = CacheManager.getCacheManager(CacheManager.DEFAULT_NAME);
		if(cacheManager == null) {
			log.warn("Could not get the default Cache Manager");
			return null;
		}
		Configuration configuration = cacheManager.getConfiguration();
		Map<String, CacheConfiguration> cacheConfigurations = configuration.getCacheConfigurations();
		CacheConfiguration cacheConfiguration = cacheConfigurations.get(cache);
		return cacheConfiguration;
	}

	/**
	 * Adjust the "max elements in memory" setting for the given cache.
	 *
	 * @param cache
	 * @param newValue
	 * @return Returns the previous value for the setting or -1 if the cache cannot be found.
	 */
	public static long changeMaxEntriesInLocalHeap(String cache, long newValue) {
		CacheConfiguration cacheConfiguration = JpaAccessUtils.getCacheConfiguration(cache);
		if(cacheConfiguration == null) {
			return -1;
		}

		long previousMaxEntries = cacheConfiguration.getMaxEntriesLocalHeap();
		cacheConfiguration.setMaxEntriesLocalHeap(newValue);
		log.info("Switched cache-entries from " + previousMaxEntries + " to " + newValue + " for cache " + cache);

		// clear out caches to avoid OOM, cannot be null, checked above
		CacheManager cacheManager = CacheManager.getCacheManager(CacheManager.DEFAULT_NAME);
		cacheManager.clearAll();

		return previousMaxEntries;
	}
}
