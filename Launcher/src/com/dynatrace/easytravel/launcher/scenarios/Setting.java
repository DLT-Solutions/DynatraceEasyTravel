package com.dynatrace.easytravel.launcher.scenarios;

import com.dynatrace.easytravel.launcher.config.Persistable;
import com.dynatrace.easytravel.launcher.engine.Procedure;



/**
 *	Interface representing a general setting
 *
 * @author stefan.moschinski
 */
public interface Setting extends Persistable, Copyable<ProcedureSetting> {

	/**
	 * Get the type of this procedure setting. The meaning of a type is specified by the context of
	 * {@link Procedure}s.
	 *
	 * @return a string representing the setting type or <code>null</code> if no type is specified
	 * @author martin.wurzinger
	 */
	String getType();

	/**
	 * @return the unique name of the procedure setting that must not be <code>null</code>
	 * @author martin.wurzinger
	 */
	String getName();

	/**
	 * @return the value of the procedure setting that must not be <code>null</code>
	 * @author martin.wurzinger
	 */
	String getValue();

	/**
	 * Create a string-representation of this setting which is suitable for sending
	 * via REST.
	 *
	 * @return
	 * @author dominik.stadler
	 */
	String toREST();

}