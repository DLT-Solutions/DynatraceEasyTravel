/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: JpaProvider.java
 * @date: 18.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider;

import static java.lang.String.format;

import java.util.Collection;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.NotImplementedException;

import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.persistence.JpaDatabaseController;

import ch.qos.logback.classic.Logger;

/**
 *
 * @author stefan.moschinski
 */
abstract class JpaProvider<T extends Base> implements EasyTravelPersistenceProvider<T> {

	private static final Logger log = LoggerFactory.make();

	private final Class<T> persistenceCls;
	private JpaDatabaseController controller;

	JpaProvider(JpaDatabaseController controller, Class<T> cls) {
		this.persistenceCls = cls;
		this.controller = controller;
	}

	@Override
	public T add(T value) {
		controller.persist(value);
		log.info("Added value: " + value); // toString() will load lazy fields!
		return value;
	}

	@Override
	public T update(T value) {
		controller.merge(value);
		log.info("Updated value: " + value); // toString() will load lazy fields!
		return value;
	}

	T find(Object value) {
		return controller.find(persistenceCls, value);
	}

	<X> X find(Class<X> persistenceCls, Object value) {
		return controller.find(persistenceCls, value);
	}

	void remove(Object entity) {
		controller.remove(entity);
	}


	@Override
	public Collection<T> getAll() {
		return controller.getByNamedQuery(persistenceCls.getSimpleName() + ".all");
	}

	@Override
	public Collection<T> getWithLimit(int limit) {
		return controller.getByNamedQuery(persistenceCls.getSimpleName() + ".all", limit);
	}

	@Override
	public int getCount() {
		String query = format("select count(j) from %s j", persistenceCls.getSimpleName());
		return controller.getCount(query);
	}

	protected <X> TypedQuery<X> createQuery(String queryText, Class<X> clazz) {
		return controller.createQuery(queryText, clazz);
	}

	protected Query createQuery(String queryText) {
		return controller.createQuery(queryText);
	}

	protected TypedQuery<T> createNamedQuery(String queryName) {
		return createNamedQuery(queryName, persistenceCls);
	}

	protected <K> TypedQuery<K> createNamedQuery(String queryName, Class<K> clazz) {
		return controller.createNamedQuery(queryName, clazz);
	}

	protected Query createNativeQuery(String queryStr) {
		return controller.createNativeQuery(queryStr);
	}

	protected void flush() {
		controller.flush();
	}

	@Override
	public void reset() {
		throw new NotImplementedException("To be implemented");
	}
}
