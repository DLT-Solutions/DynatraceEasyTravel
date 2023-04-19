package com.dynatrace.diagnostics.uemload.openkit.time;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RealTimeService implements TimeService {
	private static final Logger logger = Logger.getLogger(RealTimeService.class.getName());

	@Override
	public void waitForDuration(long duration) {
		if (duration <= 0)
			return;
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "Event thread interrupted while waiting", e);
			Thread.currentThread().interrupt();
		}
	}
}
