/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MongoObjectMarshaller.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.dbobject;

import static com.dynatrace.easytravel.jpa.Base.DATE_CREATED;

import java.util.Collections;
import java.util.Map;

import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import ch.qos.logback.classic.Logger;

/**
 *
 * @author stefan.moschinski
 */
public abstract class MongoObjectMarshaller<T extends Base> extends BasicDBObject {

	private static final Logger log = LoggerFactory.make();
	private static final long serialVersionUID = 1L;
	private final Map<String, ? extends Class<? extends MongoObjectMarshaller<? extends Base>>> subMarshaller;


	public MongoObjectMarshaller(Map<String, Class<? extends MongoObjectMarshaller<? extends Base>>> subMarshaller) {
		this.subMarshaller = subMarshaller;
	}

	public MongoObjectMarshaller() {
		this(Collections.<String, Class<? extends MongoObjectMarshaller<? extends Base>>> emptyMap());
	}

	public final DBObject marshal(T value) {
		return marshal(value, true);
	}

	public final DBObject marshal(T value, boolean withId) {
		log.debug("Marshalling: " + value + (withId ? "with" : "without") + " id");
		put(DATE_CREATED, value.getCreated());
		return marshalTypeSpecific(value, withId);
	}

	public final T unmarshal() {
		T value = unmarshalTypeSpecific();
		value.setCreated(getDate(DATE_CREATED));
		log.debug("Unmarshalled: " + value);
		return value;
	}

	protected abstract DBObject marshalTypeSpecific(T value, boolean withId);

	protected abstract T unmarshalTypeSpecific();

	Map<String, Class<? extends MongoObjectMarshaller<? extends Base>>> getSubMarshallerMapping() {
		return Collections.unmodifiableMap(subMarshaller);
	}

	abstract MongoObjectMarshaller<T> newInstance();

}
