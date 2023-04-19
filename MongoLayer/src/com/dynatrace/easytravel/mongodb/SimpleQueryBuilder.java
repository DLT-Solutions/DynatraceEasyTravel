/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: Query.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


/**
 *
 * @author stefan.moschinski
 */
public class SimpleQueryBuilder {

	public static SimpleQueryBuilder buildQuery(String keypath, String... subkeypaths) {
		return new SimpleQueryBuilder(keypath, subkeypaths);
	}

	private final String key;
	private Object value;

	private SimpleQueryBuilder(String keypath, String... subkeypaths) {
		this.key = KeypathFactory.createKeypath(keypath, subkeypaths);
	}

	public SimpleQueryBuilder value(Object value) {
		this.value = value;
		return this;
	}

	public DBObject create() {
		return new BasicDBObject(key, value);
	}
}
