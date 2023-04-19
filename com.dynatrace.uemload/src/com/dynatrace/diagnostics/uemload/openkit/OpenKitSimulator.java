package com.dynatrace.diagnostics.uemload.openkit;

import com.dynatrace.diagnostics.uemload.Simulator;
import com.dynatrace.diagnostics.uemload.UEMLoadScenario;
import com.dynatrace.diagnostics.uemload.UemLoadScheduler;
import com.dynatrace.diagnostics.uemload.openkit.action.EventType;
import com.dynatrace.diagnostics.uemload.openkit.visit.OpenKitVisit;
import com.dynatrace.diagnostics.uemload.utils.UemLoadUtils;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class OpenKitSimulator extends Simulator {
	protected static final Logger logger = Logger.getLogger(OpenKitSimulator.class.getName());

	public OpenKitSimulator(UEMLoadScenario scenario) {
		super(scenario);
	}

	@Override
	protected void warmUp() {
		// NOSONAR - not implemented on purpose
	}

	public static class ActionRunner<T extends EventType> implements Runnable {
		private final OpenKitVisit<T> visit;
		private final T[] actions;
		private final Simulator simulator;

		public ActionRunner(OpenKitVisit<T> visit, Simulator simulator) {
			this.visit = visit;
			this.actions = visit.getActions();
			this.simulator = simulator;
		}

		@Override
		public void run() {
			executeCommands();
		}

		void executeCommands() {
			final Runnable executor = () -> {
				try {
					for (T action : actions) {
						visit.executeAction(action);
						if (visit.getDevice().isCrashed() ) {
							visit.getDevice().sendCrash();
							break;
						}
						Thread.sleep(UemLoadUtils.randomInt(1000, 4000));
					}
				} catch (InterruptedException e) {
					logger.log(Level.SEVERE, "Visit thread interrupted while sleeping between actions", e);
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Exception thrown while executing visit", e);
				} finally {
					simulator.incNumberOfFinishedVisits();
					visit.finishVisit();
				}
			};
			UemLoadScheduler.scheduleOnlyIfFree(executor, 0, TimeUnit.SECONDS);
		}
	}
}
