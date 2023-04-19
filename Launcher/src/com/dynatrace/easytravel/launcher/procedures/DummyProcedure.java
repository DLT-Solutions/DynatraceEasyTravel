package com.dynatrace.easytravel.launcher.procedures;

import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.AbstractProcedure;
import com.dynatrace.easytravel.launcher.engine.Feedback;
import com.dynatrace.easytravel.launcher.engine.StopListener;
import com.dynatrace.easytravel.launcher.engine.StopMode;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;

/**
 * Simple NoOp Procedure used in Tests and for cases where we cannot create
 * the procedure at all.
 *
 * @author cwat-dstadler
 */
public class DummyProcedure extends AbstractProcedure {
	private final Feedback feedback;
	private final String details;

	public DummyProcedure(ProcedureMapping mapping, Feedback feedback, String details) throws IllegalArgumentException {
		super(mapping);
		this.feedback = feedback;
		this.details = details;
	}

	@Override
	public Feedback run() {
		return feedback;
	}

	@Override
	public boolean isStoppable() {
		return false;
	}

	@Override
	public StopMode getStopMode() {
		return StopMode.NONE;
	}

	@Override
	public Feedback stop() {
		throw new UnsupportedOperationException("Stop should not be called on DummyProcedure");
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public boolean isOperatingCheckSupported() {
		return false;
	}

	@Override
	public boolean isOperating() {
		return false;
	}

	@Override
	public String getDetails() {
		return details;
	}

	@Override
	public boolean hasLogfile() {
		return false;
	}

	@Override
	public String getLogfile() {
		return null;
	}

	@Override
	public boolean agentFound() {
		return false;
	}

	@Override
	public Technology getTechnology() {
		return null;
	}

	@Override
	public void addStopListener(StopListener stopListener) {
	}

	@Override
	public void removeStopListener(StopListener stopListener) {
	}

	@Override
	public void clearStopListeners() {
	}
}
