package com.dynatrace.easytravel.launcher.procedures.utils;

import java.util.Collection;

import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.ProcedureStateListener;


/**
 * Allows you to activate/deactivate procedures of a specific technology.
 *
 * @author stefan.moschinski
 */
public interface TechnologyActivator extends ProcedureStateListener {

	/**
	 * Enables a specific technology and all its procedures
	 * @author stefan.moschinski
	 */
	void enable();

	/**
	 * Disables a specific technology and all its procedures
	 * @author stefan.moschinski
	 */
	void disable();

	/**
	 *
	 * @return <code>true</code> if the technology is currently enabled
	 * @author stefan.moschinski
	 */
	boolean isEnabled();

	void notifyBackendListeners(boolean enabled);

	void notifyFrontedListeners(boolean enabled);

	void registerFrontendListener(TechnologyActivatorListener listener);

	void registerBackendListener(TechnologyActivatorListener listener);

	void unregisterBackendListener(TechnologyActivatorListener listener);

	/**
	 *
	 * @return the {@link Technology} that is managed by this {@link TechnologyActivator}
	 * @author stefan.moschinski
	 */
	Technology getTechnology();

	/**
	 * Enables/disables the technology (and in turn the according procedures)
	 * @param enabled
	 * @author stefan.moschinski
	 */
	void setEnabled(boolean enabled);

	/**
	 *
	 * @return the name of the {@link Technology} that is managed
	 * 			by this {@link TechnologyActivator}
	 * @author stefan.moschinski
	 */
	String getName();

	/**
	 * Use this method to set the activation state of the
	 * technology at startup of easyTravel
	 *
	 * @param enabled <code>true</code> if the technology is activated at startup
	 * @author stefan.moschinski
	 */
	void setInitiallyEnabled(boolean enabled);

	/**
	 *
	 * @return <code>true</code> if the technology was enabled at startup of easyTravel
	 * @author stefan.moschinski
	 */
	boolean wasInitiallyEnabled();

	/**
	 *
	 * @return <code>true</code> if the technology activation state has changed
	 * 			compared to the state at the startup of easyTravel
	 * @author stefan.moschinski
	 */
	boolean isChanged();

	/**
	 *
	 * @return the names of the plugin that are activated if the represented technology is enabled
	 *
	 * @author stefan.moschinski
	 */
	Collection<String> getDefaultPlugins();



}
