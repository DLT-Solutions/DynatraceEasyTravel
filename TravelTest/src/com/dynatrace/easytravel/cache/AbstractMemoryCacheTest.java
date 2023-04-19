 package com.dynatrace.easytravel.cache;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.dynatrace.easytravel.cache.AbstractMemoryCache.GenericKey;
import com.dynatrace.easytravel.utils.TestHelpers;


public class AbstractMemoryCacheTest {
	private Map<GenericKey, Object> map = new HashMap<AbstractMemoryCache.GenericKey, Object>();

	private AbstractMemoryCache cache = new AbstractMemoryCache() {
		private static final long serialVersionUID = 1L;

		@Override
		protected Map<GenericKey, Object> getCacheMap(String type) {
			return map;
		}
	};

	@Before
	public void setUp() {
		map.clear();
	}

	@Test
	public void testGetCacheMap() {
		assertNotNull(cache.getCacheMap(null));
	}

	@Test
	public void testGet() {
		assertNull("Should not find the object when it is not in the map", cache.get("type", "key1"));

		assertNull("Should be able to put, but should not find a previous item", cache.put("data1", "type", "key1"));

		assertEquals("Should find the object when it is in the map", "data1", cache.get("type", "key1"));

		assertEquals("Should be able to put and should find a previous item", "data1", cache.put("data2", "type", "key1"));

		assertEquals("Should find the second object when it is in the map", "data2", cache.get("type", "key1"));

		assertNull("Should be able to put, but should not find a previous item when using different type", cache.put("data1", "type2", "key1"));
	}

	@Test
	public void testCacheStats() {
		assertNotNull(cache.cacheStats("type"));

		assertNull("Should be able to put, but should not find a previous item", cache.put("data1", "type", "key1"));
		assertNull("Should be able to put, but should not find a previous item", cache.put("data1", "type", "key2"));

		assertNotNull("Second time to have actual content in the cache", cache.cacheStats("type"));

		assertNull("Should be able to put null, but should not find a previous item", cache.put(null, "type", "key3"));

		assertNotNull("This time to have null content in the cache", cache.cacheStats("type"));
	}

	@Test
	public void testPutMany() {
		assertNull("Should not find the object when it is not in the map", cache.get("type", "key1"));

		for(int i = 0;i < 10000;i++) {
			assertNull("Should be able to put, but should not find a previous item", cache.put("data" + i, "type1", "key" + i));
			assertNull("Should be able to put, but should not find a previous item", cache.put("data" + i, "type2", "key" + i));
			assertNull("Should be able to put, but should not find a previous item", cache.put("data" + i, "type3", "key" + i));
			assertNull("Should be able to put, but should not find a previous item", cache.put(null, "type4", "key" + i));
			assertNull("Should be able to put, but should not find a previous item", cache.put(null, "type4", (Object)null));
			assertNull("Should be able to put, but should not find a previous item", cache.put(null, "type4"));

			assertNotNull("Should be able to put, but should find a previous item", cache.put("data" + i, "type1", "key" + i));
			assertNotNull("Should be able to put, but should find a previous item", cache.put("data" + i, "type2", "key" + i));
			assertNotNull("Should be able to put, but should find a previous item", cache.put("data" + i, "type3", "key" + i));

			assertNull("Should be able to put, but should not find a previous item", cache.put(null, "type4", "key" + i));
			assertNull("Should be able to put, but should not find a previous item", cache.put(null, "type4", (Object)null));
			assertNull("Should be able to put, but should not find a previous item", cache.put(null, "type4"));
		}
		assertNotNull(cache.cacheStats("typeNotExist"));
		assertNotNull(cache.cacheStats("type1"));
		assertNotNull(cache.cacheStats("type2"));
		assertNotNull(cache.cacheStats("type3"));
	}

	@Test
	public void testGenericKey() {
		Map<GenericKey, Object> cacheMap = cache.getCacheMap("type");

		// first put two items into the cache to get a equal and a "not-equal" key
		cache.put("data1", "type", "key1");
		GenericKey key1 = cacheMap.keySet().iterator().next();
		assertNotNull(map.remove(key1));
		assertFalse(cacheMap.keySet().iterator().hasNext());

		cache.put("data2", "type", "key2", "key3");
		GenericKey notEq = cacheMap.keySet().iterator().next();
		assertNotNull(map.remove(notEq));
		assertFalse(cacheMap.keySet().iterator().hasNext());

		cache.put("data1", "type", "key1");
		GenericKey equal = cacheMap.keySet().iterator().next();
		assertNotNull(map.remove(equal));
		assertFalse(cacheMap.keySet().iterator().hasNext());

		TestHelpers.EqualsTest(key1, equal, notEq);
		TestHelpers.HashCodeTest(key1, equal);
	}

