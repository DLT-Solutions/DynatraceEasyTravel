package com.dynatrace.diagnostics.uemload;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;


abstract class AbstractUemAction implements UemAction {

	private static final Logger logger = Logger.getLogger(AbstractUemAction.class.getName());

	protected final JavaScriptAgent agent;
	private final Stopwatch start;
	private final long startTime;

	AbstractUemAction(JavaScriptAgent agent) {
		this.agent = Preconditions.checkNotNull(agent, "The passed JavaScriptAgent is not allowed to be null");
		start = Stopwatch.createStarted();
		startTime = System.currentTimeMillis();
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	@Override
	public long elapsedMillis() {
		return start.elapsed(TimeUnit.MILLISECONDS);
	}

	@Override
	public void sendActionPreview() {
		try {
			sendActionPreviewInternal();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not perform UEM action", e);
		}
	}

	@Override
	public void resetTimer() {
		start.reset();
		start.start();
	}

	protected abstract void sendActionPreviewInternal() throws IOException;

	@Override
	public int compareTo(UemAction otherAction) {
		// shortcut for identity match, there is a slight chance that the elapsedMillis() will return
		// different values if called twice in a row! We actually had this in unit test runs!
		if(this == otherAction) {
			return 0;
		}

		long thisVal = this.elapsedMillis();
		long anotherVal = otherAction.elapsedMillis();

		// greatest first
		return (thisVal > anotherVal ? -1 : (thisVal == anotherVal ? compareHashes(this.hashCode(), otherAction.hashCode()) : 1));
	}

	/**
	 * If two actions have the same elapsed time, we do not want that one is missing in the tree set
	 *
	 * @param firstActionHash
	 * @param secondActionHash
	 * @return
	 */
	private int compareHashes(Integer firstActionHash, Integer secondActionHash) {
		return firstActionHash.compareTo(secondActionHash);
	}


}
