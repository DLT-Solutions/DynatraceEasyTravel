package com.dynatrace.easytravel;

import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.model.DataAccess;

import ch.qos.logback.classic.Logger;

public abstract class HotDealProvider {

	private final RandomJourneyProvider randomJourneyProvider;
	private static final Logger LOGGER = LoggerFactory.make();

	protected HotDealProvider(DataAccess dataAccess) {
		this.randomJourneyProvider = new RandomJourneyProvider(dataAccess);
	}

	/**
	 * @return the randomJourneyProvider
	 */
	protected RandomJourneyProvider getRandomJourneyProvider() {
		LOGGER.debug("HOT DEAL DIAG ---------->>> plugin.HotDealServer / HotDealProvider class / getRandomJourneyProvider(): call randomJourneyProvider()");	
		return randomJourneyProvider;
	}

	public abstract void start();

	public abstract void close();
}
