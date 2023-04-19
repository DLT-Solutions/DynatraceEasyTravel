package com.dynatrace.easytravel.launcher.engine;


/**
 * Listener for procedure state change events.
 * 
 * @author martin.wurzinger
 */
public interface ProcedureStateListener {

    /**
     * <p>
     * Notifies implementors about a state change of the procedure <code>subject</code>.
     * </p>
     * <p>
     * Please note: The first call does not signal a state change (old and new states are equal) and
     * can be used to do some initialization work depending on the current state! Listeners are
     * notified the first time immediately after they have registered.
     * </p>
     * 
     * @param subject the procedure the state event is concerning
     * @param oldState the old procedure state
     * @param newState the new procedure state
     * @author martin.wurzinger
     */
    void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState);

}
