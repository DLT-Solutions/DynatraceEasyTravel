package com.dynatrace.easytravel.launcher.procedures;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dynatrace.easytravel.launcher.engine.*;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;

public class VMotionProcedure extends PrepareVMwareProcedure {

	private final AtomicBoolean isRunning = new AtomicBoolean(false);
	private final AtomicBoolean isOperating = new AtomicBoolean(false);

	public VMotionProcedure(ProcedureMapping mapping) throws IllegalArgumentException {
		super(mapping);
	}

	@Override
	public Feedback run() {
		isRunning.set(true);
		initDetails();
		try {
			doVmotion();
			appendDetailsBuilder("vMotion Success");
			return Feedback.Success;
		} catch (Exception e) {
			return adjustFeedback(e.getMessage());
		}
	}

	@Override
	protected Feedback adjustFeedback(String message) {
		if (message.contains("Not Found on source host.")) {
			appendDetailsBuilder(message);
			return Feedback.Failure;
		} else {
			initDetail("vMotion Failed", message);
			return Feedback.Failure;
		}
	}

	@Override
	public boolean isOperating() {
		return isRunning.get();
	}

	@Override
	public boolean isSynchronous() {
		return false;
	}

	@Override
	public boolean isOperatingCheckSupported() {
		return true;
	}


	@Override
	public Feedback stop() {
		isOperating.set(false);
		return Feedback.Neutral;
	}
}
