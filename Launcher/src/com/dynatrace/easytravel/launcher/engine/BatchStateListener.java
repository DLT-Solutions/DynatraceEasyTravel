package com.dynatrace.easytravel.launcher.engine;

import com.dynatrace.easytravel.launcher.scenarios.Scenario;



/**
 * Listener for batch state change events.
 * 
 * @author martin.wurzinger
 */
public interface BatchStateListener {

    /**
     * Procedures can react on state changes of {@link Batch}.
     * @param scenario the Scenario that changes state
     * @param oldState the old batch state
     * @param newState the new batch state
     * 
     * @author martin.wurzinger
     */
    void notifyBatchStateChanged(Scenario scenario, State oldState, State newState);
}
