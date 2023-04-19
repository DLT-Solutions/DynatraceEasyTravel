/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: JpaDatabaseController.java
 * @date: 19.12.2012
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.persistence.controller.DatabaseController;

import ch.qos.logback.classic.Logger;


/**
 *
 * @author stefan.moschinski
 */
public class JpaDatabaseController implements DatabaseController {

	private static final Logger log = LoggerFactory.make();

	protected EntityManager em;

	JpaDatabaseController() {
		// constructor for spring subclass
	}

	/**
	 *
	 * @param em
	 * @author stefan.moschinski
	 */
	public JpaDatabaseController(EntityManager em) {
		this.em = em;
	}

	@Override
	public synchronized void startTransaction() {
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
	}

	@Override
	public void commitTransaction() {
		em.getTransaction().commit();
	}

	@Override
	public void rollbackTransaction() {
		em.getTransaction().rollback();
	}

	@Override
	public synchronized void close() {
		if (em != null) {
			if (em.isOpen())
				em.close();
			em = null;
		}
	}

	@Override
	public void flush() {
		em.flush();
	}

	@Override
	public void flushAndClear() {
		em.flush();
		em.clear();
	}

	public void evictCache() {
		Session session = (Session) em.getDelegate();
		SessionFactory factory = session.getSessionFactory();
		factory.getCache().evictQueryRegions();
		factory.getCache().evictEntityRegions();
		factory.getStatistics().clear();

		log.info("Evicted Hibernate Query Cache and Entity Cache.");
	}


	public void persist(Object value) {
		em.persist(value);
	}

	public void merge(Object value) {
		em.merge(value);
	}

	public void remove(Object entity) {
		log.info("Entity " + entity + " deleted");
		em.remove(entity);
	}

	public <T> T find(Class<T> persistenceCls, Object value) {
		return em.find(persistenceCls, value);
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<T> getByNamedQuery(String namedQuery) {
		return em.createNamedQuery(namedQuery).getResultList();
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<T> getByNamedQuery(String namedQuery, int limit) {
		return em.createNamedQuery(namedQuery).setMaxResults(limit).getResultList();
	}

	public int getCount(String query) {
		return em.createQuery(query, Long.class).getSingleResult().intValue();
	}

	public <X> TypedQuery<X> createQuery(String queryText, Class<X> cls) {
		return interceptQuery(em.createQuery(queryText, cls));
	}

	public Query createQuery(String queryText) {
		return interceptQuery(em.createQuery(queryText));
	}

	public <T> TypedQuery<T> createNamedQuery(String queryName, Class<T> cls) {
		return interceptQuery(getActualTypedQuery(queryName, cls));
	}

	// necessary as PluginAwareJpaDatabaseController overrides getActualQuery() and thus we
	// cannot easily use TypedQuery in getActualQuery() as there is no TypedQuery for
	// native queries!
	protected <T> TypedQuery<T> getActualTypedQuery(String queryName, Class<T> cls) {
		return em.createNamedQuery(queryName, cls);
	}

	protected Query getActualQuery(String queryName, Class<?> cls) {
		return em.createNamedQuery(queryName, cls);
	}

	protected Query createNativeQuery(String queryName, Class<?> cls) {
		return em.createNativeQuery(queryName, cls);
	}

	public Query createNativeQuery(String queryName) {
		return em.createNativeQuery(queryName);
	}

	private <T> TypedQuery<T> interceptQuery(TypedQuery<T> query) {
		doIntercept(query);
		return query;
	}

	private Query interceptQuery(Query query) {
		doIntercept(query);
		return query;
	}

	protected void doIntercept(Query query) {
		query.setHint("org.hibernate.cacheable", true);
	}



	/**
	 * Maybe we should delete the tests that use it...
	 *
	 * @return the em
	 */
	public EntityManager getEm() {
		return em;
	}


	@Override
	public void dropContents() {
//		em.
	}

}
