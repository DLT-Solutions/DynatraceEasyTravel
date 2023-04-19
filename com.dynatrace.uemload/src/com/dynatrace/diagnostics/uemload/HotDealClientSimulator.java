package com.dynatrace.diagnostics.uemload;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dynatrace.diagnostics.uemload.scenarios.HotDealScenario;
import com.dynatrace.easytravel.MessageConnector;
import com.dynatrace.easytravel.jms.JmsConnector;
import com.dynatrace.easytravel.rmi.RmiConnectionSocketFactory;
import com.dynatrace.easytravel.rmi.RmiConnector;

public class HotDealClientSimulator extends Simulator {
	private static final int JMS_PORT = 5446;
	private static final int RMI_PORT = 11230;
	private static final Pattern URL = Pattern.compile("http://([^:/]+)([:/].*)?");
	private static final Logger logger = Logger.getLogger(HotDealClientSimulator.class.getName());

	private List<Runnable> runnables = new LinkedList<>();

	public HotDealClientSimulator(HotDealScenario scenario) {
		super(scenario);

		runnables.add(new JmsRunnable(scenario));
		runnables.add(new RmiRunnable(scenario));
	}

	@Override
	protected void warmUp() throws Exception {
		// do nothing
	}

	@Override
	protected Runnable createActionRunnerForVisit() {
		return () -> {
			for (Runnable runnable : runnables) {
				runnable.run();
			}
		};
	}

	private abstract static class HotDealRunnable implements Runnable {
		private final HotDealScenario scenario;
		private MessageConnector connector;

		public HotDealRunnable(HotDealScenario scenario) {
			this.scenario = scenario;
		}

		protected abstract boolean isEnabled(HotDealScenario scenario);
		protected abstract MessageConnector connect(String host);

		@Override
		public void run() {
			if (isEnabled(scenario)) {
				if (connector == null) {
					String host = scenario.getHostsManager().getBackendHost();
					Matcher m = URL.matcher(host);
					if (m.matches()) {
						host = m.group(1);
						connector = connect(host);
					}
				}

				List<Integer> hotDealIds = connector.getHotDealIds();
				logger.log(Level.FINE, "got me some hot deals: {0}", hotDealIds);

				//awkward error detection: we'll get back the EMPTY_LIST on error.
				if (hotDealIds == Collections.EMPTY_LIST) {
					connector = null;
				}
			}

		}

	}

	private static class JmsRunnable extends HotDealRunnable {
		public JmsRunnable(HotDealScenario scenario) {
			super(scenario);
		}

		@Override
		protected boolean isEnabled(HotDealScenario scenario) {
			return scenario.useJms();
		}



		@Override
		protected MessageConnector connect(String host) {
			return new JmsConnector(host, JMS_PORT);
		}
	}

	private static class RmiRunnable extends HotDealRunnable {
		public RmiRunnable(HotDealScenario scenario) {
			super(scenario);
		}

		@Override
		protected boolean isEnabled(HotDealScenario scenario) {
			return scenario.useRmi();
		}

		@Override
		protected MessageConnector connect(String host) {
			RmiConnectionSocketFactory socketFactory = new RmiConnectionSocketFactory(host, RMI_PORT);
			return new RmiConnector(socketFactory);
		}
	}
}
