/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: TenantMongoMarshaller.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.dbobject;

import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_AMOUNT;
import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_DESC;
import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_DESTINATION;
import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_FROM_DATE;
import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_NAME;
import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_PICTURE;
import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_START;
import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_TENANT;
import static com.dynatrace.easytravel.jpa.business.Journey.JOURNEY_TO_DATE;

import java.util.Map;

import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.mongodb.MongoConstants;
import com.google.common.collect.Maps;
import com.mongodb.DBObject;

/**
 *
 * @author stefan.moschinski
 */
public class JourneyMongoMarshaller extends MongoObjectMarshaller<Journey> {

	private static final long serialVersionUID = 1L;

	private static Map<String, Class<? extends MongoObjectMarshaller<? extends Base>>> JOURNEY_SUB_MARSHALLER;

	static {
		JOURNEY_SUB_MARSHALLER = Maps.newHashMapWithExpectedSize(1);
		JOURNEY_SUB_MARSHALLER.put(JOURNEY_TENANT, TenantMongoMarshaller.class);
		JOURNEY_SUB_MARSHALLER.put(JOURNEY_START, LocationMongoMarshaller.class);
		JOURNEY_SUB_MARSHALLER.put(JOURNEY_DESTINATION, LocationMongoMarshaller.class);
	}


	/**
	 * 
	 * @author stefan.moschinski
	 */
	public JourneyMongoMarshaller() {
		super(JOURNEY_SUB_MARSHALLER);
	}

	@Override
	protected DBObject marshalTypeSpecific(Journey journey, boolean withId) {
		if (withId)
			put(MongoConstants.ID, journey.getId());
		put(JOURNEY_NAME, journey.getName());
		put(JOURNEY_AMOUNT, journey.getAmount());
		put(JOURNEY_TENANT, new TenantMongoMarshaller().marshal(journey.getTenant()));
		put(JOURNEY_START, new LocationMongoMarshaller().marshal(journey.getStart()));
		put(JOURNEY_DESTINATION, new LocationMongoMarshaller().marshal(journey.getDestination()));
		put(JOURNEY_FROM_DATE, journey.getFromDate());
		put(JOURNEY_TO_DATE, journey.getToDate());
		put(JOURNEY_DESC, journey.getDescription());
		put(JOURNEY_PICTURE, journey.getPicture());
		return this;
	}


	@Override
	protected Journey unmarshalTypeSpecific() {
		Journey journey = new Journey();
		journey.setId(getInt(MongoConstants.ID));
		journey.setName(getString(JOURNEY_NAME));
		journey.setAmount(getDouble(JOURNEY_AMOUNT));
		journey.setFromDate(getDate(JOURNEY_FROM_DATE));
		journey.setToDate(getDate(JOURNEY_TO_DATE));
		journey.setDescription(getString(JOURNEY_DESC));
		journey.setPicture((byte[]) get(JOURNEY_PICTURE));

		journey.setStart(((LocationMongoMarshaller) get(JOURNEY_START)).unmarshal());
		journey.setDestination(((LocationMongoMarshaller) get(JOURNEY_DESTINATION)).unmarshal());
		journey.setTenant(((TenantMongoMarshaller) get(JOURNEY_TENANT)).unmarshal());
		return journey;
	}


	@Override
	public MongoObjectMarshaller<Journey> newInstance() {
		return new JourneyMongoMarshaller();
	}

}
