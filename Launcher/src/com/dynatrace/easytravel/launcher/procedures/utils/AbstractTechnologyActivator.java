package com.dynatrace.easytravel.launcher.procedures.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dynatrace.easytravel.launcher.agent.Technology;
import com.dynatrace.easytravel.launcher.engine.State;
import com.dynatrace.easytravel.launcher.engine.StatefulProcedure;
import com.dynatrace.easytravel.launcher.misc.Constants;
import com.dynatrace.easytravel.launcher.plugin.BasePluginManager;
import com.dynatrace.easytravel.launcher.plugin.PluginManager;

public abstract class AbstractTechnologyActivator implements TechnologyActivator {

	/*
	 * To avoid causing SWT Thread exceptions,
	 * only one listener per class is allowed; otherwise, old listeners (i.e. HeaderPanel {*Wrong Thread*}) would accumulate in this list.
	 */
	private Map<Class<? extends TechnologyActivatorListener>, TechnologyActivatorListener> frontendListeners = new HashMap<Class<? extends TechnologyActivatorListener>, TechnologyActivatorListener>();
	private Set<TechnologyActivatorListener> backendListeners = new HashSet<TechnologyActivatorListener>();
	private Technology technology;
	private boolean enabled;
	private Collection<String> substitutes;
	private Collection<String> plugins;
	private boolean initiallyEnabled;

	public AbstractTechnologyActivator(Technology technology, Collection<String> plugins, Collection<String> substitutes) {
		this.technology = technology;
		this.plugins = plugins;
		this.substitutes = substitutes;
		// defaults:
		this.initiallyEnabled = true;
		this.enabled = true;
	}


	@Override
	public void enable() {
		this.enabled = true;
		notifyBackendListeners(true);
		disableSubstituePlugins();
		enablePlugins();
	}

	@Override
	public void disable() {
		this.enabled = false;
		disablePlugins();
		enableSubstitutePlugins();
		notifyBackendListeners(false);
	}


	@Override
	public void registerFrontendListener(TechnologyActivatorListener listener) {
		frontendListeners.put(listener.getClass(), listener);
	}

	@Override
	public void registerBackendListener(TechnologyActivatorListener listener) {
		backendListeners.add(listener);
	}

	@Override
	public void unregisterBackendListener(TechnologyActivatorListener listener) {
		backendListeners.remove(listener);
	}

	@Override
	public void notifyFrontedListeners(final boolean enabled) {
		for (TechnologyActivatorListener listener : frontendListeners.values()) {
			listener.notifyTechnologyStateChanged(technology, enabled, plugins, substitutes);
		}
	}

	@Override
	public void notifyBackendListeners(final boolean enabled) {
		for (TechnologyActivatorListener listener : backendListeners) {
			listener.notifyTechnologyStateChanged(technology, enabled, plugins, substitutes);
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}


	@Override
	public Technology getTechnology() {
		return technology;
	}


	@Override
	public void setEnabled(boolean enabled) {
		if (enabled) {
			enable();
		} else {
			disable();
		}
	}

	@Override
	public String getName() {
		return technology.getName();
	}


	@Override
	public void setInitiallyEnabled(boolean enabled) {
		this.initiallyEnabled = enabled;
		this.enabled = enabled;
		notifyFrontedListeners(enabled);
	}


	@Override
	public boolean wasInitiallyEnabled() {
		return initiallyEnabled;
	}


	@Override
	public boolean isChanged() {
		return initiallyEnabled != enabled;
	}


	private void switchPluginState(Collection<String> plugins, boolean enable) {
		if (noBusinessBackendRunning()) {
			return;
		}
		PluginManager pluginSetupManager = new BasePluginManager();
		if (enable) {
			pluginSetupManager.addPluginsToEnable(plugins);
		} else {
			pluginSetupManager.addPluginsToDisable(plugins);
		}
		pluginSetupManager.start();
	}


	protected void enablePlugins() {
		switchPluginState(plugins, true);
	}

	protected void disablePlugins() {
		switchPluginState(plugins, false);
	}

	protected void enableSubstitutePlugins() {
		switchPluginState(substitutes, true);
	}


	protected void disableSubstituePlugins() {
		switchPluginState(substitutes, false);
	}

	private boolean noBusinessBackendRunning() {
		return runningBackends.isEmpty();
	}

	@Override
	public Collection<String> getDefaultPlugins() {
		return Collections.unmodifiableCollection(plugins);
	}

	private Set<String> runningBackends = new HashSet<String>();

	@Override
	public void notifyProcedureStateChanged(StatefulProcedure subject, State oldState, State newState) {
		if (Constants.Procedures.BUSINESS_BACKEND_ID.equals(subject.getMapping().getId())) {
			if (newState == State.OPERATING || newState == State.TIMEOUT) {
				runningBackends.add(subject.getURI());
				if (!enabled) {
					disablePlugins();
					enableSubstitutePlugins();
				}
			} else if (oldState == State.OPERATING || oldState == State.TIMEOUT) {
				runningBackends.remove(subject.getURI());
			}
		}

	}

}
