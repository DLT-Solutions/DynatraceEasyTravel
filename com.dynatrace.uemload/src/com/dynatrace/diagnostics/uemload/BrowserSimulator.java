package com.dynatrace.diagnostics.uemload;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.diagnostics.uemload.http.exception.ExceptionHandler;
import com.dynatrace.diagnostics.uemload.http.exception.SessionExpiredException;
import com.dynatrace.easytravel.logging.LoggerFactory;
import com.dynatrace.easytravel.util.TextUtils;

import ch.qos.logback.classic.Logger;

public class BrowserSimulator extends Simulator {
	private static final Logger LOGGER = LoggerFactory.make();

	public BrowserSimulator(UEMLoadScenario scenario) {
		super(scenario);
	}

	@Override
	protected void warmUp() throws Exception {
		ExtendedCommonUser user = getUserForVisit();
		Location location = getLocationForUser(user);
		Visit visit = getVisitForUser(location);
		BrowserType browserType = getScenario().getBrowser(user);
		BrowserWindowSize bws = getScenario().getBrowserWindowsSize(user);
		Bandwidth bandwidth = getScenario().getBandwidth(user);
		VisitorInfo visitorInfo = getScenario().getVisitorInfo(user);
		Browser browser = new Browser(browserType, location, 0, bandwidth, bws, visitorInfo.createVisitorID());

		int dnsSlowdownFactor = getScenario().getDNSSlowdownFactor(user);
		browser.setDNSSlowdownFactor(dnsSlowdownFactor);

		try {
			for (Action action : visit.getActions(user, location)) {
				action.run(browser, null);
			}
		} finally {
			browser.close();
		}
	}

	@Override
	protected Runnable createActionRunnerForVisit() {
		ExtendedCommonUser user = getUserForVisit();
		Location location = getLocationForUser(user);
		Visit visit = getVisitForUser(location);
		BrowserType browserType = getScenario().getBrowser(user);
		BrowserWindowSize bws = getScenario().getBrowserWindowsSize(user);
		Bandwidth bandwidth = getScenario().getBandwidth(user);
		int latency = bandwidth.getLatency();
		VisitorInfo visitorInfo = getScenario().getVisitorInfo(user);
		VisitorId visitorId = visitorInfo.createVisitorID();

		LOGGER.debug(TextUtils.merge(
				"New visit parameters. IP: {0}, visit type: {1}, bandwidth: {2} latency: {3}, visitor type: {4}, visitof ID: {5}",
				location.getIp(), visit.getVisitName(), bandwidth.name(), latency,
				visitorId.isNewVisitor() ? "New" : "Returning", visitorId.getVisitorId()));

		Browser browser = new Browser(browserType, location, latency, bandwidth, bws, visitorId);

		int dnsSlowdownFactor = getScenario().getDNSSlowdownFactor(user);
		browser.setDNSSlowdownFactor(dnsSlowdownFactor);

		return new ActionRunner(this, browser, visit.getActions(user, location), 0);
	}

	private static class ActionRunner implements UEMLoadCallback, Runnable {

		private final Simulator simulator;
		private final Browser browser;
		private final Action[] actions;
		private final int index;

		public ActionRunner(Simulator simulator, Browser browser, Action[] actions, int index) {
			this.simulator = simulator;
			this.browser = browser;
			this.actions = ArrayUtils.clone(actions);
			this.index = index;
		}

		@Override
		public void run() {
			UEMLoadCallback continuation = new UEMLoadCallback() {

				@Override
				public void run() throws IOException {
					if (index + 1 < actions.length) {
						UemLoadScheduler.schedule(new ActionRunner(simulator, browser, actions, index + 1), THINK_TIME, TimeUnit.MILLISECONDS);
					} else {
						simulator.incNumberOfFinishedVisits();
						browser.close();
					}
				}
			};

			try {
				actions[index].run(browser, continuation);
			} catch (SessionExpiredException e) {
				LOGGER.info("Session has expired.");
				if (LOGGER.isDebugEnabled()) LOGGER.debug("Session has expired", e);
				simulator.incNumberOfFinishedVisits();
				browser.close();
			} catch (IOException e) {
				LOGGER.warn(actions[index].toString(), e);
				ExceptionHandler.warn(e, LOGGER, actions[index].toString());
				simulator.incNumberOfFinishedVisits();
				browser.close();
			} catch (Exception e) {
				LOGGER.warn(actions[index].toString(), e);
				simulator.incNumberOfFinishedVisits();
				browser.close();
			}
			// no close of device in finally as we may have scheduled more work on this connection
		}
	}
}
