package com.dynatrace.easytravel.launcher.engine;

import java.util.Arrays;
import java.util.List;


/**
 * Allows to combine multiple listeners into one.
 *
 * @author dominik.stadler
 *
 */
public class ProcedureStateMultiplexer implements ProcedureStateListener {
	private final List<ProcedureStateListener> listeners;

	public ProcedureStateMultiplexer(final List<ProcedureStateListener> listeners) {
		this.listeners = listeners;
	}

	public ProcedureStateMultiplexer(final ProcedureStateListener ... listeners) {
		this.listeners = Arrays.asList(listeners);
	}

	@Override
	public void notifyProcedureStateChanged(final StatefulProcedure subject,
			final State oldState, final State newState) {
		for(ProcedureStateListener listener : listeners) {
			listener.notifyProcedureStateChanged(subject, oldState, newState);
		}
	}
}
