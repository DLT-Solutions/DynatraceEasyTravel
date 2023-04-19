package com.dynatrace.easytravel.util;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;


class GlobalPoolStrategy implements ServiceStubStrategy
{
	private final GenericKeyedObjectPool<Class<?>, Object> pool = new GenericKeyedObjectPool<Class<?>, Object>(new BaseKeyedPoolableObjectFactory<Class<?>, Object>() {
		@Override
		public Object makeObject(Class<?> clazz) throws Exception {
			return ServiceStubFactory.makeStub(clazz, ServiceStubFactory.getServiceName(clazz));
		}
	});

	public GlobalPoolStrategy() {
		super();
		pool.setWhenExhaustedAction(GenericKeyedObjectPool.WHEN_EXHAUSTED_GROW);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getServiceStub(Class<T> clazz) throws Exception {
		return (T) pool.borrowObject(clazz);
	}

	@Override
	public <T> void returnServiceStub(T stub) throws Exception {
		if (stub != null) {
			pool.returnObject(stub.getClass(), stub);
		}
	}

	@Override
	public <T> void invalidateServiceStub(T stub) throws Exception {
		if(stub != null) {
			pool.invalidateObject(stub.getClass(), stub);
		}
	}

	@Override
	public void clear() {
		pool.clear();
	}
}