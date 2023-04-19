/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 * 
 * @file: Index.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


/**
 * 
 * @author stefan.moschinski
 */
public class MongoIndex {

	private final Map<String, Object> options;
	private final Set<String> keys;

	private MongoIndex(Collection<String> keys) {
		this.keys = Sets.newHashSet(keys);
		this.options = Maps.newHashMapWithExpectedSize(0);
	}

	public static MongoIndex createCompoundIndex(String key1, String key2, String... otherKeys) {
		Collection<String> keys = Sets.newHashSet(key1, key2);
		keys.addAll(Arrays.asList(otherKeys));
		return new MongoIndex(keys);
	}


	public static MongoIndex createIndex(String keyname, String... subs) {
		return new MongoIndex(Collections.singleton(KeypathFactory.createKeypath(keyname, subs)));
	}

	public MongoIndex uniqueKey() {
		return setTrue("unique");
	}

	public MongoIndex dropDuplicates() {
		return setTrue("dropDups");
	}

	private MongoIndex setTrue(String name) {
		options.put(name, Boolean.TRUE);
		return this;
	}

	/**
	 * 
	 * @author stefan.moschinski
	 */
	public DBObject getKey() {
		BasicDBObject indices = new BasicDBObject();
		for (String key : keys) {
			indices.put(key, 1);
		}

		return indices;
	}

	public DBObject getOptions() {
		BasicDBObject options = new BasicDBObject();
		for (Entry<String, Object> opt : this.options.entrySet()) {
			options.append(opt.getKey(), opt.getValue());
		}

		return options;
	}


}
