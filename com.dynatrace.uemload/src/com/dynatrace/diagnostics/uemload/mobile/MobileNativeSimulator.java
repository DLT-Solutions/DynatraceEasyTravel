package com.dynatrace.diagnostics.uemload.mobile;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import ch.qos.logback.classic.Logger;

import com.dynatrace.diagnostics.uemload.Action;
import com.dynatrace.diagnostics.uemload.Bandwidth;
import com.dynatrace.diagnostics.uemload.ExtendedCommonUser;
import com.dynatrace.diagnostics.uemload.Location;
import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.UEMLoadCallback;
import com.dynatrace.diagnostics.uemload.UEMLoadScenario;
import com.dynatrace.diagnostics.uemload.UemLoadScheduler;
import com.dynatrace.diagnostics.uemload.Visit;
import com.dynatrace.diagnostics.uemload.VisitorInfo;
import com.dynatrace.diagnostics.uemload.http.exception.ExceptionHandler;
import com.dynatrace.diagnostics.uemload.scenarios.easytravel.mobile.MobileVisit;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.DtVersionDetector;

public class MobileNativeSimulator extends Simulator {
	private static final boolean IS_RUXIT = DtVersionDetector.isAPM();
	private static final Logger LOGGER = LoggerFactory.make();

	public MobileNativeSimulator(UEMLoadScenario scenario) {
		super(scenario);
	}

	@Override
	protected void warmUp() throws Exception {
		Location location = getScenario().getRandomLocation();
		ExtendedCommonUser user = getScenario().getRandomUser(location.getCountry(), IS_RUXIT);
		location = user.getLocation();
		MobileDeviceType deviceType = getScenario().getMobileDevice(user);
		Bandwidth bandwidth = getScenario().getRandomBandwidth(location);
		int latency = bandwidth.getLatency();
		VisitorInfo visitorInfo = getScenario().getVisitorInfo(user);
		MobileDevice device = new MobileDevice(deviceType, location, latency, bandwidth, deviceType.getBrowserType(),
				getScenario().getRandomConnectionType(), visitorInfo.createVisitorID());
		try {
			Visit visit = getScenario().getRandomVisit(location);
			for (Action action : visit.getActions(user, location)) {
				action.run(device, null);
			}
		} finally {
			device.close();
		}
	}

	@Override
	protected Runnable createActionRunnerForVisit() {
		Location location = getScenario().getRandomLocation();
		ExtendedCommonUser user = getScenario().getRandomUser(location.getCountry(), IS_RUXIT);
		location = user.getLocation();
		MobileVisit visit = (MobileVisit) getScenario().getRandomVisit(location);
		MobileDeviceType deviceType = getScenario().getMobileDevice(user);
		Bandwidth bandwidth = getScenario().getRandomBandwidth(location);
		int latency = bandwidth.getLatency();
		VisitorInfo visitorInfo = getScenario().getVisitorInfo(user);
		MobileDevice device = new MobileDevice(deviceType, location, latency, bandwidth, deviceType.getBrowserType(),
				getScenario().getRandomConnectionType(), visitorInfo.createVisitorID());
		return new MobileActionRunner(this, device, visit.getActions(deviceType, user, location), 0);
	}

	private static class MobileActionRunner implements UEMLoadCallback, Runnable {

		private final Simulator simulator;
		private final MobileDevice device;
		private final Action[] actions;
		private final int index;

		public MobileActionRunner(Simulator simulator, MobileDevice device, Action[] actions, int index) {
			this.simulator = simulator;
			this.device = device;
			this.actions = ArrayUtils.clone(actions);
			this.index = index;
		}

		@Override
		public void run() {
			UEMLoadCallback continuation = new UEMLoadCallback() {

				@Override
				public void run() throws IOException {
					if (index + 1 < actions.length) {
						UemLoadScheduler.schedule(new MobileActionRunner(simulator, device, actions, index + 1), THINK_TIME, TimeUnit.MILLISECONDS);
					} else {
						device.close();
						simulator.incNumberOfFinishedVisits();
					}
				}
			};

			try {
				actions[index].run(device, continuation);
			} catch (IOException e) {
				ExceptionHandler.warn(e, LOGGER, actions[index].toString());
				device.close();
				simulator.incNumberOfFinishedVisits();
			} catch (Exception e) {
				LOGGER.info("Exception while running Mobile Native visit: ", e);
				device.close();
				simulator.incNumberOfFinishedVisits();
			}
			// no close of device in finally as we may have scheduled more work on this connection
		}
	}
}
