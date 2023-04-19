package com.dynatrace.easytravel.launcher;

import java.util.concurrent.atomic.AtomicBoolean;

import ch.qos.logback.classic.Logger;

import com.dynatrace.easytravel.launcher.engine.BatchStateListener;
import com.dynatrace.easytravel.launcher.engine.LaunchEngine;
import com.dynatrace.easytravel.launcher.engine.ProcedureStateListener;
import com.dynatrace.easytravel.launcher.fancy.MenuActionCallback;
import com.dynatrace.easytravel.launcher.scenarios.Scenario;
import com.dynatrace.easytravel.logging.LoggerFactory;


public final class ScenarioController implements MenuActionCallback {
    private static final Logger log = LoggerFactory.make();

    /**
     * Boolean to see whether the is currently a scenario restarting.
     * This is needed because getRunningBatch() returns null
     * if there is a stopping scenario, and this behavior can't be changed.
     */
    private static final AtomicBoolean CURRENTY_RESTARTING = new AtomicBoolean();

    private final LaunchEngine engine = LaunchEngine.getNewInstance();
    private final Scenario scenario;
    private String text;

    public ScenarioController(Scenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public void run() {
        engine.runAsync(scenario);
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void stop() {
        LaunchEngine.stopAsync(scenario);
    }

    @Override
    public Boolean isEnabled() {
        return scenario.isEnabled();
    }

    public void addBatchStateListener(BatchStateListener listener) {
        engine.addBatchStateListener(listener);
    }

    public void addProcedureStateListener(ProcedureStateListener listener) {
        engine.addProcedureStateListener(listener);
    }

	public void restart() {
		// stop the Scenario synchronously
		try {
			if (!CURRENTY_RESTARTING.compareAndSet(false, true)) {
				return;
			}
	    	LaunchEngine.stop();

	    	log.info("Stopping Batch Done, now starting it again");

			// start it again
			engine.runAsync(scenario);
		} finally {
			CURRENTY_RESTARTING.set(false);
		}
	}

    /**
     * Returns true if there is currently a scenario restarting.
     * This is needed because getRunningBatch() returns null
     * if there is a stopping scenario, and this behavior can't be changed.
     */
    public static boolean isCurrentlyRestarting() {
    	return CURRENTY_RESTARTING.get();
    }
}
