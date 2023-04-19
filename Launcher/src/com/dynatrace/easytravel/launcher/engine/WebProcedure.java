package com.dynatrace.easytravel.launcher.engine;

/**
 * Interface for a procedure with a (maybe dynamic) assigned port.
 *
 * @author peter.kaiser
 */
public interface WebProcedure extends Procedure {

    /**
     * @return the (maybe dynamic) assigned port
     */
	int getPort();

    /**
     * @return the property name for this procedure's port (for passing to scripts like ant).
     */
	String getPortPropertyName();
}