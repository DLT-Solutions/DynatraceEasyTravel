package com.dynatrace.easytravel.launcher.engine;


/**
 * Helper class to stop a set of procedures
 * @author stefan.moschinski
 */
public interface Stopper {

	/**
	 * Executes the stop of the passed procedures.
	 * @return
	 * @author stefan.moschinski
	 */
	boolean execute();

	/**
	 * Defines condition(s) when a procedure is not stoppable.
	 * @param proc
	 * @return
	 * @author stefan.moschinski
	 */
	boolean notStoppable(final Procedure proc);
}
