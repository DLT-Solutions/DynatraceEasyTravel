package com.dynatrace.easytravel.launcher.sync;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.logging.LoggerFactory;

/**
 * Utility class to wait until a certain condition (predication) is fulfilled.
 *
 * @param <PredicateSubjectType> the type of the evaluation subject
 * @author martin.wurzinger
 */
public class PredicateMatcher<PredicateSubjectType> {
    private static final Logger LOGGER = LoggerFactory.make();

    private final PredicateSubjectType subject;
    private final int timeoutMs;
    private final int intervalMs;

    public PredicateMatcher(PredicateSubjectType subject, int timeoutMs, int intervalMs) {
        this.subject = subject;
        this.timeoutMs = timeoutMs;
        this.intervalMs = intervalMs;
    }

	/**
	 * @param predicate the predicate to wait until it evaluates to <code>true</code> or the timeout is reached
	 * @return <code>true</code> if predicate has finally matched or returns <code>false</code> if waiting for a
	 *         a complying predicate failed (timeout)
	 * @throws InterruptedException
	 * @author martin.wurzinger
	 */
    public boolean waitForMatch(Predicate<PredicateSubjectType> predicate) {
        final long deadline = System.currentTimeMillis() + timeoutMs;

        boolean isPredicateMatching = false;

        LOGGER.debug("Waiting up to " + (timeoutMs/1000) + " seconds for predicate '" + predicate.getClass().getSimpleName() + "' to match.");

        try {
            while (!isPredicateMatching && System.currentTimeMillis() < deadline) {
            	if(predicate.shouldStop()) {
            		LOGGER.info("Waiting for Predicate '" + predicate.getClass().getSimpleName() + "' was interrupted.");
            		return true;
            	}

            	isPredicateMatching = predicate.eval(subject);

                if (!isPredicateMatching) {
                    Thread.sleep(intervalMs);
                }
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Waiting for a matching predicate was interrupted.", e);
            return false;
        }

        LOGGER.debug("Had result: " + isPredicateMatching + " when waiting for predicate '" + predicate.getClass().getSimpleName() + "' to match.");

        return isPredicateMatching;
    }

    public boolean waitForNotMatch(Predicate<PredicateSubjectType> predicate) {
        final long deadline = System.currentTimeMillis() + timeoutMs;

        boolean isPredicateMatching = true;

        try {
            while (isPredicateMatching && System.currentTimeMillis() < deadline) {
            	if(predicate.shouldStop()) {
            		LOGGER.info("Waiting for Predicate '" + predicate.getClass().getSimpleName() + "' was interrupted.");
            		return true;
            	}

                isPredicateMatching = predicate.eval(subject);

                if (isPredicateMatching) {
                    Thread.sleep(intervalMs);
                }
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Waiting for a not matching predicate was interrupted.", e);
            return false;
        }

        return isPredicateMatching;
    }

}
