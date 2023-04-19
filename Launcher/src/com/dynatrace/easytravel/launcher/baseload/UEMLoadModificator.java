package com.dynatrace.easytravel.launcher.baseload;

import com.dynatrace.easytravel.config.ConfigChangeListener;
import com.dynatrace.easytravel.launcher.engine.BatchStateListener;


/**
 * Classes implementing this interface have the capability to change the UEM load settings at runtime
 * 
 * @author stefan.moschinski
 */
public interface UEMLoadModificator extends BatchStateListener, ConfigChangeListener {

	/**
	 * Enables the {@link UEMLoadModificator} instance
	 *
	 * @author stefan.moschinski
	 */
	void enable();

	/**
	 * Disables the {@link UEMLoadModificator} instance
	 *
	 * @author stefan.moschinski
	 */
	void disable();

	/**
	 *
	 * @return the {@link UEMLoadModificatorType} of the {@link UEMLoadModificator} instance
	 * @author stefan.moschinski
	 */
	UEMLoadModificatorType getType();

	enum UEMLoadModificatorType {
		LOAD_INCREASER,
		CONFIG_WATCHER;
	}

}
