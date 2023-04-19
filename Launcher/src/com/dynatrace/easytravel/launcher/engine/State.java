package com.dynatrace.easytravel.launcher.engine;

/**
 * State of {@link Procedure}s and {@link Batch}es.
 *
 * @author martin.wurzinger
 */
public enum State {

    /** runnable entity has not been started or has already been stopped */
    STOPPED,

    /** runnable entity that is starting up at the moment */
    STARTING,

    /** runnable entity that is full serving */
    OPERATING,

    /** runnable entity that is stopping at the moment */
    STOPPING,


    /** We don't know about this Procedure at all */
    UNKNOWN,

    /** An error happened when trying to start the procedure */
    FAILED,

    ACCESS_DENIED,

    /** The procedure did not finish startup in time */
    TIMEOUT;

    /**
     * Get centrally defined state for initializations.
     *
     * @return
     * @author martin.wurzinger
     */
    public static State getDefault() {
        return STOPPED;
    }
}