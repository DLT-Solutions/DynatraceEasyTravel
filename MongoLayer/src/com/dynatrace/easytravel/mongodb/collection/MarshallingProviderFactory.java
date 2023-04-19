/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MarshallerFactory.java
 * @date: 09.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.collection;

import static java.lang.String.format;

import java.util.Map;

import org.apache.commons.lang3.SerializationException;

import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.mongodb.dbobject.*;
import com.google.common.collect.Maps;


/**
 *
 * @author stefan.moschinski
 */
public class MarshallingProviderFactory {

	final static Map<Class<? extends MongoDbCollection<? extends Base>>, Class<? extends MongoObjectMarshaller<? extends Base>>> mapping;

	static {
		mapping = Maps.newHashMap();
		mapping.put(BookingCollection.class, BookingMongoMarshaller.class);
		mapping.put(JourneyCollection.class, JourneyMongoMarshaller.class);
		mapping.put(LocationCollection.class, LocationMongoMarshaller.class);
		mapping.put(LoginHistoryCollection.class, LoginHistoryMongoMarshaller.class);
		mapping.put(ScheduleCollection.class, ScheduleMongoMarshaller.class);
		mapping.put(TenantCollection.class, TenantMongoMarshaller.class);
		mapping.put(UserCollection.class, UserMongoMarshaller.class);
	}

	@SuppressWarnings("unchecked")
	static <T extends Base, X extends MongoDbCollection<T>> MarshallingProvider<MongoObjectMarshaller<T>> createMarshallerInstance(
			Class<X> cls) {
		try
		{
			return new MarshallingProvider<MongoObjectMarshaller<T>>((MongoObjectMarshaller<T>) mapping.get(cls).newInstance());
		} catch (Exception e)
		{
			throw new SerializationException(format("Could not create an instance for class '%s", cls), e);
		}
	}

}
