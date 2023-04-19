package com.dynatrace.easytravel.util;

interface ServiceStubStrategy
{
	public <T> T getServiceStub(Class<T> clazz) throws Exception;

	public <T> void returnServiceStub(T stub) throws Exception;

	public <T> void invalidateServiceStub(T stub) throws Exception;

	public void clear();
}