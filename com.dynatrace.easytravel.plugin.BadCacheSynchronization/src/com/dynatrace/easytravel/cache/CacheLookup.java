package com.dynatrace.easytravel.cache;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.dynatrace.easytravel.frontend.data.JourneyDO;
import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

/**
 * Slows down frontend web requests using synchronization and doing useless things in the synchronized block (so we don't have thread sleep or object.wait as hotspots)
 */
public class CacheLookup extends AbstractGenericPlugin  {

	private static final Map<CacheKey, String> globalCache = new HashMap<CacheKey, String>(100);
	private static final int slowdown = Runtime.getRuntime().availableProcessors();

	@Override
	public Object doExecute(String location, Object... context) {
		for (Object contextElem : context) {
			if (contextElem != null && contextElem instanceof JourneyDO) {
				final JourneyDO journey = (JourneyDO) contextElem;
				synchronized(globalCache) {
					if (!globalCache.containsKey(new CacheKey(journey.getPicture()))) {
						globalCache.put(new CacheKey(journey.getPicture()), "yes");
					}
				}
			}
		}
		return null;
	}

	public class CacheKey {
		final byte[] key;
		public CacheKey(final byte[] bs) {
			if(bs != null)
				this.key = Arrays.copyOf(bs, Math.max(bs.length, 2000)); // only keep max 2000 bytes, we don't want to cause mem problems
			else
				this.key = null;
		}

		/**
		 * just be expensive
		 */
		@Override
		public int hashCode() {
			StringBuilder key = new StringBuilder();

			if(this.key != null) {
				for(int i = 0; i < Math.max(this.key.length, 300 * slowdown); i++) {
					key.append(this.key[i]);
				}
			}
			return key.toString().hashCode();
		}

		/**
		 * have a expensive equals method so we get sync times
		 */
		@Override
		public boolean equals(final Object obj) {
			if(obj instanceof CacheKey) {
				CacheKey other = (CacheKey) obj;
				if(key == null && other.key == null) {
					return true;
				}

				if(key != null && other.key != null) {
					byte[] key = Arrays.copyOf(this.key, this.key.length);
					byte[] otherKey = Arrays.copyOf(other.key, other.key.length);

					sort(key);
					sort(otherKey);

					if(key.length != otherKey.length) {
						return false;
					}

					for(int i = 0; i<key.length; i++) {
						if(key[i] != otherKey[i]) {
							return false;
						}
					}
				}
				return true;
			}
			return false;
		}
	}

	/**
	 * found this bubble sort on the internet - dev had no idea of Arrays.sort
	 */
	public static void sort(byte[] x) {
	    int n = x.length;
	    for (int pass=1; pass < n; pass++) {  // count how many times
	        // This next loop becomes shorter and shorter
	        for (int i=0; i < n-pass; i++) {
	            if (x[i] > x[i+1]) {
	                // exchange elements
	                byte temp = x[i];
	                x[i] = x[i+1];
	                x[i+1] = temp;
	            }
	        }
	    }
	}
}
