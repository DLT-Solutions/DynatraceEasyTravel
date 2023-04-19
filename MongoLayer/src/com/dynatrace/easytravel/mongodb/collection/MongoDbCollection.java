/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: MongoDbCollection.java
 * @date: 13.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.mongodb.collection;

import static com.dynatrace.easytravel.mongodb.SimpleQueryBuilder.buildQuery;
import static java.lang.String.format;

import java.util.*;
import java.util.Map.Entry;

import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.mongodb.MongoConstants;
import com.dynatrace.easytravel.mongodb.MongoIndex;
import com.dynatrace.easytravel.mongodb.dbobject.MarshallingProvider;
import com.dynatrace.easytravel.mongodb.dbobject.MongoObjectMarshaller;
import com.dynatrace.easytravel.persistence.provider.EasyTravelPersistenceProvider;
import com.mongodb.*;

import ch.qos.logback.classic.Logger;


/**
 *
 * @author stefan.moschinski
 */
abstract class MongoDbCollection<OV extends Base> implements
		EasyTravelPersistenceProvider<OV> {

	private static final Logger log = LoggerFactory.make();

	protected static final DBObject NO_CONDITION = new BasicDBObject();
	private static final int NO_LIMIT = 0;
	private static final int NO_SKIPS = 0;

	private DB database;
	private DBCollection collection;

	private Collection<MongoIndex> indices;
	private MarshallingProvider<MongoObjectMarshaller<OV>> marshallingProvider;


	@SuppressWarnings("unchecked")
	MongoDbCollection(DB database, String collectionName, MongoIndex... indices) {
		this.database = database;
		this.marshallingProvider = MarshallingProviderFactory.createMarshallerInstance(this.getClass());
		this.indices = Arrays.asList(indices);
		configureCollection(collectionName);
	}

	@Override
	public OV add(OV value) {
		DBObject marshal = marshallingProvider.createMarshaller().marshal(value);
		try {
			collection.insert(marshal);
		} catch (com.mongodb.DuplicateKeyException e) {
			log.info(format("A '%s' exception happened, updating instead of adding the value '%s'", e.getClass().getSimpleName(),
					value));

			Object id = marshal.get(MongoConstants.ID);
			if (id == null)
				throw new RuntimeException(
						format("Got a '%s' exception, but could not find '_id'", e.getClass().getSimpleName()), e);

			update(buildQuery(MongoConstants.ID).value(id).create(), value);
		}
		return value;
	}

	public OV update(DBObject query, OV value) {
		collection.update(query, buildQuery("$set").value(marshallingProvider.createMarshaller().marshal(value, false)).create(),
				true,
				true);
		return value;
	}

	@Override
	public OV update(OV value) {
		DBObject marshal = marshallingProvider.createMarshaller().marshal(value);
		Object id = marshal.get(MongoConstants.ID);
		if (id == null)
			throw new RuntimeException(
					format("Unconditional MongoDB updates are not supported, could not find '_id' for value '%s'", value));
		return update(buildQuery(MongoConstants.ID).value(id).create(), value);
	}

	public WriteResult delete(DBObject query) {
		return collection.remove(query);
	}

	private void configureCollection(String collectionName) {
		this.collection = database.getCollection(collectionName); // creates implicitly collection
		collection.setObjectClass(marshallingProvider.getMarshallerClass());

		setInternalMarshallers(collection, marshallingProvider.getSubMarshallerMapping());
		ensureIndices(collection, indices);
	}

	private void ensureIndices(DBCollection collection, Iterable<MongoIndex> indices) {
		for (MongoIndex index : indices) {
			collection.createIndex(index.getKey(), index.getOptions());
		}
	}

	private void setInternalMarshallers(DBCollection collection,
			Map<String, Class<? extends MongoObjectMarshaller<? extends Base>>> internalMarshallers) {
		Set<Entry<String, Class<? extends MongoObjectMarshaller<? extends Base>>>> entries = internalMarshallers.entrySet();
		for (Entry<String, Class<? extends MongoObjectMarshaller<? extends Base>>> entry : entries) {
			collection.setInternalClass(entry.getKey(), entry.getValue());
		}
	}

	protected OV findOneByKeyValue(String key, Object value) {
		DBObject query = new BasicDBObject(key, value);
		return findOne(query);
	}

	protected Collection<OV> findByKeyValue(String key, Object value) {
		DBObject query = new BasicDBObject(key, value);
		return find(query);
	}
	
	@SuppressWarnings("unchecked")
	protected Collection<OV> findLatest(int limit) {
		DBCursor cursor = collection.find().sort(new BasicDBObject("x", 1)).limit(limit);
		Collection<OV> items = new ArrayList<OV>(cursor.size());
		while (cursor.hasNext()) {
			items.add(((MongoObjectMarshaller<? extends OV>) cursor.next()).unmarshal());
		}
		return items;
	}

	@SuppressWarnings("unchecked")
	OV findOne(DBObject query) {
		MongoObjectMarshaller<? extends OV> found = (MongoObjectMarshaller<? extends OV>) collection.findOne(query);
		if (found == null)
			return null;

		return found.unmarshal();
	}

	Collection<OV> find(DBObject query) {
		return find(query, NO_LIMIT);
	}

	Collection<OV> find(DBObject query, int limit) {
		return find(query, NO_SKIPS, limit);
	}

	@SuppressWarnings("unchecked")
	Collection<OV> find(DBObject query, int elementsToSkip, int limit) {
		DBCursor cursor = collection.find(query).skip(elementsToSkip).limit(limit);
		Collection<OV> items = new ArrayList<OV>(cursor.size());
		while (cursor.hasNext()) {
			items.add(((MongoObjectMarshaller<? extends OV>) cursor.next()).unmarshal());
		}
		return items;
	}

	@Override
	public Collection<OV> getAll() {
		return find(NO_CONDITION);
	}

	@Override
	public Collection<OV> getWithLimit(int limit) {
		return find(NO_CONDITION, limit);
	}

	@Override
	public int getCount() {
		return getSelectiveCount(NO_CONDITION);
	}

	/**
	 * Counts the elements matching the query in the collection
	 *
	 * @param query conditions that must be matched by the items that are counted
	 * @return count of the items that match the condition
	 */
	protected int getSelectiveCount(DBObject query) {
		return (int) collection.getCount(query);
	}

	DBCollection getCollection() {
		return collection;
	}

	protected AggregationOutput aggregate(DBObject firstOp, DBObject... additionalOps) {
		return collection.aggregate(firstOp, additionalOps);
	}

	protected Collection<DBObject> find(DBObject ref, DBObject keys) {
		DBCursor find = collection.find(ref, keys);
		return find.toArray();
	}

	@Override
	public void reset() {
		collection.drop();
		configureCollection(collection.getName());
	}

}
