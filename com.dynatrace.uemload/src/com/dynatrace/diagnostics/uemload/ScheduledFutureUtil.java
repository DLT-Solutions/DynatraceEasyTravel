package com.dynatrace.diagnostics.uemload;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ScheduledFutureUtil {

	private static final Logger log = Logger.getLogger(ScheduledFutureUtil.class.getName());

	private static final int INCREMENT_MILLIS = 250;
	private static final int TIMEOUT_MILLIS = 3000;

	static boolean stop(List<ScheduledFuture<?>> currentVisits) {
		return stop(currentVisits, false); // interrupt threads
	}

	static boolean stop(List<ScheduledFuture<?>> currentVisits, boolean mayInterruptIfRunning) {
		if (currentVisits != null) {
			for (ScheduledFuture<?> future : currentVisits) {
				future.cancel(mayInterruptIfRunning);
			}
			return waitForTermination(currentVisits, INCREMENT_MILLIS, TIMEOUT_MILLIS);
		}
		return true;
	}

	static boolean waitForTermination(List<ScheduledFuture<?>> currentVisits, long increment, long timeout) {
		for (int i = 0; i <= timeout; i += increment) {
			if (areAllSchedulesFinished(currentVisits)) {
				return true;
			}
			waitInMillis(increment);
		}
		return false;
	}

	private static boolean areAllSchedulesFinished(List<ScheduledFuture<?>> currentVisits) {
		boolean allEnded = true;
		for (ScheduledFuture<?> future : currentVisits) {
			allEnded = allEnded && !isRunning(future);
		}
		return allEnded;
	}

	private static void waitInMillis(long waiInMillis) {
		try {
			Thread.sleep(waiInMillis);
		} catch (InterruptedException e) {
			log.log(Level.WARNING, "Waiting for ScheduledFutures to terminate has been interrupted", e);
		}
	}

	private static boolean isRunning(ScheduledFuture<?> future) {
		return !future.isDone() && future.getDelay(TimeUnit.MILLISECONDS) <= 0;
	}
}
