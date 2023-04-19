/***************************************************
 * dynaTrace Diagnostics (c) dynaTrace software GmbH
 *
 * @file: NullEasytravelPersistenceProvider.java
 * @date: 08.01.2013
 * @author: stefan.moschinski
 */
package com.dynatrace.easytravel.persistence.provider.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.dynatrace.easytravel.jpa.Base;
import com.dynatrace.easytravel.persistence.provider.EasyTravelPersistenceProvider;
import com.google.common.collect.Lists;


/**
 *
 * @author stefan.moschinski
 */
public class InMemoryEasytravelPersistenceProvider<T extends Base> implements EasyTravelPersistenceProvider<T> {

	
	private Set<T> values;

	/**
	 * 
	 * @author stefan.moschinski
	 */
	public InMemoryEasytravelPersistenceProvider() {
		this.values = new HashSet<T>();
	}
	

	@Override
	public T add(T value) {
		values.add(value);
		return value;
	}

	@Override
	public T update(T value) {
		values.add(value);
		return value;
	}

	@Override
	public Collection<T> getAll() {
		return Collections.unmodifiableCollection(values);
	}

	@Override
	public Collection<T> getWithLimit(int limit) {
		int count = limit > values.size() ? values.size() : limit;
		return Collections.unmodifiableCollection(Lists.newArrayList(values).subList(0, count));
	}

	@Override
	public int getCount() {
		return values.size();
	}

	protected boolean delete(T value) {
		return values.remove(value);
	}



	/**
	 * @return the values
	 */
	protected Set<T> getValues() {
		return values;
	}


	@Override
	public void reset() {
		values.clear();
	}


}
