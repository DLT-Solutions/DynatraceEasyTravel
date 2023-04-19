/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: UserMongo.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.dbobject;

import com.dynatrace.easytravel.jpa.business.Location;
import com.dynatrace.easytravel.mongodb.MongoConstants;

/**
 *
 * @author stefan.moschinski
 */
public class LocationMongoMarshaller extends MongoObjectMarshaller<Location> {

	private static final long serialVersionUID = 1L;

	@Override
	protected LocationMongoMarshaller marshalTypeSpecific(Location location, boolean withId) {
		if (withId)
			put(MongoConstants.ID, location.getName());
		return this;
	}

	@Override
	protected Location unmarshalTypeSpecific() {
		Location location = new Location();
		location.setName(getString(MongoConstants.ID));
		return location;
	}

	@Override
	public MongoObjectMarshaller<Location> newInstance() {
		return new LocationMongoMarshaller();
	}
}
