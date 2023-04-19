package com.dynatrace.easytravel.util;


class AlwaysCreateStrategy implements ServiceStubStrategy
{
	@Override
	public <T> T getServiceStub(Class<T> clazz) throws Exception {
		return ServiceStubFactory.makeStub(clazz, ServiceStubFactory.getServiceName(clazz));
	}

	@Override
	public <T> void returnServiceStub(T stub) throws Exception {
	}

	@Override
	public <T> void invalidateServiceStub(T stub) throws Exception {
	}

	@Override
	public void clear() {
	}
}