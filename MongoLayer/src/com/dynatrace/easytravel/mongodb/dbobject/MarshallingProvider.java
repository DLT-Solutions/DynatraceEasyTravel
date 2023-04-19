/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MarshallingProvider.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.dbobject;

import java.util.Map;

import com.dynatrace.easytravel.jpa.Base;



/**
 *
 * @author stefan.moschinski
 */
public class MarshallingProvider<T extends MongoObjectMarshaller<? extends Base>> {

	private final T marshaller;

	/**
	 * 
	 * @author stefan.moschinski
	 */
	public MarshallingProvider(T marshaller) {
		this.marshaller = marshaller;
	}

	@SuppressWarnings("unchecked")
	public T createMarshaller() {
		return (T) this.marshaller.newInstance();
	}

	@SuppressWarnings("unchecked")
	public Class<T> getMarshallerClass() {
		return (Class<T>) this.marshaller.getClass();
	}

	/**
	 * 
	 * @return
	 * @author stefan.moschinski
	 */
	public Map<String, Class<? extends MongoObjectMarshaller<? extends Base>>> getSubMarshallerMapping() {
		return this.marshaller.getSubMarshallerMapping();
	}
}