	@Test
	public void testGenericKeyTypeNotEqual() {
		Map<GenericKey, Object> cacheMap = cache.getCacheMap("type");

		// first put two items into the cache to get a equal and a "not-equal" key
		cache.put("data1", "type", "key1");
		GenericKey key1 = cacheMap.keySet().iterator().next();
		assertNotNull(map.remove(key1));
		assertFalse(cacheMap.keySet().iterator().hasNext());

		cache.put("data2", "type2", "key1");
		GenericKey notEq = cacheMap.keySet().iterator().next();
		assertNotNull(map.remove(notEq));
		assertFalse(cacheMap.keySet().iterator().hasNext());

		cache.put("data1", "type", "key1");
		GenericKey equal = cacheMap.keySet().iterator().next();
		assertNotNull(map.remove(equal));
		assertFalse(cacheMap.keySet().iterator().hasNext());

		TestHelpers.EqualsTest(key1, equal, notEq);
		TestHelpers.HashCodeTest(key1, equal);
	}

	@Test
	public void testGenericKeyTypeNull() {
		Map<GenericKey, Object> cacheMap = cache.getCacheMap(null);

		// first put two items into the cache to get a equal and a "not-equal" key
		cache.put("data1", null, "key1");
		GenericKey key1 = cacheMap.keySet().iterator().next();
		assertNotNull(map.remove(key1));
		assertFalse(cacheMap.keySet().iterator().hasNext());

		cache.put("data2", null, "key2", "key3");
		GenericKey notEq = cacheMap.keySet().iterator().next();
		assertNotNull(map.remove(notEq));
		assertFalse(cacheMap.keySet().iterator().hasNext());

		cache.put("data1", null, "key1");
		GenericKey equal = cacheMap.keySet().iterator().next();
		assertNotNull(map.remove(equal));
		assertFalse(cacheMap.keySet().iterator().hasNext());

		TestHelpers.EqualsTest(key1, equal, notEq);
		TestHelpers.HashCodeTest(key1, equal);
	}

	@Test
	public void testGenericKeyTypeMixed1() {
		Map<GenericKey, Object> cacheMap = cache.getCacheMap(null);

		// first put two items into the cache to get a equal and a "not-equal" key
		cache.put("data1", null, "key1");
		GenericKey key1 = cacheMap.keySet().iterator().next();
		assertNotNull(map.remove(key1));
		assertFalse(cacheMap.keySet().iterator().hasNext());

		cache.put("data2", "type", "key1");
		GenericKey notEq = cacheMap.keySet().iterator().next();
		assertNotNull(map.remove(notEq));
		assertFalse(cacheMap.keySet().iterator().hasNext());

		cache.put("data1", null, "key1");
		GenericKey equal = cacheMap.keySet().iterator().next();
		assertNotNull(map.remove(equal));
		assertFalse(cacheMap.keySet().iterator().hasNext());

		TestHelpers.EqualsTest(key1, equal, notEq);
		TestHelpers.HashCodeTest(key1, equal);
	}

	@Test
	public void testGenericKeyTypeMixed2() {
		Map<GenericKey, Object> cacheMap = cache.getCacheMap(null);

		// first put two items into the cache to get a equal and a "not-equal" key
		cache.put("data1", "type", "key1");
		GenericKey key1 = cacheMap.keySet().iterator().next();
		assertNotNull(map.remove(key1));
		assertFalse(cacheMap.keySet().iterator().hasNext());

		cache.put("data2", null, "key1");
		GenericKey notEq = cacheMap.keySet().iterator().next();
		assertNotNull(map.remove(notEq));
		assertFalse(cacheMap.keySet().iterator().hasNext());

		cache.put("data1", "type", "key1");
		GenericKey equal = cacheMap.keySet().iterator().next();
		assertNotNull(map.remove(equal));
		assertFalse(cacheMap.keySet().iterator().hasNext());

		TestHelpers.EqualsTest(key1, equal, notEq);
		TestHelpers.HashCodeTest(key1, equal);
	}
}