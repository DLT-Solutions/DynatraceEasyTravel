package com.dynatrace.easytravel.launcher.procedures;

import java.util.concurrent.atomic.AtomicBoolean;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.*;
import com.dynatrace.easytravel.launcher.procedures.utils.VMwareUtils;
import com.dynatrace.easytravel.launcher.scenarios.ProcedureMapping;

public class PrepareVMwareProcedure extends AbstractProcedure {

	private final AtomicBoolean isRunning = new AtomicBoolean(false);
	private final EasyTravelConfig config = EasyTravelConfig.read();

	private final StringBuilder detailsBuilder;

	public PrepareVMwareProcedure(ProcedureMapping mapping) {
		super(mapping);
		detailsBuilder = new StringBuilder();
	}

	protected void doVmotion() throws Exception {
		VMwareUtils.callvMotionTask(config.vCenterHost, config.vCenterUser, config.vCenterPassword, config.vmName, config.resPool, config.fromHost, config.toHost);
	}

	@Override
	public Feedback run() {
		isRunning.set(true);
		initDetails();

		try {
			doVmotion();
			appendDetailsBuilder("Prepare VMware Success");
			} catch (Exception e) {
				return adjustFeedback(e.getMessage());
			} finally {
				isRunning.set(false);
		}

		return Feedback.Success;
	}

	protected Feedback adjustFeedback(String message) {
		if (message.contains("Not Found on source host.")) {
			appendDetailsBuilder(message);
			return Feedback.Neutral;
		} else {
			initDetail("Prepare VMware Failed", message);
			return Feedback.Failure;
		}
	}

	private void lineSeparator() {
		appendDetailsBuilder(System.getProperty("line.separator"));
	}

	protected void initDetail(String detailName, String detailValue) {
		appendDetailsBuilder(detailName);
		appendDetailsBuilder(": ");
		appendDetailsBuilder(detailValue);
		lineSeparator();
	}

	protected void initDetails() {
		clearInitDetails();
		initDetail("vCenterHost", config.vCenterHost);
		initDetail("VMname", config.vmName);
		initDetail("Resource Pool", config.resPool);
		initDetail("Source Host", config.fromHost);
		initDetail("Destination Host", config.toHost);
		lineSeparator();
	}

	protected void appendDetailsBuilder(String message) {
		detailsBuilder.append(message);
	}

	private void clearInitDetails() {
		detailsBuilder.setLength(0);
	}

	@Override
	public boolean isRunning() {
		return isRunning.get();
	}

	@Override
	public StopMode getStopMode() {
		return StopMode.PARALLEL;
	}

	@Override
	public boolean isStoppable() {
		return true;
	}

	@Override
	public Feedback stop() {
		return Feedback.Failure;
	}

	@Override
	public boolean isOperatingCheckSupported() {
		return false;
	}

	@Override
	public boolean isOperating() {
		throw new UnsupportedOperationException("Operating check not supported.");
	}

	@Override
	public boolean isSynchronous() {
		return true;
	}

	@Override
	public void addStopListener(StopListener stopListener) {
		// stop notifications not supported
	}

	@Override
	public void removeStopListener(StopListener stopListener) {
		// stop notifications not supported
	}

	@Override
	public void clearStopListeners() {
		// stop notifications not supported
	}

	@Override
	public String getDetails() {
		return detailsBuilder.toString();
	}

	@Override
	public String getLogfile() {
		return null;
	}

	@Override
	public boolean hasLogfile() {
		return false;
	}

	@Override
	public Technology getTechnology() {
		return null;
	}

	@Override
	public boolean agentFound() {
		return false;
	}
}


