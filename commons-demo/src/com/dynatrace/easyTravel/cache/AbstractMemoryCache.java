package com.dynatrace.easytravel.cache;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.spring.AbstractPlugin;

/**
 * An abstract MemoryCache implementation that simply relies on a Map implementation.
 *
 * This cache can handle different types of objects and multiple keys.
 * It supports one map instance or one map instance per type.
 * see GenericKey
 *
 * @author philipp.grasboeck
 */
public abstract class AbstractMemoryCache extends AbstractPlugin implements MemoryCache, Serializable
{
	private static final long serialVersionUID = 6028612299637040649L;

	/**
	 * Subclass-hook to provide a map for the given type.
	 * This must never return null.
	 * @param type
	 * @return
	 */
	protected abstract Map<GenericKey, Object> getCacheMap(String type);

	// behaviour control for subclasses
	protected final boolean eager;
	protected final boolean singleton;

	protected AbstractMemoryCache()
	{
		EasyTravelConfig config = EasyTravelConfig.read();
		eager = config.memoryCacheEager;
		singleton = config.memoryCacheSingleton;
	}

	@Override
	public Object get(String type, Object... keys) {
		return getCacheMap(type).get(new GenericKey(type, keys));
	}

	@Override
	public Object put(Object value, String type, Object... keys) {
		return getCacheMap(type).put(new GenericKey(type, keys), value);
	}

	@Override
	public String cacheStats(String type) {
		Map<GenericKey, Object> cacheMap = getCacheMap(type);

		StringBuilder buf = new StringBuilder();
		buf.append(getName()).append("::").append(type).append(",N=").append(cacheMap.size()).append(" [");

		for (Map.Entry<GenericKey, Object> entry : cacheMap.entrySet())
		{
			GenericKey key = entry.getKey();
			Object value = entry.getValue();
			buf.append("\n");
			buf.append(key.type);
			buf.append("::");
			buf.append(Arrays.toString(key.keys));
			buf.append("=@");
			buf.append((value != null) ? value.getClass() : "null");
		}

		buf.append("]");
		return buf.toString();
	}

	// get the hashcode for a GenericKey
	private static int constructHashCode(String type, Object[] keys)
	{
		final int prime = 31;
		int result = 1;

		result = prime * result + (type == null ? 0 : type.hashCode());
		result = prime * result + Arrays.hashCode(keys);

		return result;
	}

	public final class GenericKey implements Serializable
	{
		private static final long serialVersionUID = -8356687732873852301L;

		private final String type;
		private final Object[] keys;
		private final int hashCode;

		private GenericKey(String type, Object[] keys) {
			this.type = type;
			this.keys = ArrayUtils.clone(keys);
			this.hashCode = constructHashCode(type, keys);
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GenericKey other = (GenericKey) obj;
			if (!Arrays.equals(keys, other.keys))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}
	}
}
