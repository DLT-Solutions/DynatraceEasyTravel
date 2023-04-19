package com.dynatrace.easytravel.util;

import org.apache.axis2.client.Stub;
import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.logging.LoggerFactory;

/**
 * Class for getting AXIS-generated service stubs using the
 * EasyTravelConfig.
 *
 * Extended this class to support the ServiceStubStrategy enum constants of EasyTravelConfig.
 *
 * @author philipp.grasboeck
 */
public class ServiceStubProvider {
	private static final Logger log = LoggerFactory.make();

	private static final ServiceStubStrategy STRATEGY;

	static {
		EasyTravelConfig config = EasyTravelConfig.read();
		STRATEGY = getServiceStubStrategy(config.serviceStubStrategy);

		CallbackRunnable.CLEANUP.add(new Runnable() {
			@Override
			public void run() {
				if (log.isInfoEnabled()) log.info("Clearing stub provider...");
				STRATEGY.clear();
			}
		});
	}

	private static ServiceStubStrategy getServiceStubStrategy(EasyTravelConfig.ServiceStubStrategy strategy) {
		switch (strategy) {
			case alwaysCreate:       return new AlwaysCreateStrategy();
			case globalCache:        return new GlobalCacheStrategy();
			case threadLocalCache:   return new ThreadLocalCacheStrategy();
			case threadGlobalCache:  return new ThreadGlobalCacheStrategy();
			case globalPool:         return new GlobalPoolStrategy();
		}
		throw new IllegalStateException("No implementation for serviceStubStrategy: " + strategy);
	}

	public static <T extends Stub> T getServiceStub(Class<T> clazz)
	{
		try {
			return STRATEGY.getServiceStub(clazz);
		} catch (Exception e) {
			log.error("Cannot get service stub for class: " + clazz);
			throw new Error(e);
		}
	}

	public static <T extends Stub> void returnServiceStub(T stub) {
		try {
			stub._getServiceClient().cleanupTransport();

			STRATEGY.returnServiceStub(stub);
		} catch (Exception e) {
			log.error("Cannot return service stub: " + stub);
			throw new Error(e);
		}
	}

	public static <T extends Stub> void invalidateServiceStub(T stub) {
		try {
			STRATEGY.invalidateServiceStub(stub);
		} catch (Exception e) {
			log.error("Cannot invalidate service stub: " + stub);
			throw new Error(e);
		}
	}

	public static void clear() {
		STRATEGY.clear();
	}
}
