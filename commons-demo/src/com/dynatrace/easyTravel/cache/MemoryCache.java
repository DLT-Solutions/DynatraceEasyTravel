package com.dynatrace.easytravel.cache;

import com.dynatrace.easytravel.spring.Plugin;

/**
 * API for a memory cache plugin
 * 
 * @author philipp.grasboeck
 *
 */
public interface MemoryCache extends Plugin {
	
	/**
	 * Get an object from the cache.
	 * 
	 * @param type the type of object
	 * @param keys one or more keys to identify the object
	 * @return
	 */
	public Object get(String type, Object... keys);
	
	/**
	 * Put an object to the cache.
	 * 
	 * @param value The object to put
	 * @param type the type of object 
	 * @param keys one or more keys to identify the object
	 * @return
	 */
	public Object put(Object value, String type, Object... keys);
	
	public String cacheStats(String type);
}
